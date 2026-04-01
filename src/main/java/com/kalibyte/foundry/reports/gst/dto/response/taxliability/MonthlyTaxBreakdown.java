package com.kalibyte.foundry.reports.gst.dto.response.taxliability;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyTaxBreakdown {

    private String month;
    private int invoiceCount;
    private BigDecimal taxableValue;
    private BigDecimal cgst;
    private BigDecimal sgst;
    private BigDecimal igst;
    private BigDecimal totalTax;
}