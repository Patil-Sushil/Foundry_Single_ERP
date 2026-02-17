package com.kalibyte.foundry.enquiry.service.impl;

import com.kalibyte.foundry.common.exception.ResourceNotFoundException;
import com.kalibyte.foundry.common.response.PageResponse;
import com.kalibyte.foundry.common.util.SecurityUtils;
import com.kalibyte.foundry.customer.entity.Customer;
import com.kalibyte.foundry.customer.repository.CustomerRepository;
import com.kalibyte.foundry.enquiry.dto.*;
import com.kalibyte.foundry.enquiry.entity.*;
import com.kalibyte.foundry.enquiry.repository.*;
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
    private final MetalCategoryRepository metalCategoryRepository;
    private final MetalTypeRepository metalTypeRepository;

    @Override
    public EnquiryResponse create(EnquiryCreateRequest request) {

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Enquiry enquiry = Enquiry.builder()
                .enquiryNo(generateEnquiryNumber())
                .enquiryDate(request.getEnquiryDate())
                .customer(customer)
                .status("NEW")
                .build();

        enquiry.setCreatedBy(SecurityUtils.getCurrentUsername());

        List<EnquiryItem> items = new ArrayList<>();
        BigDecimal totalWeight = BigDecimal.ZERO;

        for (EnquiryItemCreateRequest itemReq : request.getEnquiryItems()) {

            MetalCategory category = metalCategoryRepository.findById(itemReq.getMetalCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Metal category not found"));

            MetalType type = metalTypeRepository.findById(itemReq.getMetalTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Metal type not found"));

            BigDecimal itemWeight =
                    itemReq.getApproxPieceWeightKg()
                            .multiply(BigDecimal.valueOf(itemReq.getRequiredQuantity()));

            totalWeight = totalWeight.add(itemWeight);

            EnquiryItem item = EnquiryItem.builder()
                    .enquiry(enquiry)
                    .partName(itemReq.getPartName())
                    .metalCategory(category)
                    .metalType(type)
                    .castingProcess(itemReq.getCastingProcess())
                    .requiredQuantity(itemReq.getRequiredQuantity())
                    .approxPieceWeightKg(itemReq.getApproxPieceWeightKg())
                    .totalWeightKg(itemWeight)
                    .patternAvailable(itemReq.getPatternAvailable())
                    .machineRequired(itemReq.getMachineRequired())
                    .build();

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

    private EnquiryResponse toResponse(Enquiry enquiry) {

        List<EnquiryItemResponse> itemResponses =
                enquiry.getEnquiryItems()
                        .stream()
                        .map(item -> EnquiryItemResponse.builder()
                                .partName(item.getPartName())
                                .metalCategory(item.getMetalCategory().getName())
                                .metalType(item.getMetalType().getName())
                                .requiredQuantity(item.getRequiredQuantity())
                                .approxPieceWeightKg(item.getApproxPieceWeightKg())
                                .totalWeightKg(item.getTotalWeightKg())
                                .castingProcess(item.getCastingProcess())
                                .patternAvailable(item.getPatternAvailable())
                                .machineRequired(item.getMachineRequired())
                                .build())
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
