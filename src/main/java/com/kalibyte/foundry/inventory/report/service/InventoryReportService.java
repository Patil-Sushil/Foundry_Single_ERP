package com.kalibyte.foundry.inventory.report.service;

import com.kalibyte.foundry.inventory.report.dto.*;

import java.time.LocalDate;

public interface InventoryReportService {
    InwardReportResponse getInwardReport(LocalDate start, LocalDate end, Long vendorId, Long itemId, Long poId);
    IssueReportResponse getIssueReport(LocalDate start, LocalDate end, Long departmentId, Long itemId);
    ItemLedgerReport getItemLedgerReport(Long itemId, LocalDate start, LocalDate end);
    DailyMovementReport getDailyMovementReport(LocalDate date, String category);
    StockSummaryReport getStockSummaryReport(String category, Boolean belowReorder, Long departmentId);
    VendorSummaryReport getVendorSummaryReport(LocalDate start, LocalDate end, Long vendorId);
}
