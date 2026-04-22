package com.kalibyte.foundry.inventory.inward.controller;

import com.kalibyte.foundry.common.response.ApiResponse;
import com.kalibyte.foundry.common.response.PageResponse;
import com.kalibyte.foundry.inventory.inward.dto.request.ConfirmInwardRequest;
import com.kalibyte.foundry.inventory.inward.dto.request.StartInwardRequest;
import com.kalibyte.foundry.inventory.inward.dto.request.UpdateReceivedQuantityRequest;
import com.kalibyte.foundry.inventory.inward.dto.response.InwardResponse;
import com.kalibyte.foundry.inventory.inward.dto.response.InwardReviewResponse;
import com.kalibyte.foundry.inventory.inward.dto.response.InwardSummary;
import com.kalibyte.foundry.inventory.inward.entity.enums.InwardStatus;
import com.kalibyte.foundry.inventory.inward.service.MaterialInwardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/inwards")
@Tag(name = "Material Inward", description = "Material Inward Management APIs")
@PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'FINANCE')")
public class MaterialInwardController {

    private final MaterialInwardService materialInwardService;

	public MaterialInwardController(MaterialInwardService materialInwardService) {
		this.materialInwardService = materialInwardService;
	}

	@PostMapping("/from-po/{poId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<InwardResponse> startFromPO(
            @PathVariable Long poId,
            @Valid @RequestBody StartInwardRequest request) {
        return ApiResponse.success("Inward started successfully", 
                materialInwardService.startFromPO(poId, request));
    }

    @PutMapping("/{id}/received-quantities")
    public ApiResponse<InwardResponse> updateReceivedQuantities(
            @PathVariable Long id,
            @RequestBody List<@Valid UpdateReceivedQuantityRequest> requests) {
        return ApiResponse.success("Received quantities updated successfully",
                materialInwardService.updateReceivedQuantities(id, requests));
    }
    @GetMapping("/{id}/review")
    public ApiResponse<InwardReviewResponse> getReview(@PathVariable Long id) {
        return ApiResponse.success("Inward review retrieved successfully", 
                materialInwardService.getReview(id));
    }

    @PostMapping("/{id}/confirm")
    public ApiResponse<InwardResponse> confirm(
            @PathVariable Long id,
            @RequestBody(required = false) ConfirmInwardRequest request) {
        return ApiResponse.success("Inward confirmed successfully", 
                materialInwardService.confirm(id, request));
    }

    @GetMapping("/{id}")
    public ApiResponse<InwardResponse> getById(@PathVariable Long id) {
        return ApiResponse.success("Inward retrieved successfully", 
                materialInwardService.getById(id));
    }

    @GetMapping
    public ApiResponse<PageResponse<InwardSummary>> getAll(
            @RequestParam(required = false) InwardStatus status,
            @RequestParam(required = false) Long vendorId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            Pageable pageable) {
        return ApiResponse.success("Inwards retrieved successfully", 
                materialInwardService.getAll(status, vendorId, from, to, pageable));
    }
}
