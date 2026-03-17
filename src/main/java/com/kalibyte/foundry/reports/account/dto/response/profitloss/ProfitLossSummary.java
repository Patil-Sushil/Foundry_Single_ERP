package com.kalibyte.foundry.reports.account.dto.response.profitloss;

import java.math.BigDecimal;

/**
 * Summary section of the Profit & Loss report.
 *
 * Contains core financial totals used to evaluate
 * company profitability.
 */
public record ProfitLossSummary(

        BigDecimal totalRevenue,
        BigDecimal totalCollections,

        BigDecimal cogs,

        BigDecimal grossProfit,

        BigDecimal operatingExpenses,

        BigDecimal netProfit,

        BigDecimal grossMarginPercent,
        BigDecimal netMarginPercent,
        BigDecimal expenseRatioPercent

) {}
