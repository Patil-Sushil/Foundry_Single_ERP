package com.kalibyte.foundry.reports.gst.dto.response.b2b;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Gstr1B2BReport {

    private LocalDate periodFrom;
    private LocalDate periodTo;
    private String periodDescription;

    private int totalCustomers;
    private int totalInvoices;
    private BigDecimal totalTaxableValue;
    private BigDecimal totalCgst;
    private BigDecimal totalSgst;
    private BigDecimal totalIgst;
    private BigDecimal totalGst;
    private BigDecimal totalInvoiceValue;

    private List<B2BCustomerGroup> customerGroups;
}
