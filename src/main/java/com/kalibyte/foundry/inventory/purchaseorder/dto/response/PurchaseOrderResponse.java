package com.kalibyte.foundry.inventory.purchaseorder.dto.response;

import com.kalibyte.foundry.inventory.purchaseorder.entity.enums.POStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record PurchaseOrderResponse(
    Long id,
    String poNumber,
    POStatus status,
    String vendorName,
    Long vendorId,
    List<OrderItemDetail> items,
    BigDecimal totalOrderValue,
    LocalDate poDate,
    LocalDate expectedDeliveryDate,
    String notes,
    LocalDateTime createdAt
) {}
