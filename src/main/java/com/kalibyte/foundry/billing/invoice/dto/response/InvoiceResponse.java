package com.kalibyte.foundry.billing.invoice.dto.response;

import com.kalibyte.foundry.billing.invoice.entity.enums.InvoiceStatus;
import com.kalibyte.foundry.order.entity.enums.GstType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceResponse {

    private UUID id;

    private String invoiceNumber;

    private UUID orderId;

    private String vehicleNumber;

    private BigDecimal subtotal;

    // GST breakdown
    private GstType gstType;
    private BigDecimal gstPercentage;
    private BigDecimal cgst;
    private BigDecimal sgst;
    private BigDecimal igst;
    private BigDecimal totalGst;

    private BigDecimal totalAmount;

    private LocalDate invoiceDate;

    private LocalDate dueDate;

    private InvoiceStatus billStatus;

    private List<InvoiceItemResponse> items;
}