package com.kalibyte.foundry.furnace.furnace_report.controller;

import com.kalibyte.foundry.common.response.ApiResponse;
import com.kalibyte.foundry.furnace.furnace_heats.entity.Enum.HeatMaterialType;
import com.kalibyte.foundry.furnace.furnace_report.dto.response.FurnaceResponse;
import com.kalibyte.foundry.furnace.furnace_report.dto.Request.FurnaceRequest;
import com.kalibyte.foundry.furnace.furnace_report.service.FurnaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/furnace/reports")
@RequiredArgsConstructor
public class FurnaceController {

    private final FurnaceService furnaceService;

	@PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','PRODUCTION')")
    public ResponseEntity<ApiResponse<FurnaceResponse>> createFurnace(@Valid @RequestBody FurnaceRequest request) {
        FurnaceResponse response = furnaceService.createFurnace(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Furnace report created successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','PRODUCTION')")
    public ResponseEntity<ApiResponse<List<FurnaceResponse>>> findAll() {
        List<FurnaceResponse> responses = furnaceService.findAll();
        return ResponseEntity.ok(new ApiResponse<>(true, "Furnace reports fetched successfully", responses));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','PRODUCTION')")
    public ResponseEntity<ApiResponse<FurnaceResponse>> findById(@PathVariable long id) {
        FurnaceResponse response = furnaceService.findById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Furnace report fetched successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','PRODUCTION')")
    public ResponseEntity<ApiResponse<FurnaceResponse>> updateFurnace(@PathVariable Long id, @Valid @RequestBody FurnaceRequest request) {
        FurnaceResponse response = furnaceService.updateFurnace(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Furnace report updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','PRODUCTION')")
    public ResponseEntity<ApiResponse<Void>> deleteFurnace(@PathVariable Long id) {
        furnaceService.deleteFurnace(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Furnace report deleted successfully", null));
    }

    @GetMapping("/{id}/material-summary")
    @PreAuthorize("hasAnyRole('ADMIN','PRODUCTION')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getMaterialSummary(
            @PathVariable Long id,
            @RequestParam(required = false) HeatMaterialType type) {
        List<Map<String, Object>> summary = furnaceService.getMaterialSummary(id, type);
        return ResponseEntity.ok(new ApiResponse<>(true, "Material summary fetched successfully", summary));
    }

    @GetMapping("/ref/{refNo}")
    @PreAuthorize("hasAnyRole('ADMIN','PRODUCTION')")
    public ResponseEntity<ApiResponse<FurnaceResponse>> findByRefNo(@PathVariable String refNo) {
        FurnaceResponse response = furnaceService.findByFurnaceRefNo(refNo);
        return ResponseEntity.ok(new ApiResponse<>(true, "Furnace report fetched successfully", response));
    }
}
