package com.kalibyte.foundry.inventory.inward.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateReceivedQuantityRequest(
    @NotNull(message = "Received Item ID is required")
    Long receivedItemId,

    @NotNull(message = "Received Quantity is required")
    @DecimalMin(value = "0.001", message = "Quantity must be > 0")
    BigDecimal receivedQuantity,

    @DecimalMin(value = "0.01", message = "Unit Rate must be > 0")
    BigDecimal unitRate,

    @NotBlank
    String vendorInvoiceNumber,
    @NotBlank
    LocalDate vendorInvoiceDate
) implements Serializable {}
