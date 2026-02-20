package com.kalibyte.foundry.inventory.issue.dto.response;

import java.math.BigDecimal;

public record ConsumptionDetail(
    String itemName,
    String itemCode,
    String unit,
    BigDecimal totalQuantity,
    BigDecimal totalValue
) {}
