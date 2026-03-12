package com.kalibyte.foundry.enquiry.controller;

import com.kalibyte.foundry.common.response.ApiResponse;
import com.kalibyte.foundry.common.response.PageResponse;
import com.kalibyte.foundry.enquiry.dto.request.EnquiryCreateRequest;
import com.kalibyte.foundry.enquiry.dto.request.UpdateEnquiryStatusRequest;
import com.kalibyte.foundry.enquiry.dto.response.EnquiryResponse;
import com.kalibyte.foundry.enquiry.service.EnquiryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/enquiries")
@RequiredArgsConstructor
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

    // Get all enquiries related to a specific customer
    @GetMapping("/customer/{customerId}")
    public ApiResponse<PageResponse<EnquiryResponse>> getEnquiriesByCustomerId(
            @PathVariable UUID customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success(enquiryService.getByCustomerId(customerId, page, size));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<EnquiryResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEnquiryStatusRequest request) {

        EnquiryResponse response =
                enquiryService.updateStatus(id, request.getStatus());

        return ApiResponse.success(response);
    }
}
