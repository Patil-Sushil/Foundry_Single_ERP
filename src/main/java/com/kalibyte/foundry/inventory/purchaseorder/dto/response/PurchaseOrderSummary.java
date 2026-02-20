package com.kalibyte.foundry.inventory.purchaseorder.dto.response;

import com.kalibyte.foundry.inventory.purchaseorder.entity.enums.POStatus;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PurchaseOrderSummary(
    Long id,
    String poNumber,
    String vendorName,
    POStatus status,
    int totalItems,
    BigDecimal totalOrderValue,
    LocalDate poDate,
    LocalDate expectedDeliveryDate
) {}
