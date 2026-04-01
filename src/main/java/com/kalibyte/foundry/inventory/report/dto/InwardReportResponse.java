package com.kalibyte.foundry.inventory.report.dto;

import com.kalibyte.foundry.inventory.inward.entity.enums.ReceiptStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record InwardReportResponse(
    BigDecimal totalQuantity,
    BigDecimal totalValue,
    long totalInwardCount,
    List<InwardDocumentDetail> records
) {
    public record InwardDocumentDetail(
        String inwardNumber,
        LocalDate inwardDate,
        String vendorName,
        String poNumber,
        BigDecimal totalValue,
        List<InwardItemDetail> items
    ) {}

    public record InwardItemDetail(
        String itemCode,
        String itemName,
        BigDecimal quantity,
        String unit,
        BigDecimal unitRate,
        BigDecimal totalValue,
        ReceiptStatus status
    ) {}
}
