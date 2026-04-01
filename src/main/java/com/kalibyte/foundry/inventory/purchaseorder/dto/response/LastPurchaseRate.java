package com.kalibyte.foundry.inventory.purchaseorder.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LastPurchaseRate(
    Long itemId,
    Long vendorId,
    BigDecimal rate,
    LocalDate lastPurchasedOn
) {}
