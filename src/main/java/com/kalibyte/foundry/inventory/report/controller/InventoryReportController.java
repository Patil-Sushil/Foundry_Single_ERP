package com.kalibyte.foundry.inventory.report.controller;

import com.kalibyte.foundry.common.response.ApiResponse;
import com.kalibyte.foundry.inventory.report.dto.*;
import com.kalibyte.foundry.inventory.report.service.InventoryReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/inventory/reports")
@RequiredArgsConstructor
@Tag(name = "Inventory Reports", description = "Endpoints for inventory analytics and reports")
public class InventoryReportController {

    private final InventoryReportService reportService;

    @GetMapping("/inwards")
    @Operation(summary = "Material Inward Report")
    public ApiResponse<InwardReportResponse> getInwardReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long vendorId,
            @RequestParam(required = false) Long itemId,
            @RequestParam(required = false) Long purchaseOrderId
    ) {
        LocalDate start = startDate != null ? startDate : LocalDate.now().withDayOfMonth(1);
        LocalDate end = endDate != null ? endDate : LocalDate.now();
        return ApiResponse.success(reportService.getInwardReport(start, end, vendorId, itemId, purchaseOrderId));
    }

    @GetMapping("/issues")
    @Operation(summary = "Material Outward (Issue) Report")
    public ApiResponse<IssueReportResponse> getIssueReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long itemId
    ) {
        LocalDate start = startDate != null ? startDate : LocalDate.now().withDayOfMonth(1);
        LocalDate end = endDate != null ? endDate : LocalDate.now();
        return ApiResponse.success(reportService.getIssueReport(start, end, departmentId, itemId));
    }

    @GetMapping("/items/{itemId}/ledger")
    @Operation(summary = "Item-wise Inward/Outward Report (Item Ledger)")
    public ApiResponse<ItemLedgerReport> getItemLedgerReport(
            @PathVariable Long itemId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        LocalDate start = startDate != null ? startDate : LocalDate.now().minusMonths(1);
        LocalDate end = endDate != null ? endDate : LocalDate.now();
        return ApiResponse.success(reportService.getItemLedgerReport(itemId, start, end));
    }

    @GetMapping("/daily-movement")
    @Operation(summary = "Daily Stock Movement Report")
    public ApiResponse<DailyMovementReport> getDailyMovementReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String category
    ) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        return ApiResponse.success(reportService.getDailyMovementReport(targetDate, category));
    }

    @GetMapping("/stock-summary")
    @Operation(summary = "Stock Summary Report")
    public ApiResponse<StockSummaryReport> getStockSummaryReport(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean belowReorderLevel,
            @RequestParam(required = false) Long departmentId
    ) {
        return ApiResponse.success(reportService.getStockSummaryReport(category, belowReorderLevel, departmentId));
    }

    @GetMapping("/vendor-summary")
    @Operation(summary = "Vendor-wise Purchase & Inward Summary")
    public ApiResponse<VendorSummaryReport> getVendorSummaryReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long vendorId
    ) {
        LocalDate start = startDate != null ? startDate : LocalDate.now().withDayOfMonth(1);
        LocalDate end = endDate != null ? endDate : LocalDate.now();
        return ApiResponse.success(reportService.getVendorSummaryReport(start, end, vendorId));
    }
}
