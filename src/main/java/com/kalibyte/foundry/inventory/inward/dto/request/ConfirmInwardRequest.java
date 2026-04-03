package com.kalibyte.foundry.inventory.inward.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ConfirmInwardRequest(
    String vendorInvoiceNumber,
    LocalDate vendorInvoiceDate,
    BigDecimal vendorInvoiceAmount,
    String remarks
) {
    public boolean hasInvoiceDetails() {
        return vendorInvoiceNumber != null && !vendorInvoiceNumber.isBlank();
    }
}
