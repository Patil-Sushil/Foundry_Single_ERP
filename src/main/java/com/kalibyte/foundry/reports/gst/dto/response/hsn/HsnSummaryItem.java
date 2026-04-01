package com.kalibyte.foundry.reports.gst.dto.response.hsn;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HsnSummaryItem {

    private String hsnCode;
    private String description;
    private String uqc; // Unit Quantity Code (e.g., KGS, NOS)
    private BigDecimal totalQuantity;
    private BigDecimal totalValue;
    private BigDecimal taxableValue;
    private BigDecimal gstRate;
    private BigDecimal cgstAmount;
    private BigDecimal sgstAmount;
    private BigDecimal igstAmount;
    private BigDecimal totalGst;
}