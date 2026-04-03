package com.kalibyte.foundry.inventory.purchaseinvoice.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdatePurchaseInvoiceRequest(
    String vendorInvoiceNumber,
    LocalDate vendorInvoiceDate,
    BigDecimal invoiceAmount,
    Long materialInwardId,
    String remarks
) {}
