package com.kalibyte.foundry.dashboard.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FinancialHealth {
    private LocalDate periodStartDate;
    private LocalDate periodEndDate;
    private String periodLabel;

    private BigDecimal totalReceivables;
    private Long overdueInvoicesCount;
    private BigDecimal overdueInvoicesValue;
    private BigDecimal totalCollections;
    private BigDecimal periodCgst;
    private BigDecimal periodSgst;
    private BigDecimal periodIgst;
    private BigDecimal periodTotalTaxLiability;
    private BigDecimal periodMaterialCost;
    private BigDecimal periodSalesValue;
    private BigDecimal materialCostRatioPercentage;
}
