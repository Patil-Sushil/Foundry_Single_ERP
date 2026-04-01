package com.kalibyte.foundry.inventory.report.dto;

import java.math.BigDecimal;
import java.util.List;

public record VendorSummaryReport(
    List<VendorSummaryDetail> vendors
) {
    public record VendorSummaryDetail(
        Long vendorId,
        String vendorName,
        String contactNumber,
        long totalPOsRaised,
        BigDecimal totalPOValue,
        long totalInwardsReceived,
        BigDecimal totalInwardValue,
        BigDecimal pendingPOValue,
        BigDecimal ledgerBalance,
        List<SuppliedItemDetail> suppliedItems // Only for detailed view
    ) {}

    public record SuppliedItemDetail(
        String itemCode,
        String itemName,
        BigDecimal totalQuantity,
        BigDecimal avgRate
    ) {}
}
