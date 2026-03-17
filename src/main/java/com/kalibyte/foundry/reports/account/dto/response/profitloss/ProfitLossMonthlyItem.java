package com.kalibyte.foundry.reports.account.dto.response.profitloss;

import java.math.BigDecimal;
import java.time.YearMonth;

/**
 * Monthly financial trend item used in P&L trend charts.
 */
public record ProfitLossMonthlyItem(

        YearMonth month,
        BigDecimal revenue,
        BigDecimal cogs,
        BigDecimal expenses,
        BigDecimal netProfit,
        BigDecimal growthPercent

) {}
