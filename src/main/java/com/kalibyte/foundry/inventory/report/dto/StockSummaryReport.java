package com.kalibyte.foundry.inventory.report.dto;

import com.kalibyte.foundry.inventory.item.entity.enums.ItemCategory;
import com.kalibyte.foundry.inventory.item.entity.enums.ItemSubCategory;
import com.kalibyte.foundry.inventory.item.entity.enums.StockStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record StockSummaryReport(
    BigDecimal totalStockValue,
    long lowStockCount,
    long criticalStockCount,
    List<StockSummaryItem> items
) {
    public record StockSummaryItem(
        Long itemId,
        String itemCode,
        String itemName,
        ItemCategory category,
        ItemSubCategory subCategory,
        String unit,
        BigDecimal currentStock,
        BigDecimal avgRate,
        BigDecimal stockValue,
        BigDecimal reorderLevel,
        BigDecimal minStockLevel,
        StockStatus status,
        BigDecimal lastPurchaseRate,
        LocalDate lastInwardDate,
        LocalDate lastIssueDate
    ) {}
}
