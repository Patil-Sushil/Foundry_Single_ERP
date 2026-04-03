package com.kalibyte.foundry.dashboard.service;

import com.kalibyte.foundry.dashboard.dto.response.*;
import com.kalibyte.foundry.dashboard.util.DateRangeResolver;

public interface DashboardService {
    DashboardSummaryResponse getSummary(DateRangeResolver.DateRange range);
    SalesInsights getSalesInsights(DateRangeResolver.DateRange range);
    ProductionKPIs getProductionKPIs(DateRangeResolver.DateRange range);
    FinancialHealth getFinancialHealth(DateRangeResolver.DateRange range);
    InventoryAlerts getInventoryAlerts(DateRangeResolver.DateRange range);
}
