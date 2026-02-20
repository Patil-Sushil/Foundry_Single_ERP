package com.kalibyte.foundry.inventory.purchaseorder.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

public record OrderItemRequest(
    @NotNull(message = "Item ID is required")
    Long itemId,

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.001", message = "Quantity must be > 0")
    BigDecimal quantity,

    @NotNull(message = "Unit rate is required")
    @DecimalMin(value = "0.01", message = "Unit rate must be > 0")
    BigDecimal unitRate,
    
    String notes
) implements Serializable {}
