package com.kalibyte.foundry.qa.customerreturn.controller;

import com.kalibyte.foundry.common.response.ApiResponse;
import com.kalibyte.foundry.qa.common.enums.ReturnDisposition;
import com.kalibyte.foundry.qa.common.enums.ReturnStatus;
import com.kalibyte.foundry.qa.customerreturn.dto.*;
import com.kalibyte.foundry.qa.customerreturn.entity.CustomerReturn;
import com.kalibyte.foundry.qa.customerreturn.mapper.CustomerReturnMapper;
import com.kalibyte.foundry.qa.customerreturn.service.CustomerReturnService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/qa/customer-returns")
@RequiredArgsConstructor
public class CustomerReturnController {

    private final CustomerReturnService service;
    private final CustomerReturnMapper mapper;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY')")
    public ResponseEntity<ApiResponse<CustomerReturnResponse>> receive(@Valid @RequestBody CustomerReturnRequest request) {
        CustomerReturn entity = mapper.toEntity(request);
        return ResponseEntity.ok(ApiResponse.success(mapper.toResponse(service.receiveReturn(entity))));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY', 'PRODUCTION')")
    public ResponseEntity<ApiResponse<CustomerReturnResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(mapper.toResponse(service.getById(id))));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY', 'PRODUCTION')")
    public ResponseEntity<ApiResponse<List<CustomerReturnResponse>>> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) UUID orderId,
            @RequestParam(required = false) ReturnStatus status,
            @RequestParam(required = false) ReturnDisposition disposition
    ) {
        return ResponseEntity.ok(ApiResponse.success(mapper.toResponseList(service.list(startDate, endDate, customerId, orderId, status, disposition))));
    }

    @PatchMapping("/{id}/assess")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY')")
    public ResponseEntity<ApiResponse<CustomerReturnResponse>> assess(
            @PathVariable Long id,
            @Valid @RequestBody CustomerReturnAssessmentRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(mapper.toResponse(service.assessReturn(
                id, request.getQaFinding(), request.getRootCauseCategory(), request.getRootCauseDescription(),
                request.getInspectorName(), request.getRemarks()))));
    }

    @PatchMapping("/{id}/disposition")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY')")
    public ResponseEntity<ApiResponse<CustomerReturnResponse>> disposition(
            @PathVariable Long id,
            @Valid @RequestBody CustomerReturnDispositionRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(mapper.toResponse(service.dispositionReturn(
                id, request.getDisposition(), request.getRemarks(), request.getPerformedBy(),
                request.getCreditAmount(), request.getReplacementOrderId()))));
    }

    @PatchMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY')")
    public ResponseEntity<ApiResponse<CustomerReturnResponse>> close(@PathVariable Long id, @RequestParam String performedBy) {
        return ResponseEntity.ok(ApiResponse.success(mapper.toResponse(service.closeReturn(id, performedBy))));
    }

    @GetMapping("/by-customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY', 'PRODUCTION')")
    public ResponseEntity<ApiResponse<List<CustomerReturnResponse>>> getByCustomer(@PathVariable UUID customerId) {
        return ResponseEntity.ok(ApiResponse.success(mapper.toResponseList(service.list(null, null, customerId, null, null, null))));
    }
}
