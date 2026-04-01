package com.kalibyte.foundry.inventory.inward.dto.response;

import java.math.BigDecimal;

public record ReceivedItemDetail(
    Long id,
    Long itemId,
    String itemName,
    String itemCode,
    String unit,
    BigDecimal poQuantity,
    BigDecimal receivedQuantity,
    BigDecimal unitRate,
    BigDecimal taxableAmount,
    BigDecimal gstRate,
    BigDecimal taxAmount,
    BigDecimal grandTotal,
    String notes
) {}
