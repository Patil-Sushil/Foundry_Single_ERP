package com.kalibyte.foundry.labors.labor.controller;

import com.kalibyte.foundry.common.response.ApiResponse;
import com.kalibyte.foundry.labors.labor.dto.LaborerRequest;
import com.kalibyte.foundry.labors.labor.dto.LaborerResponse;
import com.kalibyte.foundry.labors.labor.service.LaborerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/labors")
@RequiredArgsConstructor
@Tag(name = "Laborer Management", description = "APIs for managing laborers and their profiles")
@SecurityRequirement(name = "bearerAuth")
public class LaborerController {

    private final LaborerService laborerService;

    @PostMapping
    @Operation(summary = "Create a new laborer", description = "Only accessible by ADMIN")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<LaborerResponse>> createLaborer(@Valid @RequestBody LaborerRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Laborer created successfully", laborerService.createLaborer(request)));
    }

    @GetMapping
    @Operation(summary = "Get all laborers", description = "Only accessible by ADMIN")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<LaborerResponse>>> getAllLaborers() {
        return ResponseEntity.ok(ApiResponse.success(laborerService.getAllLaborers()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get laborer by ID", description = "Only accessible by ADMIN")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<LaborerResponse>> getLaborerById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(laborerService.getLaborerById(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update laborer by ID", description = "Only accessible by ADMIN")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<LaborerResponse>> updateLaborer(@PathVariable Long id, @Valid @RequestBody LaborerRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Laborer updated successfully", laborerService.updateLaborer(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate a labor", description = "Only accessible by ADMIN")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<LaborerResponse>> deactivateLaborer(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Deactivated the labor",laborerService.deleteLaborer(id)));
    }

}
