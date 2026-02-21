package com.kalibyte.foundry.pattern.controller;

import com.kalibyte.foundry.common.response.PageResponse;
import com.kalibyte.foundry.pattern.dto.request.PatternCreateRequest;
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

    // create new Patterns
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SALES')")
    public PatternResponse create(@Valid @RequestBody PatternCreateRequest request) {
        return patternService.create(request);
    }

    // list all patterns with pagination and sorting
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SALES')")
    public PageResponse<PatternResponse> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sort
    ) {
        return patternService.getAll(page, size, sort);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SALES')")
    public PatternResponse getById(@PathVariable UUID id) {
        return patternService.getById(id);
    }

}
