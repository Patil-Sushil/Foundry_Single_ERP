package com.kalibyte.foundry.production.controller;

import com.kalibyte.foundry.common.response.ApiResponse;
import com.kalibyte.foundry.production.dto.response.report.daily.DailyProductionReport;
import com.kalibyte.foundry.production.dto.response.report.monthly.MonthlyProductionReport;
import com.kalibyte.foundry.production.dto.response.report.orderwise.OrderProductionReport;
import com.kalibyte.foundry.production.dto.response.report.summary.ProductionDashboardSummary;
import com.kalibyte.foundry.production.service.ProductionReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports/production")
@RequiredArgsConstructor
public class ProductionReportController {

    private final ProductionReportService service;

    //------------------------------------------------
    // ORDER REPORT
    //------------------------------------------------

    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyRole('ADMIN','PRODUCTION')")
    public ApiResponse<OrderProductionReport> orderReport(@PathVariable UUID orderId) {
        return ApiResponse.success(service.getOrderReport(orderId));
    }

    //------------------------------------------------
    // DAILY
    //------------------------------------------------

    @GetMapping("/daily")
    @PreAuthorize("hasAnyRole('ADMIN','PRODUCTION')")
    public ApiResponse<DailyProductionReport> daily(
            @RequestParam LocalDate date
    ) {
        return ApiResponse.success(service.getDailyReport(date));
    }

    //------------------------------------------------
    // MONTHLY
    //------------------------------------------------

    @GetMapping("/monthly")
    @PreAuthorize("hasAnyRole('ADMIN','PRODUCTION')")
    public ApiResponse<MonthlyProductionReport> monthly(
            @RequestParam int month,
            @RequestParam int year
    ) {
        return ApiResponse.success(service.getMonthlyReport(month, year));
    }

    //------------------------------------------------
    // DASHBOARD
    //------------------------------------------------

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN','PRODUCTION')")
    public ApiResponse<ProductionDashboardSummary> dashboard() {
        return ApiResponse.success(service.getDashboardSummary());
    }
}
