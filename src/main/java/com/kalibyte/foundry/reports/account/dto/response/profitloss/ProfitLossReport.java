package com.kalibyte.foundry.reports.account.dto.response.profitloss;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Complete Profit & Loss report response.
 */
public record ProfitLossReport(

        ProfitLossSummary summary,

        List<ProfitLossExpenseItem> expenseBreakdown,

        List<ProfitLossMonthlyItem> monthlyTrend,

        LocalDateTime generatedAt,
        String generatedBy

) {}