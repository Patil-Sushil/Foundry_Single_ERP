package com.kalibyte.foundry.inventory.item.dto.response;

import com.kalibyte.foundry.inventory.item.entity.enums.ItemUnit;
import java.math.BigDecimal;

public record ItemSummary(
    Long id,
    String name,
    String code,
    ItemUnit unit,
    BigDecimal currentStock,
    BigDecimal avgRate
) {}
