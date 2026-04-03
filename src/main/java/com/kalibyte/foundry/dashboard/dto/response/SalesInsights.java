package com.kalibyte.foundry.dashboard.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SalesInsights {
    private LocalDate periodStartDate;
    private LocalDate periodEndDate;
    private String periodLabel;

    private Long newOrdersCount;
    private BigDecimal newOrdersValue;
    private Long directOrdersCount;
    private Long quotationOrdersCount;
    private BigDecimal periodRevenue;
    private BigDecimal previousPeriodRevenue;
    private BigDecimal revenueGrowthPercentage;
    private List<CustomerSummary> top5Customers;
    private Map<String, Long> orderPipelineStatus;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerSummary {
        private String customerId;
        private String customerName;
        private BigDecimal totalOrderValue;
    }
}
