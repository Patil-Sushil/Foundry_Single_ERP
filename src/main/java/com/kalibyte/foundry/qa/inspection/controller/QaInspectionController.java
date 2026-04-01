package com.kalibyte.foundry.qa.inspection.controller;

import com.kalibyte.foundry.common.response.ApiResponse;
import com.kalibyte.foundry.qa.common.enums.InspectionResult;
import com.kalibyte.foundry.qa.common.enums.InspectionStage;
import com.kalibyte.foundry.qa.common.enums.InspectionStatus;
import com.kalibyte.foundry.qa.inspection.dto.QaInspectionRequest;
import com.kalibyte.foundry.qa.inspection.dto.QaInspectionResponse;
import com.kalibyte.foundry.qa.inspection.entity.QaInspection;
import com.kalibyte.foundry.qa.inspection.mapper.QaInspectionMapper;
import com.kalibyte.foundry.qa.inspection.service.QaInspectionService;
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
@RequestMapping("/api/qa/inspections")
@RequiredArgsConstructor
public class QaInspectionController {

    private final QaInspectionService service;
    private final QaInspectionMapper mapper;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY')")
    public ResponseEntity<ApiResponse<QaInspectionResponse>> createDraft(@Valid @RequestBody QaInspectionRequest request) {
        QaInspection entity = mapper.toEntity(request);
        if (request.getFindings() != null) {
            request.getFindings().forEach(f -> entity.addFinding(mapper.toFindingEntity(f)));
        }
        return ResponseEntity.ok(ApiResponse.success(service.createDraft(entity)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY', 'PRODUCTION')")
    public ResponseEntity<ApiResponse<QaInspectionResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY', 'PRODUCTION')")
    public ResponseEntity<ApiResponse<List<QaInspectionResponse>>> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) UUID orderId,
            @RequestParam(required = false) UUID productionEntryId,
            @RequestParam(required = false) InspectionStage inspectionStage,
            @RequestParam(required = false) InspectionResult result,
            @RequestParam(required = false) InspectionStatus status
    ) {
        List<QaInspectionResponse> list = service.list(startDate, endDate, orderId, productionEntryId, inspectionStage, result, status);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY')")
    public ResponseEntity<ApiResponse<QaInspectionResponse>> updateDraft(@PathVariable Long id, @Valid @RequestBody QaInspectionRequest request) {
        QaInspection entity = mapper.toEntity(request);
        if (request.getFindings() != null) {
            request.getFindings().forEach(f -> entity.addFinding(mapper.toFindingEntity(f)));
        }
        return ResponseEntity.ok(ApiResponse.success(service.updateDraft(id, entity)));
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY')")
    public ResponseEntity<ApiResponse<QaInspectionResponse>> complete(@PathVariable Long id, @RequestParam String performedBy) {
        return ResponseEntity.ok(ApiResponse.success(service.completeInspection(id, performedBy)));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY')")
    public ResponseEntity<ApiResponse<QaInspectionResponse>> cancel(@PathVariable Long id, @RequestParam String performedBy) {
        return ResponseEntity.ok(ApiResponse.success(service.cancelInspection(id, performedBy)));
    }

    @GetMapping("/by-production/{productionEntryId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY', 'PRODUCTION')")
    public ResponseEntity<ApiResponse<List<QaInspectionResponse>>> getByProduction(@PathVariable UUID productionEntryId) {
        List<QaInspectionResponse> list = service.list(null, null, null, productionEntryId, null, null, null);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/by-order/{orderId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY', 'PRODUCTION')")
    public ResponseEntity<ApiResponse<List<QaInspectionResponse>>> getByOrder(@PathVariable UUID orderId) {
        List<QaInspectionResponse> list = service.list(null, null, orderId, null, null, null, null);
        return ResponseEntity.ok(ApiResponse.success(list));
    }
}
