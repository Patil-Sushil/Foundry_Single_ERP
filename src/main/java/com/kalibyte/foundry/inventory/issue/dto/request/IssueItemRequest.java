package com.kalibyte.foundry.inventory.issue.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

public record IssueItemRequest(
    @NotNull(message = "Item ID is required")
    Long itemId,

    @NotNull(message = "Issued Quantity is required")
    @DecimalMin(value = "0.001", message = "Quantity must be > 0")
    BigDecimal issuedQuantity,

    String notes
) implements Serializable {}
