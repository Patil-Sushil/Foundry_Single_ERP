package com.kalibyte.foundry.reports.expense.dto.response.revenue;

import java.math.BigDecimal;
import java.time.YearMonth;


/**
 * Represents revenue statistics for a single month.
 *
 * This DTO is used in the Revenue Report to provide a month-wise
 * breakdown of invoiced revenue, collections, outstanding amounts,
 * and growth metrics.
 *
 * Using Java record ensures immutability and concise data representation.
 */
public record RevenueMonthlyItem(YearMonth month,
                                 BigDecimal invoicedAmount,
                                 BigDecimal collectedAmount,
                                 BigDecimal outstandingAmount,
                                 Long invoiceCount,
                                 BigDecimal growthPercent) {
}
