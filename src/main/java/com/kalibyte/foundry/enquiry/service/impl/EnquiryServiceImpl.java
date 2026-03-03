package com.kalibyte.foundry.enquiry.service.impl;

import com.kalibyte.foundry.common.exception.ResourceNotFoundException;
import com.kalibyte.foundry.common.response.PageResponse;
import com.kalibyte.foundry.common.util.SecurityUtils;
import com.kalibyte.foundry.customer.entity.Customer;
import com.kalibyte.foundry.customer.repository.CustomerRepository;
import com.kalibyte.foundry.enquiry.dto.request.EnquiryCreateRequest;
import com.kalibyte.foundry.enquiry.dto.request.EnquiryItemCreateRequest;
import com.kalibyte.foundry.enquiry.dto.response.EnquiryItemResponse;
import com.kalibyte.foundry.enquiry.dto.response.EnquiryResponse;
import com.kalibyte.foundry.enquiry.entity.*;
import com.kalibyte.foundry.enquiry.entity.ENUM.EnquiryStatus;
import com.kalibyte.foundry.enquiry.entity.ENUM.MetalCategory;
import com.kalibyte.foundry.enquiry.entity.ENUM.MetalType;
import com.kalibyte.foundry.enquiry.repository.EnquiryRepository;
import com.kalibyte.foundry.enquiry.service.EnquiryService;
import com.kalibyte.foundry.pattern.dto.request.PatternReceiptRequest;
import com.kalibyte.foundry.pattern.entity.Pattern;
import com.kalibyte.foundry.pattern.entity.PatternReceipt;
import com.kalibyte.foundry.pattern.repository.PatternRepository;
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
    private final PatternRepository patternRepository;

    @Override
    public EnquiryResponse create(EnquiryCreateRequest request) {

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Enquiry enquiry = Enquiry.builder()
                .enquiryNo(generateEnquiryNumber())
                .enquiryDate(request.getEnquiryDate())
                .customer(customer)
                .status(EnquiryStatus.PENDING) // Default status for new enquiries
                .build();

        enquiry.setCreatedBy(SecurityUtils.getCurrentUsername());

        List<EnquiryItem> items = new ArrayList<>();
        BigDecimal totalWeight = BigDecimal.ZERO;

        for (EnquiryItemCreateRequest itemReq : request.getEnquiryItems()) {

            if (Boolean.TRUE.equals(itemReq.getPatternProvidedByCustomer())) {
                if (itemReq.getPatternReceipt() == null) {
                    throw new IllegalArgumentException(
                            "Pattern receipt required when customer provides pattern"
                    );
                }
            } else {
                if (itemReq.getPatternId() == null) {
                    throw new IllegalArgumentException(
                            "Pattern ID required when pattern not provided by customer"
                    );
                }
            }

            //  ENUM BASED METAL VALIDATION
            MetalType type = itemReq.getMetalType();

            if (type == null) {
                throw new IllegalArgumentException("Metal type is required");
            }

            //  Automatically derive category
            MetalCategory category = type.getCategory();

            if (!MetalType.isValidForCategory(type, category)) {
                throw new IllegalArgumentException(
                        "Selected metal type does not belong to selected category"
                );
            }

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
            item.setPatternProvidedByCustomer(itemReq.getPatternProvidedByCustomer());

            if (Boolean.TRUE.equals(itemReq.getPatternProvidedByCustomer())) {

                PatternReceiptRequest pr = itemReq.getPatternReceipt();

                PatternReceipt receipt = PatternReceipt.builder()
                        .inwardDate(pr.getInwardDate())
                        .outwardDate(pr.getOutwardDate())
                        .name(pr.getName())
                        .type(pr.getType())
                        .material(pr.getMaterial())
                        .build();

                receipt.setCreatedBy(SecurityUtils.getCurrentUsername());
                item.setPatternReceipt(receipt);

            } else {

                Pattern pattern = patternRepository.findById(itemReq.getPatternId())
                        .orElseThrow(() -> new ResourceNotFoundException("Pattern not found"));

                item.setPattern(pattern);
            }

            items.add(item);
        }

        enquiry.setTotalWeightKg(totalWeight);
        enquiry.setEnquiryItems(items);

        enquiryRepository.save(enquiry);

        return toResponse(enquiry);
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

        return PageResponse.from(enquiryPage, this::toResponse);
    }

    @Override
    public EnquiryResponse getById(UUID enquiryId) {

        Enquiry enquiry = enquiryRepository.findById(enquiryId)
                .orElseThrow(() -> new ResourceNotFoundException("Enquiry not found"));

        return toResponse(enquiry);
    }

    @Override
    public PageResponse<EnquiryResponse> getByCustomerId(UUID customerId, int page, int size) {

        // Check if customer exists
        if (!customerRepository.existsById(customerId)){
            throw new ResourceNotFoundException("Customer not found");
        }

        Pageable pageable = PageRequest.of(page, size,Sort.by("enquiryDate").descending());

        Page<Enquiry>enquiryPage = enquiryRepository.findByCustomerId(customerId, pageable);

        return PageResponse.from(enquiryPage, this::toResponse);
    }

    @Override
    public EnquiryResponse updateStatus(UUID enquiryId, EnquiryStatus newStatus) {
        Enquiry enquiry = enquiryRepository.findById(enquiryId)
                .orElseThrow(() -> new ResourceNotFoundException("Enquiry not found"));
        EnquiryStatus CurrentStatus = enquiry.getStatus();

        validateSatusTransition(CurrentStatus, newStatus);

        enquiry.setStatus(newStatus);
        enquiry.setUpdatedBy(SecurityUtils.getCurrentUsername());

        return toResponse(enquiry);


    }

    private void validateSatusTransition(EnquiryStatus current, EnquiryStatus newStatus) {
        if (current == EnquiryStatus.CLOSED) {
            throw new IllegalStateException("Cannot change status of CLOSED enquiry");
        }

        if (current == EnquiryStatus.PENDING &&
                !(newStatus == EnquiryStatus.QUOTED || newStatus == EnquiryStatus.CLOSED)) {
            throw new IllegalStateException("Invalid status transition from PENDING");
        }

        if (current == EnquiryStatus.QUOTED &&
                newStatus != EnquiryStatus.CLOSED) {
            throw new IllegalStateException("Invalid status transition from QUOTED");
        }
    }

    private EnquiryResponse toResponse(Enquiry enquiry) {

        List<EnquiryItemResponse> itemResponses = enquiry.getEnquiryItems()
                .stream()
                .map(item -> {

                    String patternName = null;
                    String patternType = null;
                    String patternMaterial = null;
                    LocalDate inwardDate = null;
                    LocalDate outwardDate = null;

                    if (Boolean.TRUE.equals(item.getPatternProvidedByCustomer())) {

                        PatternReceipt pr = item.getPatternReceipt();

                        if (pr != null) {
                            patternName = pr.getName();
                            patternType = pr.getType().name();
                            patternMaterial = pr.getMaterial().name();
                            inwardDate = pr.getInwardDate();
                            outwardDate = pr.getOutwardDate();
                        }

                    } else {

                        Pattern pattern = item.getPattern();

                        if (pattern != null) {
                            patternName = pattern.getName();
                            patternType = pattern.getType().name();
                            patternMaterial = pattern.getMaterial().name();
                        }
                    }

                    return EnquiryItemResponse.builder()
                            .partName(item.getPartName())
                            .metalCategory(item.getMetalCategory().getDisplayName())
                            .metalType(item.getMetalType().getDisplayName())
                            .requiredQuantity(item.getRequiredQuantity())
                            .approxPieceWeightKg(item.getApproxPieceWeightKg())
                            .totalWeightKg(item.getTotalWeightKg())
                            .castingProcess(item.getCastingProcess())
                            .machineRequired(item.getMachineRequired())
                            .patternProvidedByCustomer(item.getPatternProvidedByCustomer())
                            .patternName(patternName)
                            .patternType(patternType)
                            .patternMaterial(patternMaterial)
                            .inwardDate(inwardDate)
                            .outwardDate(outwardDate)
                            .build();
                })
                .toList();

        return EnquiryResponse.builder()
                .id(enquiry.getId())
                .enquiryNo(enquiry.getEnquiryNo())
                .enquiryDate(enquiry.getEnquiryDate())
                .customerName(enquiry.getCustomer().getName())
                .totalWeightKg(enquiry.getTotalWeightKg())
                .status(enquiry.getStatus())
                .items(itemResponses)
                .build();
    }
}