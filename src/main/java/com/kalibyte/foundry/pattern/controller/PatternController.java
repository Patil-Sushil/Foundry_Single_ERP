package com.kalibyte.foundry.pattern.controller;

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

    //  Create Pattern
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SALES')")
    public PatternResponse create(@Valid @RequestBody PatternCreateRequest request) {
        return patternService.create(request);
    }

    //  Update Pattern Details (not status)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public PatternResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody PatternUpdateRequest request) {
        return patternService.update(id, request);
    }

    //  Change Pattern Status
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','PRODUCTION')")
    public PatternResponse changeStatus(
            @PathVariable UUID id,
            @Valid @RequestBody PatternStatusUpdateRequest request) {
        return patternService.changeStatus(id, request);
    }

    //  Get All Patterns
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SALES','PRODUCTION')")
    public PageResponse<PatternResponse> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        return patternService.getAll(page, size, sort);
    }

    //  Get Pattern By ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SALES','PRODUCTION')")
    public PatternResponse getById(@PathVariable UUID id) {
        return patternService.getById(id);
    }
}