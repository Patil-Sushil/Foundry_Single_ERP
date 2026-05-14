package com.kalibyte.foundry.common.castingprocess.controller;

import com.kalibyte.foundry.common.response.ApiResponse;
import com.kalibyte.foundry.common.castingprocess.dto.CastingProcessRequest;
import com.kalibyte.foundry.common.castingprocess.dto.CastingProcessResponse;
import com.kalibyte.foundry.common.castingprocess.service.CastingProcessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/casting-processes")
@RequiredArgsConstructor
public class CastingProcessController {

    private final CastingProcessService service;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CastingProcessResponse> create(@Valid @RequestBody CastingProcessRequest request) {
        return ApiResponse.success("Casting process created successfully", service.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CastingProcessResponse> update(@PathVariable UUID id, @Valid @RequestBody CastingProcessRequest request) {
        return ApiResponse.success("Casting process updated successfully", service.update(id, request));
    }

    @GetMapping("/{id}")
    public ApiResponse<CastingProcessResponse> get(@PathVariable UUID id) {
        return ApiResponse.success(service.get(id));
    }

    @GetMapping
    public ApiResponse<List<CastingProcessResponse>> getAll() {
        return ApiResponse.success(service.getAll());
    }

    @GetMapping("/active")
    public ApiResponse<List<CastingProcessResponse>> getAllActive() {
        return ApiResponse.success(service.getAllActive());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ApiResponse.success("Casting process deactivated successfully", null);
    }
}
