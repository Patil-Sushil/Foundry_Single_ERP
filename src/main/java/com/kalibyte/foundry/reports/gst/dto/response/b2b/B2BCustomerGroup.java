package com.kalibyte.foundry.reports.gst.dto.response.b2b;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class B2BCustomerGroup {

    private String gstin;
    private String customerName;
    private int invoiceCount;
    private BigDecimal totalTaxableValue;
    private BigDecimal totalGst;
    private BigDecimal totalInvoiceValue;
    private List<B2BInvoiceItem> invoices;
}
