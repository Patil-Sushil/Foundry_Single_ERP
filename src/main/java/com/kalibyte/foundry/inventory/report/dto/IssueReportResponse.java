package com.kalibyte.foundry.inventory.report.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record IssueReportResponse(
    BigDecimal totalQuantity,
    BigDecimal totalValue,
    long totalIssueCount,
    List<IssueDocumentDetail> records
) {
    public record IssueDocumentDetail(
        String issueNumber,
        LocalDate issueDate,
        String departmentName,
        BigDecimal totalValue,
        List<IssueItemDetail> items
    ) {}

    public record IssueItemDetail(
        String itemCode,
        String itemName,
        BigDecimal quantity,
        String unit,
        BigDecimal unitRate,
        BigDecimal totalValue
    ) {}
}
