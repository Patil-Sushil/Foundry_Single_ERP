package com.kalibyte.foundry.inventory.purchaseinvoice.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PurchaseInvoiceResponse(
    Long id,
    String vendorInvoiceNumber,
    LocalDate vendorInvoiceDate,
    BigDecimal invoiceAmount,
    Long vendorId,
    String vendorName,
    String vendorGstin,
    Long purchaseOrderId,
    String poNumber,
    Long materialInwardId,
    String inwardNumber,
    String source,
    Boolean isVerified,
    LocalDateTime verifiedAt,
    BigDecimal inwardAmount,
    BigDecimal amountMismatch,
    Boolean hasAmountMismatch,
    String remarks,
    LocalDateTime createdAt
) {}
