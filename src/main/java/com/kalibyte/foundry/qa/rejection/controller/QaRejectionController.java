package com.kalibyte.foundry.qa.rejection.controller;

import com.kalibyte.foundry.common.response.ApiResponse;
import com.kalibyte.foundry.qa.common.enums.RejectionDisposition;
import com.kalibyte.foundry.qa.common.enums.RejectionStatus;
import com.kalibyte.foundry.qa.rejection.dto.QaRejectionResponse;
import com.kalibyte.foundry.qa.rejection.service.QaRejectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/qa/rejections")
@RequiredArgsConstructor
public class QaRejectionController {

    private final QaRejectionService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY', 'PRODUCTION')")
    public ResponseEntity<ApiResponse<List<QaRejectionResponse>>> list(
            @RequestParam(required = false) UUID orderId,
            @RequestParam(required = false) RejectionStatus status,
            @RequestParam(required = false) RejectionDisposition disposition
    ) {
        return ResponseEntity.ok(ApiResponse.success(service.list(orderId, status, disposition)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY', 'PRODUCTION')")
    public ResponseEntity<ApiResponse<QaRejectionResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    @PatchMapping("/{id}/disposition")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY')")
    public ResponseEntity<ApiResponse<QaRejectionResponse>> disposition(
            @PathVariable Long id,
            @RequestParam RejectionDisposition disposition,
            @RequestParam(required = false) String remarks,
            @RequestParam String performedBy
    ) {
        return ResponseEntity.ok(ApiResponse.success(service.dispositionRejection(id, disposition, remarks, performedBy)));
    }

    @GetMapping("/by-order/{orderId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY', 'PRODUCTION')")
    public ResponseEntity<ApiResponse<List<QaRejectionResponse>>> getByOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok(ApiResponse.success(service.list(orderId, null, null)));
    }
}
