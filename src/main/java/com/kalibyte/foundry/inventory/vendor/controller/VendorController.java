package com.kalibyte.foundry.inventory.vendor.controller;

import com.kalibyte.foundry.common.response.ApiResponse;
import com.kalibyte.foundry.common.response.PageResponse;
import com.kalibyte.foundry.common.security.UserPrincipal;
import com.kalibyte.foundry.inventory.vendor.dto.request.CreateVendorRequest;
import com.kalibyte.foundry.inventory.vendor.dto.request.UpdateVendorRequest;
import com.kalibyte.foundry.inventory.vendor.dto.response.VendorResponse;
import com.kalibyte.foundry.inventory.vendor.dto.response.VendorSummary;
import com.kalibyte.foundry.inventory.vendor.service.VendorService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vendors")
@Tag(name = "Vendors", description = "Vendor Management APIs")
public class VendorController {

    private final VendorService vendorService;

	public VendorController(VendorService vendorService) {
		this.vendorService = vendorService;
	}

	@PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<VendorResponse> create(
            @Valid @RequestBody CreateVendorRequest request) {
        return ApiResponse.success("Vendor created successfully", vendorService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','STORE')")
    public ApiResponse<PageResponse<VendorResponse>> getAll(
            @RequestParam(required = false) Boolean isActive,
            Pageable pageable) {
        return ApiResponse.success("Vendors retrieved successfully", vendorService.getAll(isActive, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STORE')")
    public ApiResponse<VendorResponse> getById(@PathVariable Long id) {
        return ApiResponse.success("Vendor retrieved successfully", vendorService.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STORE')")
    public ApiResponse<VendorResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateVendorRequest request) {
        return ApiResponse.success("Vendor updated successfully", vendorService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable Long id) {
        vendorService.deactivate(id);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN','STORE')")
    public ApiResponse<List<VendorSummary>> search(@RequestParam String q) {
        return ApiResponse.success("Vendors searched successfully", vendorService.search(q));
    }
}
