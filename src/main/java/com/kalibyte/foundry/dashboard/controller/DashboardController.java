package com.kalibyte.foundry.dashboard.controller;

import com.kalibyte.foundry.common.response.ApiResponse;
import com.kalibyte.foundry.dashboard.dto.request.DateRangePreset;
import com.kalibyte.foundry.dashboard.dto.request.DateRangeRequest;
import com.kalibyte.foundry.dashboard.dto.response.*;
import com.kalibyte.foundry.dashboard.service.DashboardService;
import com.kalibyte.foundry.dashboard.util.DateRangeResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Dashboard KPIs and business insights")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @Operation(summary = "Get dashboard summary for a date range")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getSummary(
            @RequestParam(required = false) DateRangePreset preset,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        DateRangeResolver.DateRange range = resolveRange(preset, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getSummary(range)));
    }

    @GetMapping("/sales")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    @Operation(summary = "Get detailed sales and revenue insights for a date range")
    public ResponseEntity<ApiResponse<SalesInsights>> getSalesInsights(
            @RequestParam(required = false) DateRangePreset preset,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        DateRangeResolver.DateRange range = resolveRange(preset, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getSalesInsights(range)));
    }

    @GetMapping("/production")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION')")
    @Operation(summary = "Get production floor and furnace KPIs for a date range")
    public ResponseEntity<ApiResponse<ProductionKPIs>> getProductionKPIs(
            @RequestParam(required = false) DateRangePreset preset,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        DateRangeResolver.DateRange range = resolveRange(preset, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getProductionKPIs(range)));
    }

    @GetMapping("/finance")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @Operation(summary = "Get financial health metrics for a date range")
    public ResponseEntity<ApiResponse<FinancialHealth>> getFinancialHealth(
            @RequestParam(required = false) DateRangePreset preset,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        DateRangeResolver.DateRange range = resolveRange(preset, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getFinancialHealth(range)));
    }

    @GetMapping("/inventory")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE')")
    @Operation(summary = "Get inventory alerts and supply chain data for a date range")
    public ResponseEntity<ApiResponse<InventoryAlerts>> getInventoryAlerts(
            @RequestParam(required = false) DateRangePreset preset,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        DateRangeResolver.DateRange range = resolveRange(preset, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getInventoryAlerts(range)));
    }

    private DateRangeResolver.DateRange resolveRange(DateRangePreset preset, LocalDate start, LocalDate end) {
        DateRangeRequest request = DateRangeRequest.builder()
                .preset(preset)
                .startDate(start)
                .endDate(end)
                .build();
        return DateRangeResolver.resolve(request);
    }
}
