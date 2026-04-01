package com.kalibyte.foundry.inventory.item.dto.response;

import java.math.BigDecimal;

public record ItemSummary(
    Long id,
    String name,
    String code,
    String unit,
    Boolean isScrap,
    BigDecimal currentStock,
    BigDecimal avgRate
) {}
