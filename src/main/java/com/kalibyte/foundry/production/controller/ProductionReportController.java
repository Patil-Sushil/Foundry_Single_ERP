package com.kalibyte.foundry.production.controller;

import com.kalibyte.foundry.common.response.ApiResponse;
import com.kalibyte.foundry.production.dto.response.report.daily.DailyProductionReport;
import com.kalibyte.foundry.production.dto.response.report.monthly.MonthlyProductionReport;
import com.kalibyte.foundry.production.dto.response.report.orderwise.OrderProductionReport;
import com.kalibyte.foundry.production.dto.response.report.summary.ProductionDashboardSummary;
import com.kalibyte.foundry.production.service.ProductionReportService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

import com.kalibyte.foundry.production.dto.response.report.orderwise.OrderItemProgress;
import com.kalibyte.foundry.production.dto.response.report.dashboard.WipDashboardResponse;
import com.kalibyte.foundry.production.dto.response.report.dashboard.DelayedOrderResponse;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/reports/production")
@RequiredArgsConstructor
@Validated
public class ProductionReportController {

    private final ProductionReportService service;

    // ── DASHBOARD ENHANCEMENTS ──────────────────────

    @GetMapping("/dashboard/order-progress")
    @PreAuthorize("hasAnyRole('ADMIN','PRODUCTION')")
    public ResponseEntity<ApiResponse<List<OrderItemProgress>>> orderProgressDashboard() {
        return ResponseEntity.ok(ApiResponse.success(service.getAllOrderProgress()));
    }

    @GetMapping("/dashboard/wip")
    @PreAuthorize("hasAnyRole('ADMIN','PRODUCTION')")
    public ResponseEntity<ApiResponse<WipDashboardResponse>> wipDashboard() {
        return ResponseEntity.ok(ApiResponse.success(service.getWipDashboard()));
    }

    @GetMapping("/dashboard/delayed")
    @PreAuthorize("hasAnyRole('ADMIN','PRODUCTION')")
    public ResponseEntity<ApiResponse<List<DelayedOrderResponse>>> delayedOrders() {
        return ResponseEntity.ok(ApiResponse.success(service.getDelayedOrders()));
    }

    // ── ORDER REPORT ────────────────────────────────

    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyRole('ADMIN','PRODUCTION')")
    public ResponseEntity<ApiResponse<OrderProductionReport>> orderReport(
            @PathVariable UUID orderId
    ) {
        return ResponseEntity.ok(ApiResponse.success(service.getOrderReport(orderId)));
    }

    // ── DAILY ───────────────────────────────────────

    @GetMapping("/daily")
    @PreAuthorize("hasAnyRole('ADMIN','PRODUCTION')")
    public ResponseEntity<ApiResponse<DailyProductionReport>> daily(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(ApiResponse.success(service.getDailyReport(date)));
    }

    // ── MONTHLY ─────────────────────────────────────

    @GetMapping("/monthly")
    @PreAuthorize("hasAnyRole('ADMIN','PRODUCTION')")
    public ResponseEntity<ApiResponse<MonthlyProductionReport>> monthly(
            @RequestParam @Min(1) @Max(12) int month,
            @RequestParam @Min(2000) int year
    ) {
        return ResponseEntity.ok(ApiResponse.success(service.getMonthlyReport(month, year)));
    }

    // ── DASHBOARD ───────────────────────────────────

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN','PRODUCTION')")
    public ResponseEntity<ApiResponse<ProductionDashboardSummary>> dashboard() {
        return ResponseEntity.ok(ApiResponse.success(service.getDashboardSummary()));
    }
}