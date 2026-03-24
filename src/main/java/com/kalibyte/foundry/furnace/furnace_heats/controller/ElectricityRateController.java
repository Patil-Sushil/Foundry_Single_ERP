package com.kalibyte.foundry.furnace.furnace_heats.controller;

import com.kalibyte.foundry.common.response.ApiResponse;
import com.kalibyte.foundry.furnace.furnace_heats.dto.request.ElectricityRateRequest;
import com.kalibyte.foundry.furnace.furnace_heats.entity.ElectricityRate;
import com.kalibyte.foundry.furnace.furnace_heats.service.ElectricityRateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/electricity-rate")
@RequiredArgsConstructor
@Tag(name = "Electricity Rate", description = "Endpoints for managing furnace electricity rates")
public class ElectricityRateController {

    private final ElectricityRateService electricityRateService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update electricity rate", description = "Deactivates the current rate and creates a new active version. Requires ADMIN role.")
    public ResponseEntity<ApiResponse<ElectricityRate>> updateRate(@Valid @RequestBody ElectricityRateRequest request) {
        ElectricityRate newRate = electricityRateService.updateRate(request.getRatePerUnit());
        return ResponseEntity.ok(ApiResponse.success("Electricity rate updated successfully", newRate));
    }

    @GetMapping("/current")
    @Operation(summary = "Get current active rate", description = "Fetches the currently active electricity rate per unit used for new heats.")
    public ResponseEntity<ApiResponse<Double>> getCurrentRate() {
        return ResponseEntity.ok(ApiResponse.success("Current electricity rate fetched", electricityRateService.getCurrentRate()));
    }
}
