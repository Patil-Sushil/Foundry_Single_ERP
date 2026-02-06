package com.kalibyte.foundry.enquiry.controller;

import com.kalibyte.foundry.common.response.ApiResponse;
import com.kalibyte.foundry.common.response.PageResponse;
import com.kalibyte.foundry.enquiry.dto.EnquiryCreateRequest;
import com.kalibyte.foundry.enquiry.dto.EnquiryResponse;
import com.kalibyte.foundry.enquiry.service.EnquiryService;
import com.kalibyte.foundry.infrastructure.tenancy.annotation.TenantRequired;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/enquiries")
@RequiredArgsConstructor
@TenantRequired
public class EnquiryController {

    private final EnquiryService enquiryService;

    // Create new enquiry
    @PostMapping
    public ApiResponse<EnquiryResponse> create(
            @Valid @RequestBody EnquiryCreateRequest request
    ) {
        return ApiResponse.success(enquiryService.create(request));
    }

    // Get All Enquiries with Pagination
    @GetMapping
    public ApiResponse<PageResponse<EnquiryResponse>> getAllEnquiries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success(enquiryService.getAll(page, size));
    }

    // Get Enquiry by ID
    @GetMapping("/{id}")
    public ApiResponse<EnquiryResponse> getEnquiryById(
            @PathVariable("id") UUID id
    ) {
        return ApiResponse.success(enquiryService.getById(id));
    }
}
