package com.kalibyte.foundry.reports.gst.dto.response.b2b;

import com.kalibyte.foundry.order.entity.enums.GstType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class B2BInvoiceItem {

    private UUID invoiceId;
    private String invoiceNumber;
    private LocalDate invoiceDate;
    private BigDecimal invoiceValue;

    // Customer / Receiver
    private String customerName;
    private String gstin;
    private String placeOfSupply;

    // GST Details
    private GstType gstType;
    private BigDecimal taxableValue;
    private BigDecimal gstRate;
    private BigDecimal cgstAmount;
    private BigDecimal sgstAmount;
    private BigDecimal igstAmount;
    private BigDecimal totalGst;

    // Reverse Charge
    @Builder.Default
    private String reverseCharge = "N";
}