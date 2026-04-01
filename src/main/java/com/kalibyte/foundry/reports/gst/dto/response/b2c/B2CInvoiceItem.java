package com.kalibyte.foundry.reports.gst.dto.response.b2c;

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
public class B2CInvoiceItem {

    private UUID invoiceId;
    private String invoiceNumber;
    private LocalDate invoiceDate;

    private String customerName;
    private String placeOfSupply;

    private GstType gstType;
    private BigDecimal taxableValue;
    private BigDecimal gstRate;
    private BigDecimal cgstAmount;
    private BigDecimal sgstAmount;
    private BigDecimal igstAmount;
    private BigDecimal totalGst;
    private BigDecimal invoiceValue;
}