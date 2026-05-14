package com.kalibyte.foundry.production.controller;

import com.kalibyte.foundry.common.response.ApiResponse;
import com.kalibyte.foundry.common.response.PageResponse;
import com.kalibyte.foundry.production.dto.request.ProductionEntryRequest;
import com.kalibyte.foundry.production.dto.request.UpdateStatusRequest;
import com.kalibyte.foundry.production.dto.response.entry.ProductionEntryListItem;
import com.kalibyte.foundry.production.dto.response.entry.ProductionEntryResponse;
import com.kalibyte.foundry.production.entity.enums.ProductionShift;
import com.kalibyte.foundry.production.entity.enums.ProductionStatus;
import com.kalibyte.foundry.production.service.ProductionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/production")
public class ProductionController {

    private final ProductionService service;

    public ProductionController(ProductionService service) {
        this.service = service;
    }

    // ── CREATE ──────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','PRODUCTION')")
    public ResponseEntity<ApiResponse<ProductionEntryResponse>> create(
            @Valid @RequestBody ProductionEntryRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.createEntry(request)));
    }

    // ── GET BY ID ───────────────────────────────────

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','PRODUCTION')")
    public ResponseEntity<ApiResponse<ProductionEntryResponse>> getById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    // ── LIST (PAGINATED + FILTERED) ─────────────────

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','PRODUCTION')")
    public ResponseEntity<ApiResponse<PageResponse<ProductionEntryListItem>>> list(
            @RequestParam(required = false) UUID orderId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) ProductionStatus status,
            @RequestParam(required = false) ProductionShift shift,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                service.list(orderId, fromDate, toDate, status, shift, page, size)
        ));
    }

    // ── UPDATE STATUS ───────────────────────────────

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','PRODUCTION')")
    public ResponseEntity<ApiResponse<ProductionEntryResponse>> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStatusRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(service.updateStatus(id, request)));
    }

    // ── DELETE (SOFT) ───────────────────────────────

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Deleted successfully", null));
    }

    //    ── UPDATE ENTRY (FULL) ───────────────────────────
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','PRODUCTION')")
    public ResponseEntity<ApiResponse<ProductionEntryResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody ProductionEntryRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(service.updateEntry(id, request)));
    }
}