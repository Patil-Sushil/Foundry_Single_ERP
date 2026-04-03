package com.kalibyte.foundry.dashboard.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductionKPIs {
    private LocalDate periodStartDate;
    private LocalDate periodEndDate;
    private String periodLabel;

    private Long heatCount;
    private BigDecimal averagePowerToWeightRatio;
    private BigDecimal furnaceYieldPercentage;
    private BigDecimal liquidMetalWeight;
    private BigDecimal totalChargeWeight;
    private Map<String, Long> stageWiseBottlenecks;
    private BigDecimal dispatchedQuantity;
    private BigDecimal scheduledTarget;
    private BigDecimal dispatchPerformancePercentage;
}
