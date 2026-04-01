// src/main/java/com/kalibyte/foundry/reports/gst/dto/response/b2c/Gstr1B2CReport.java
package com.kalibyte.foundry.reports.gst.dto.response.b2c;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Gstr1B2CReport {

    private LocalDate periodFrom;
    private LocalDate periodTo;
    private String periodDescription;

    private String type; // "B2C_LARGE" or "B2C_SMALL"

    private int totalInvoices;
    private BigDecimal totalTaxableValue;
    private BigDecimal totalCgst;
    private BigDecimal totalSgst;
    private BigDecimal totalIgst;
    private BigDecimal totalGst;
    private BigDecimal totalInvoiceValue;

    private List<B2CInvoiceItem> invoices;
}