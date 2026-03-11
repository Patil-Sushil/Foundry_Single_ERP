package com.kalibyte.foundry.furnace.furnace_heats.controller;

import com.kalibyte.foundry.common.response.ApiResponse;
import com.kalibyte.foundry.furnace.furnace_heats.dto.FurnaceHeatRequest;
import com.kalibyte.foundry.furnace.furnace_heats.dto.FurnaceHeatResponse;
import com.kalibyte.foundry.furnace.furnace_heats.dto.HeatsByOrderResponse;
import com.kalibyte.foundry.furnace.furnace_heats.service.FurnaceHeatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/furnace")

public class FurnaceHeatController {

    private final FurnaceHeatService furnaceHeatService;

	public FurnaceHeatController(FurnaceHeatService furnaceHeatService) {
		this.furnaceHeatService = furnaceHeatService;
	}

	@GetMapping("/heats/by-order/{orderId}")
    @PreAuthorize("hasAnyRole('ADMIN','PRODUCTION')")
    public ResponseEntity<ApiResponse<HeatsByOrderResponse>> getHeatsByOrder(
            @PathVariable UUID orderId) {
        HeatsByOrderResponse responses= furnaceHeatService.getHeatsByOrderId(orderId);
        return ResponseEntity.ok(new ApiResponse<>(true,"heats fetched by orders",responses));
    }

    @GetMapping("/reports/{reportId}/heats")
    @PreAuthorize("hasAnyRole('ADMIN','PRODUCTION')")
    public ResponseEntity<ApiResponse<List<FurnaceHeatResponse>>> getHeatsByReportId(@PathVariable Long reportId) {
        List<FurnaceHeatResponse> responses = furnaceHeatService.getHeatsByReportId(reportId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Heats fetched successfully", responses));
    }

    @GetMapping("/reports/{reportId}/heats/{heatId}")
    @PreAuthorize("hasAnyRole('ADMIN','PRODUCTION')")
    public ResponseEntity<ApiResponse<FurnaceHeatResponse>> getHeatById(@PathVariable Long reportId, @PathVariable Long heatId) {
        FurnaceHeatResponse response = furnaceHeatService.getHeatById(heatId);
        // Simple validation to ensure it belongs to the report could be added here if needed
        return ResponseEntity.ok(new ApiResponse<>(true, "Heat fetched successfully", response));
    }

    @PostMapping("/reports/{reportId}/heats")
    @PreAuthorize("hasAnyRole('ADMIN','PRODUCTION')")
    public ResponseEntity<ApiResponse<FurnaceHeatResponse>> createHeat(@PathVariable Long reportId, @Valid @RequestBody FurnaceHeatRequest request) {
        FurnaceHeatResponse response = furnaceHeatService.createHeat(reportId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Heat created successfully", response));
    }

    @PutMapping("/reports/{reportId}/heats/{heatId}")
    @PreAuthorize("hasAnyRole('ADMIN','PRODUCTION')")
    public ResponseEntity<ApiResponse<FurnaceHeatResponse>> updateHeatInReport(@PathVariable Long reportId, @PathVariable Long heatId, @Valid @RequestBody FurnaceHeatRequest request) {
        FurnaceHeatResponse response = furnaceHeatService.updateHeat(heatId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Heat updated successfully", response));
    }

    @PutMapping("/heats/{heatId}")
    @PreAuthorize("hasAnyRole('ADMIN','PRODUCTION')")
    public ResponseEntity<ApiResponse<FurnaceHeatResponse>> updateHeatDirect(@PathVariable Long heatId, @Valid @RequestBody FurnaceHeatRequest request) {
        FurnaceHeatResponse response = furnaceHeatService.updateHeat(heatId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Heat updated successfully", response));
    }

    @DeleteMapping("/reports/{reportId}/heats/{heatId}")
    @PreAuthorize("hasAnyRole('ADMIN','PRODUCTION')")
    public ResponseEntity<ApiResponse<Void>> deleteHeat(@PathVariable Long reportId, @PathVariable Long heatId) {
        furnaceHeatService.deleteHeat(heatId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Heat deleted successfully", null));
    }

    @DeleteMapping("/reports/{reportId}/heats")
    @PreAuthorize("hasAnyRole('ADMIN','PRODUCTION')")
    public ResponseEntity<ApiResponse<Void>> deleteAllHeatsByReportId(@PathVariable Long reportId) {
        furnaceHeatService.deleteAllHeatsByReportId(reportId);
        return ResponseEntity.ok(new ApiResponse<>(true, "All heats for report deleted successfully", null));
    }
}
