package com.kalibyte.foundry.inventory.report.dto;

import com.kalibyte.foundry.inventory.item.entity.enums.ItemCategory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DailyMovementReport(
    LocalDate date,
    List<DailyMovementItem> records
) {
    public record DailyMovementItem(
        String itemCode,
        String itemName,
        ItemCategory category,
        BigDecimal openingStock,
        BigDecimal totalInwardQty,
        BigDecimal totalInwardValue,
        BigDecimal totalIssueQty,
        BigDecimal totalIssueValue,
        BigDecimal closingStock,
        BigDecimal netMovement
    ) {}
}
