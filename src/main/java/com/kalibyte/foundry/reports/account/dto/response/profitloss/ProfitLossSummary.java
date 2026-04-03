package com.kalibyte.foundry.reports.account.dto.response.profitloss;

import java.math.BigDecimal;

/**
 * Summary section of the Profit & Loss report.
 *
 * Contains core financial totals used to evaluate
 * company profitability.
 */
public record ProfitLossSummary(

        BigDecimal totalRevenue,        // Realized (Invoices)
        BigDecimal totalCollections,

        BigDecimal wipProductionValue,  // Unrealized (Accepted Production)
        
        BigDecimal furnaceMaterialCost,
        BigDecimal furnaceElectricityCost,
        BigDecimal laborCost,
        BigDecimal generalMaterialIssueCost,

        BigDecimal grossProfit,

        BigDecimal operatingExpenses,   // General expenses (Admin, etc.)

        BigDecimal netProfit,

        BigDecimal grossMarginPercent,
        BigDecimal netMarginPercent,
        BigDecimal expenseRatioPercent

) {}
