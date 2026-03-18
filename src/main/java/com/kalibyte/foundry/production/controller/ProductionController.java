package com.kalibyte.foundry.production.controller;

import com.kalibyte.foundry.common.response.ApiResponse;
import com.kalibyte.foundry.production.dto.request.ProductionEntryRequest;
import com.kalibyte.foundry.production.dto.request.UpdateStatusRequest;
import com.kalibyte.foundry.production.dto.response.entry.ProductionEntryResponse;
import com.kalibyte.foundry.production.service.ProductionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/production")
@RequiredArgsConstructor
public class ProductionController {

    private final ProductionService service;

    //------------------------------------------------
    // CREATE ENTRY
    //------------------------------------------------

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','PRODUCTION')")
    public ResponseEntity<ApiResponse<ProductionEntryResponse>> create(
            @Valid @RequestBody ProductionEntryRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.createEntry(request)));
    }

    //------------------------------------------------
    // GET BY ID
    //------------------------------------------------

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','PRODUCTION')")
    public ResponseEntity<ApiResponse<ProductionEntryResponse>> get(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    //------------------------------------------------
    // UPDATE STATUS
    //------------------------------------------------

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','PRODUCTION')")
    public ResponseEntity<ApiResponse<ProductionEntryResponse>> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStatusRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(service.updateStatus(id, request)));
    }

    //------------------------------------------------
    // DELETE (SOFT)
    //------------------------------------------------

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable UUID id) {

        service.delete(id);

        return ResponseEntity.ok(ApiResponse.success("Deleted successfully", null));
    }
}
