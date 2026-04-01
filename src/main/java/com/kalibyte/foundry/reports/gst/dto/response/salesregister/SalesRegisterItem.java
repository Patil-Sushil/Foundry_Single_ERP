package com.kalibyte.foundry.reports.gst.dto.response.salesregister;

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
public class SalesRegisterItem {

    private UUID invoiceId;
    private String invoiceNumber;
    private LocalDate invoiceDate;
    private LocalDate dueDate;

    // Customer
    private String customerName;
    private String companyName;
    private String gstin;
    private String state;
    private String placeOfSupply;

    // Order
    private String orderNumber;
    private String orderType;

    // Amount
    private BigDecimal taxableValue;
    private GstType gstType;
    private BigDecimal gstRate;
    private BigDecimal cgstAmount;
    private BigDecimal sgstAmount;
    private BigDecimal igstAmount;
    private BigDecimal totalGst;
    private BigDecimal invoiceValue;

    // Status
    private String invoiceStatus;
    private String paymentStatus;
}
