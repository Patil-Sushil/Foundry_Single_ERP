package com.kalibyte.foundry.inventory.issue.dto.response;

import java.math.BigDecimal;

public record IssuedItemDetail(
    Long id,
    String itemName,
    String itemCode,
    String unit,
    BigDecimal issuedQuantity,
    BigDecimal unitRate,
    BigDecimal amount,
    String notes
) {}
