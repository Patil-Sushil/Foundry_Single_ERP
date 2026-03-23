package com.kalibyte.foundry.enquiry.service.impl;

import com.kalibyte.foundry.common.exception.ResourceNotFoundException;
import com.kalibyte.foundry.common.response.PageResponse;
import com.kalibyte.foundry.common.util.SecurityUtils;
import com.kalibyte.foundry.customer.entity.Customer;
import com.kalibyte.foundry.customer.repository.CustomerRepository;
import com.kalibyte.foundry.enquiry.dto.request.EnquiryCreateRequest;
import com.kalibyte.foundry.enquiry.dto.request.EnquiryItemCreateRequest;
import com.kalibyte.foundry.enquiry.dto.response.EnquiryResponse;
import com.kalibyte.foundry.enquiry.entity.*;
import com.kalibyte.foundry.enquiry.entity.enums.EnquiryStatus;
import com.kalibyte.foundry.enquiry.entity.enums.MetalCategory;
import com.kalibyte.foundry.enquiry.entity.enums.MetalType;
import com.kalibyte.foundry.enquiry.mapper.EnquiryMapper;
import com.kalibyte.foundry.enquiry.repository.EnquiryRepository;
import com.kalibyte.foundry.enquiry.service.EnquiryService;
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
            item.setMetalType(type);
            item.setCastingProcess(itemReq.getCastingProcess());
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
}