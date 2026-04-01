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
    BigDecimal totalTaxableAmount,
    BigDecimal totalTaxAmount,
    BigDecimal cgst,
    BigDecimal sgst,
    BigDecimal igst,
    com.kalibyte.foundry.order.entity.enums.GstType gstType,
    BigDecimal grandTotal,
    LocalDate poDate,
    LocalDate expectedDeliveryDate
) {}
