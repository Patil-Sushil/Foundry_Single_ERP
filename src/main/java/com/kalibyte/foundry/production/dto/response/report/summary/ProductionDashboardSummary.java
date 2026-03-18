package com.kalibyte.foundry.production.dto.response.report.summary;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductionDashboardSummary {

    private Integer todayProduction;
    private Integer todayDispatch;

    private Integer monthProduction;
    private Integer monthDispatch;

    private Integer totalPendingDispatch;

    private Integer activeOrders;
}
