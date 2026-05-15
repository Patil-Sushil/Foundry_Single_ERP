package com.kalibyte.foundry.qa.defect.controller;

import com.kalibyte.foundry.common.response.ApiResponse;
import com.kalibyte.foundry.qa.common.enums.DefectCategory;
import com.kalibyte.foundry.qa.common.enums.Severity;
import com.kalibyte.foundry.qa.defect.dto.DefectCatalogRequest;
import com.kalibyte.foundry.qa.defect.dto.DefectCatalogResponse;
import com.kalibyte.foundry.qa.defect.entity.DefectCatalog;
import com.kalibyte.foundry.qa.defect.mapper.DefectCatalogMapper;
import com.kalibyte.foundry.qa.defect.service.DefectCatalogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/qa/defects")
@RequiredArgsConstructor
public class DefectCatalogController {

    private final DefectCatalogService service;
    private final DefectCatalogMapper mapper;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY', 'PRODUCTION')")
    public ResponseEntity<ApiResponse<List<DefectCatalogResponse>>> list(
            @RequestParam(required = false) DefectCategory category,
            @RequestParam(required = false) Severity severity,
            @RequestParam(required = false) Boolean isActive
    ) {
        List<DefectCatalog> list = service.list(category, severity, isActive);
        return ResponseEntity.ok(ApiResponse.success(mapper.toResponseList(list)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY', 'PRODUCTION')")
    public ResponseEntity<ApiResponse<DefectCatalogResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(mapper.toResponse(service.getById(id))));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY')")
    public ResponseEntity<ApiResponse<DefectCatalogResponse>> create(@Valid @RequestBody DefectCatalogRequest request) {
        DefectCatalog defect = mapper.toEntity(request);
        return ResponseEntity.ok(ApiResponse.success(mapper.toResponse(service.create(defect))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY')")
    public ResponseEntity<ApiResponse<DefectCatalogResponse>> update(@PathVariable Long id, @Valid @RequestBody DefectCatalogRequest request) {
        DefectCatalog defect = mapper.toEntity(request);
        return ResponseEntity.ok(ApiResponse.success(mapper.toResponse(service.update(id, defect))));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY')")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        service.deactivate(id);
        return ResponseEntity.ok(ApiResponse.success("Defect deactivated successfully", null));
    }
}
