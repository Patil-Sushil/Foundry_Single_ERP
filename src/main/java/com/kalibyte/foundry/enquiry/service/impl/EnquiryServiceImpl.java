package com.kalibyte.foundry.enquiry.service.impl;

import com.kalibyte.foundry.common.exception.ResourceNotFoundException;
import com.kalibyte.foundry.common.response.PageResponse;
import com.kalibyte.foundry.common.util.TenantUtils;
import com.kalibyte.foundry.customer.entity.Customer;
import com.kalibyte.foundry.customer.repository.CustomerRepository;
import com.kalibyte.foundry.enquiry.dto.EnquiryCreateRequest;
import com.kalibyte.foundry.enquiry.dto.EnquiryItemCreateRequest;
import com.kalibyte.foundry.enquiry.dto.EnquiryItemResponse;
import com.kalibyte.foundry.enquiry.dto.EnquiryResponse;
import com.kalibyte.foundry.enquiry.entity.Enquiry;
import com.kalibyte.foundry.enquiry.entity.EnquiryItem;
import com.kalibyte.foundry.enquiry.entity.MetalCategory;
import com.kalibyte.foundry.enquiry.entity.MetalType;
import com.kalibyte.foundry.enquiry.repository.EnquiryRepository;
import com.kalibyte.foundry.enquiry.repository.MetalCategoryRepository;
import com.kalibyte.foundry.enquiry.repository.MetalTypeRepository;
import com.kalibyte.foundry.enquiry.service.EnquiryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Transactional
public class EnquiryServiceImpl implements EnquiryService {

    private final EnquiryRepository enquiryRepository;
    private final CustomerRepository customerRepository;
    private final MetalCategoryRepository metalCategoryRepository;
    private final MetalTypeRepository metalTypeRepository;
    private final EnquiryRepository.EnquiryNumberGenerator enquiryNumberGenerator;

    @Override
    public EnquiryResponse create(EnquiryCreateRequest request) {

        Long tenantId = TenantUtils.getTenantId();

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        long yearlyCount = enquiryRepository.countForYear(
                tenantId,
                LocalDate.now().getYear()
        );

        Enquiry enquiry = Enquiry.builder()
                .tenantId(tenantId)
                .enquiryNo(enquiryNumberGenerator.generate(tenantId, yearlyCount))
                .enquiryDate(request.getEnquiryDate())
                .customer(customer)
                .status("NEW")
                .build();

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

    @Override
    public PageResponse<EnquiryResponse> getAll(int page, int size) {

        Long tenantId = TenantUtils.getTenantId();

        Pageable pageable =
                PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Enquiry> enquiryPage =
                enquiryRepository.findAllByTenantId(tenantId, pageable);

        return PageResponse.from(enquiryPage.map(this::toResponse));
    }


    @Override
    public EnquiryResponse getById(UUID enquiryId) {

        Long tenantId = TenantUtils.getTenantId();

        Enquiry enquiry = enquiryRepository
                .findByIdAndTenantId(enquiryId, tenantId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Enquiry not found"));

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
