package com.kalibyte.foundry.inventory.purchaseinvoice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreatePurchaseInvoiceRequest(
    @NotBlank(message = "Vendor invoice number is required")
    String vendorInvoiceNumber,
    @NotNull(message = "Vendor invoice date is required")
    LocalDate vendorInvoiceDate,
    BigDecimal invoiceAmount,
    @NotNull(message = "Vendor ID is required")
    Long vendorId,
    Long purchaseOrderId,
    Long materialInwardId,
    String remarks
) {}
