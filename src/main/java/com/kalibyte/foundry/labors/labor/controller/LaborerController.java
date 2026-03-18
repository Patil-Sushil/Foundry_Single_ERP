package com.kalibyte.foundry.labors.labor.controller;

import com.kalibyte.foundry.common.response.ApiResponse;
import com.kalibyte.foundry.labors.labor.dto.LaborerRequestDTO;
import com.kalibyte.foundry.labors.labor.dto.LaborerResponseDTO;
import com.kalibyte.foundry.labors.labor.service.LaborerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<ApiResponse<LaborerResponseDTO>> createLaborer(@RequestBody LaborerRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success("Laborer created successfully", laborerService.createLaborer(request)));
    }

    @GetMapping
    @Operation(summary = "Get all laborers", description = "Only accessible by ADMIN")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<LaborerResponseDTO>>> getAllLaborers() {
        return ResponseEntity.ok(ApiResponse.success(laborerService.getAllLaborers()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get laborer by ID", description = "Only accessible by ADMIN")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<LaborerResponseDTO>> getLaborerById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(laborerService.getLaborerById(id)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate a labor", description = "Only accessible by ADMIN")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<LaborerResponseDTO>> deactivateLaborer(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Deactivated the labor",laborerService.deleteLaborer(id)));
    }

}
