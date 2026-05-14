package com.kalibyte.foundry.enquiry.service.impl;

import com.kalibyte.foundry.common.exception.ResourceNotFoundException;
import com.kalibyte.foundry.common.response.PageResponse;
import com.kalibyte.foundry.common.util.SecurityUtils;
import com.kalibyte.foundry.customer.entity.Customer;
import com.kalibyte.foundry.customer.repository.CustomerRepository;
import com.kalibyte.foundry.enquiry.dto.request.EnquiryCreateRequest;
import com.kalibyte.foundry.enquiry.dto.request.EnquiryItemCreateRequest;
import com.kalibyte.foundry.enquiry.dto.request.EnquiryReviseRequest;
import com.kalibyte.foundry.enquiry.dto.response.EnquiryResponse;
import com.kalibyte.foundry.enquiry.entity.*;
import com.kalibyte.foundry.enquiry.entity.enums.EnquiryStatus;
import com.kalibyte.foundry.enquiry.entity.enums.MetalCategory;
import com.kalibyte.foundry.enquiry.entity.enums.MetalType;
import com.kalibyte.foundry.enquiry.mapper.EnquiryMapper;
import com.kalibyte.foundry.enquiry.repository.EnquiryRepository;
import com.kalibyte.foundry.enquiry.service.EnquiryService;
import com.kalibyte.foundry.common.castingprocess.service.CastingProcessService;
import com.kalibyte.foundry.quotation.entity.enums.QuotationStatus;
import com.kalibyte.foundry.quotation.repository.QuotationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class EnquiryServiceImpl implements EnquiryService {

    private final EnquiryRepository enquiryRepository;
    private final CustomerRepository customerRepository;
    private final EnquiryMapper enquiryMapper;
    private final QuotationRepository quotationRepository;
    private final CastingProcessService castingProcessService;

    @Override
    public EnquiryResponse create(EnquiryCreateRequest request) {

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Enquiry enquiry = Enquiry.builder()
                .enquiryNo(generateEnquiryNumber())
                .enquiryDate(request.getEnquiryDate())
                .customer(customer)
                .status(EnquiryStatus.PENDING)
                .build();

        enquiry.setCreatedBy(SecurityUtils.getCurrentUsername());

        List<EnquiryItem> items = new ArrayList<>();
        BigDecimal totalWeight = BigDecimal.ZERO;

        for (EnquiryItemCreateRequest itemReq : request.getEnquiryItems()) {

            MetalType type = itemReq.getMetalType();

            if (type == null) {
                throw new IllegalArgumentException("Metal type is required");
            }

            MetalCategory category = type.getCategory();

            BigDecimal itemWeight =
                    itemReq.getApproxPieceWeightKg()
                            .multiply(BigDecimal.valueOf(itemReq.getRequiredQuantity()));

            totalWeight = totalWeight.add(itemWeight);

            EnquiryItem item = new EnquiryItem();
            item.setEnquiry(enquiry);
            item.setPartName(itemReq.getPartName());
            item.setMetalCategory(category);
            item.setMaterialGrade(itemReq.getMaterialGrade());
            item.setMetalType(type);
            item.setCastingProcess(castingProcessService.getEntity(itemReq.getCastingProcessId()));
            item.setRequiredQuantity(itemReq.getRequiredQuantity());
            item.setApproxPieceWeightKg(itemReq.getApproxPieceWeightKg());
            item.setTotalWeightKg(itemWeight);
            item.setMachineRequired(itemReq.getMachineRequired());

            // NEW LOGIC
            if (itemReq.getPatternProvidedBy() == null) {
                throw new IllegalArgumentException("Pattern source is required");
            }

            item.setPatternProvidedBy(itemReq.getPatternProvidedBy());

            items.add(item);
        }

        enquiry.setTotalWeightKg(totalWeight);
        enquiry.setEnquiryItems(items);

        enquiryRepository.save(enquiry);

        return enquiryMapper.toResponse(enquiry);
    }

    private String generateEnquiryNumber() {
        int year = LocalDate.now().getYear();
        String prefix = "ENQ-" + year + "-";

        String lastNumber = enquiryRepository
                .findTopByEnquiryNoStartingWithOrderByEnquiryNoDesc(prefix)
                .map(Enquiry::getEnquiryNo)
                .orElse(null);

        int nextSequence = 1;

        if (lastNumber != null) {
            String[] parts = lastNumber.split("-");
            nextSequence = Integer.parseInt(parts[2]) + 1;
        }

        return String.format("%s%05d", prefix, nextSequence);
    }

    @Override
    public PageResponse<EnquiryResponse> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("enquiryDate").descending());
        Page<Enquiry> enquiryPage = enquiryRepository.findAll(pageable);
        return PageResponse.from(enquiryPage, enquiryMapper::toResponse);
    }

    @Override
    public EnquiryResponse getById(UUID enquiryId) {
        Enquiry enquiry = enquiryRepository.findById(enquiryId)
                .orElseThrow(() -> new ResourceNotFoundException("Enquiry not found"));
        return enquiryMapper.toResponse(enquiry);
    }

    @Override
    public PageResponse<EnquiryResponse> getByCustomerId(UUID customerId, int page, int size) {

        if (!customerRepository.existsById(customerId)){
            throw new ResourceNotFoundException("Customer not found");
        }

        Pageable pageable = PageRequest.of(page, size,Sort.by("enquiryDate").descending());
        Page<Enquiry> enquiryPage = enquiryRepository.findByCustomerId(customerId, pageable);

        return PageResponse.from(enquiryPage, enquiryMapper::toResponse);
    }

    @Override
    public EnquiryResponse updateStatus(UUID enquiryId, EnquiryStatus newStatus) {

        Enquiry enquiry = enquiryRepository.findById(enquiryId)
                .orElseThrow(() -> new ResourceNotFoundException("Enquiry not found"));

        enquiry.setStatus(newStatus);
        enquiry.setUpdatedBy(SecurityUtils.getCurrentUsername());

        return enquiryMapper.toResponse(enquiry);
    }

    /**
     * Revise an existing enquiry.
     * PURPOSE:
     * - Track changes to requirements for audit.
     * - Increments revision_no on the same enquiry record.
     * - Note: This does NOT auto-revise quotations.
     */
    @Override
    @Transactional
    public EnquiryResponse reviseEnquiry(UUID enquiryId, EnquiryReviseRequest request) {
        Enquiry enquiry = enquiryRepository.findById(enquiryId)
                .orElseThrow(() -> new ResourceNotFoundException("Enquiry not found"));

        // If active quotations exist for this enquiry, cancel them as they're now based on old requirements
        quotationRepository.findAllByEnquiryIdAndStatusNot(enquiryId, QuotationStatus.CANCELLED).forEach(q -> {
            q.setStatus(QuotationStatus.CANCELLED);
            q.setUpdatedBy(SecurityUtils.getCurrentUsername());
        });

        // Rule: Only the latest revision can be revised
        Integer maxRevision = enquiryRepository.findMaxRevisionByEnquiryNo(enquiry.getEnquiryNo());
        if (maxRevision != null && enquiry.getRevisionNo() < maxRevision) {
            throw new IllegalArgumentException("Only the latest revision of an enquiry can be revised.");
        }

        // Increment revision
        enquiry.setRevisionNo(enquiry.getRevisionNo() + 1);
        enquiry.setRevisionNote(request.getRevisionNote());
        enquiry.setStatus(EnquiryStatus.REVISED);
        enquiry.setUpdatedBy(SecurityUtils.getCurrentUsername());

        // Update items (clear and re-add for simplicity in tracking current state)
        enquiry.getEnquiryItems().clear();
        BigDecimal totalWeight = BigDecimal.ZERO;

        for (EnquiryItemCreateRequest itemReq : request.getItems()) {
            MetalType type = itemReq.getMetalType();
            MetalCategory category = type.getCategory();

            BigDecimal itemWeight = itemReq.getApproxPieceWeightKg()
                    .multiply(BigDecimal.valueOf(itemReq.getRequiredQuantity()));
            totalWeight = totalWeight.add(itemWeight);

            EnquiryItem item = EnquiryItem.builder()
                    .enquiry(enquiry)
                    .partName(itemReq.getPartName())
                    .metalCategory(category)
                    .materialGrade(itemReq.getMaterialGrade())
                    .metalType(type)
                    .castingProcess(castingProcessService.getEntity(itemReq.getCastingProcessId()))
                    .requiredQuantity(itemReq.getRequiredQuantity())
                    .approxPieceWeightKg(itemReq.getApproxPieceWeightKg())
                    .totalWeightKg(itemWeight)
                    .machineRequired(itemReq.getMachineRequired())
                    .patternProvidedBy(itemReq.getPatternProvidedBy())
                    .build();
            
            item.setCreatedBy(SecurityUtils.getCurrentUsername());
            enquiry.getEnquiryItems().add(item);
        }

        enquiry.setTotalWeightKg(totalWeight);
        enquiryRepository.save(enquiry);

        return enquiryMapper.toResponse(enquiry);
    }
}