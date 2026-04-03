package com.kalibyte.foundry.inventory.purchaseinvoice.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PurchaseInvoiceSummary(
    Long id,
    String vendorInvoiceNumber,
    LocalDate vendorInvoiceDate,
    BigDecimal invoiceAmount,
    String vendorName,
    String poNumber,
    String inwardNumber,
    String source,
    Boolean isVerified,
    Boolean hasAmountMismatch
) {}
