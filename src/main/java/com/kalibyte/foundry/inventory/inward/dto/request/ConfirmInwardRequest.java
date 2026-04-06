package com.kalibyte.foundry.inventory.inward.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ConfirmInwardRequest(
    @NotBlank
    String vendorInvoiceNumber,
    @NotBlank
    LocalDate vendorInvoiceDate,
    BigDecimal vendorInvoiceAmount,
    String remarks
) {
    public boolean hasInvoiceDetails() {
        return vendorInvoiceNumber != null && !vendorInvoiceNumber.isBlank();
    }
}
