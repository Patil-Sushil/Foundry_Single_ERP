package com.kalibyte.foundry.reports.gst.dto.response.taxliability;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxLiabilitySummary {

    private LocalDate periodFrom;
    private LocalDate periodTo;
    private String periodDescription;

    private BigDecimal totalTaxableValue;
    private BigDecimal totalCgst;
    private BigDecimal totalSgst;
    private BigDecimal totalIgst;
    private BigDecimal totalOutputTax;

    private int totalB2BInvoices;
    private BigDecimal b2bTaxableValue;
    private BigDecimal b2bTax;

    private int totalB2CInvoices;
    private BigDecimal b2cTaxableValue;
    private BigDecimal b2cTax;

    private List<MonthlyTaxBreakdown> monthlyBreakdown;
}