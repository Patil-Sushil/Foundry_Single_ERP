package com.kalibyte.foundry.inventory.purchaseorder.dto.response;

import com.kalibyte.foundry.inventory.item.entity.enums.ItemUnit;
import java.math.BigDecimal;

public record OrderItemDetail(
    Long id,
    Long itemId,
    String itemName,
    String itemCode,
    ItemUnit unit,
    BigDecimal orderedQuantity,
    BigDecimal receivedQuantity,
    BigDecimal pendingQuantity,
    BigDecimal unitRate,
    BigDecimal taxableValue,
    BigDecimal gstRate,
    String hsnCode,
    BigDecimal taxAmount,
    BigDecimal totalValue,
    String notes
) {}
