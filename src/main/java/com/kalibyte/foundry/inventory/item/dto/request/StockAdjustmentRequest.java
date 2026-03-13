package com.kalibyte.foundry.inventory.item.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Request DTO for Stock Adjustment.
 * This allows inventory managers to manually add or subtract stock.
 */
public record StockAdjustmentRequest(
    @NotNull(message = "Quantity is required")
    BigDecimal quantity,

    @Positive(message = "Unit rate must be positive")
    BigDecimal unitRate,

    @NotBlank(message = "Reason is required")
    @Size(max = 255, message = "Reason cannot exceed 255 characters")
    String reason
) implements Serializable {

    @AssertTrue(message = "Quantity cannot be zero")
    private boolean isQuantityValid() {
        return quantity != null && quantity.compareTo(BigDecimal.ZERO) != 0;
    }

    @AssertTrue(message = "Unit rate is required for positive stock adjustments (inwards)")
    private boolean isUnitRateProvidedForInward() {
        if (quantity != null && quantity.compareTo(BigDecimal.ZERO) > 0) {
            return unitRate != null;
        }
        return true;
    }
}
