package com.kalibyte.foundry.inventory.report.dto;

import com.kalibyte.foundry.inventory.item.entity.enums.ItemCategory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ItemLedgerReport(
    String itemCode,
    String itemName,
    ItemCategory category,
    String unit,
    BigDecimal currentStock,
    BigDecimal avgRate,
    BigDecimal openingStock,
    BigDecimal closingStock,
    BigDecimal totalInwardQty,
    BigDecimal totalInwardValue,
    BigDecimal totalIssueQty,
    BigDecimal totalIssueValue,
    BigDecimal netMovement,
    List<ItemLedgerTransaction> transactions
) {
    public record ItemLedgerTransaction(
        LocalDate date,
        String type, // INWARD / ISSUE
        String documentNumber,
        String reference, // Vendor or Department
        BigDecimal quantityIn,
        BigDecimal quantityOut,
        BigDecimal rate,
        BigDecimal runningBalance
    ) {}
}
