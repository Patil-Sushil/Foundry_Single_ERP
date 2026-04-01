package com.kalibyte.foundry.inventory.inward.dto.response;

import java.math.BigDecimal;

public record ReceivedItemComparison(
    Long id,
    String itemName,
    String itemCode,
    String unit,
    BigDecimal orderedQuantity,
    BigDecimal receivedQuantity,
    BigDecimal quantityDifference,
    String receiptStatus,
    BigDecimal unitRate,
    BigDecimal taxableAmount,
    BigDecimal gstRate,
    BigDecimal taxAmount,
    BigDecimal grandTotal
) {}
