// src/main/java/com/kalibyte/foundry/reports/gst/dto/response/salesregister/SalesRegisterReport.java
package com.kalibyte.foundry.reports.gst.dto.response.salesregister;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesRegisterReport {

    private LocalDate periodFrom;
    private LocalDate periodTo;
    private String periodDescription;

    private int totalInvoices;
    private BigDecimal totalTaxableValue;
    private BigDecimal totalCgst;
    private BigDecimal totalSgst;
    private BigDecimal totalIgst;
    private BigDecimal totalGst;
    private BigDecimal totalInvoiceValue;

    private List<SalesRegisterItem> items;
}