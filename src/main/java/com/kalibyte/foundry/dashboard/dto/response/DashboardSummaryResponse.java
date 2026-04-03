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
public class DashboardSummaryResponse {
    private LocalDate periodStartDate;
    private LocalDate periodEndDate;
    private String periodLabel;

    private Long newOrdersCount;
    private BigDecimal newOrdersValue;
    private BigDecimal periodRevenue;
    private BigDecimal previousPeriodRevenue;
    private Long heatCount;
    private BigDecimal averageMeltingEfficiency;
    private BigDecimal furnaceYieldPercentage;
    private BigDecimal totalReceivables;
    private Long overdueInvoicesCount;
    private BigDecimal overdueInvoicesValue;
    private BigDecimal totalCollections;
    private Long lowStockAlertsCount;
    private BigDecimal rejectionRatePercentage;
}
