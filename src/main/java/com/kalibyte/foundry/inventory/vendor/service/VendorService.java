package com.kalibyte.foundry.inventory.vendor.service;

import com.kalibyte.foundry.common.response.PageResponse;
import com.kalibyte.foundry.inventory.vendor.dto.request.CreateVendorRequest;
import com.kalibyte.foundry.inventory.vendor.dto.request.UpdateVendorRequest;
import com.kalibyte.foundry.inventory.vendor.dto.response.VendorResponse;
import com.kalibyte.foundry.inventory.vendor.dto.response.VendorSummary;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface VendorService {
    VendorResponse create(CreateVendorRequest request);
    VendorResponse update(Long id, UpdateVendorRequest request);
    VendorResponse getById(Long id);
    PageResponse<VendorResponse> getAll(Boolean isActive, Pageable pageable);
    void deactivate(Long id);
    List<VendorSummary> search(String query);
}
