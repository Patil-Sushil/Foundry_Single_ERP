package com.kalibyte.foundry.pattern.controller;

import com.kalibyte.foundry.common.response.ApiResponse;
import com.kalibyte.foundry.common.response.PageResponse;
import com.kalibyte.foundry.pattern.dto.request.PatternCreateRequest;
import com.kalibyte.foundry.pattern.dto.request.PatternStatusUpdateRequest;
import com.kalibyte.foundry.pattern.dto.request.PatternUpdateRequest;
import com.kalibyte.foundry.pattern.dto.response.PatternResponse;
import com.kalibyte.foundry.pattern.service.PatternService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/patterns")
@RequiredArgsConstructor
public class PatternController {

    private final PatternService patternService;

    // Create Pattern
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SALES')")
    public ApiResponse<PatternResponse> create(@Valid @RequestBody PatternCreateRequest request) {
        PatternResponse response = patternService.create(request);
        return ApiResponse.success("Pattern created successfully", response);
    }

    // Update Pattern Details
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PatternResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody PatternUpdateRequest request) {

        PatternResponse response = patternService.update(id, request);
        return ApiResponse.success("Pattern updated successfully", response);
    }

    // Change Pattern Status
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','PRODUCTION')")
    public ApiResponse<PatternResponse> changeStatus(
            @PathVariable UUID id,
            @Valid @RequestBody PatternStatusUpdateRequest request) {

        PatternResponse response = patternService.changeStatus(id, request);
        return ApiResponse.success("Pattern status updated successfully", response);
    }

    // Get All Patterns
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SALES','PRODUCTION')")
    public ApiResponse<PageResponse<PatternResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {

        PageResponse<PatternResponse> response = patternService.getAll(page, size, sort);
        return ApiResponse.success(response);
    }

    // Get Pattern By ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SALES','PRODUCTION')")
    public ApiResponse<PatternResponse> getById(@PathVariable UUID id) {

        PatternResponse response = patternService.getById(id);
        return ApiResponse.success(response);
    }
}