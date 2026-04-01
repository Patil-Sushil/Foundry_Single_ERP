package com.kalibyte.foundry.reports.gst.dto.response.hsn;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HsnSummaryReport {

    private LocalDate periodFrom;
    private LocalDate periodTo;
    private String periodDescription;

    private int totalHsnCodes;
    private BigDecimal totalTaxableValue;
    private BigDecimal totalCgst;
    private BigDecimal totalSgst;
    private BigDecimal totalIgst;
    private BigDecimal totalGst;
    private BigDecimal totalInvoiceValue;

    private List<HsnSummaryItem> items;
}