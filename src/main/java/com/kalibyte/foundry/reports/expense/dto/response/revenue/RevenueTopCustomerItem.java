package com.kalibyte.foundry.reports.expense.dto.response.revenue;

import java.math.BigDecimal;

/**
 * Represents the revenue contribution of a customer.
 *
 * Used in the Revenue Report to identify the top-performing
 * customers by invoiced revenue within a given date range.
 */
public record RevenueTopCustomerItem(String customerName,
                                     BigDecimal totalInvoiced,
                                     BigDecimal totalPaid,
                                     BigDecimal outstanding,
                                     BigDecimal revenueSharePercent) {
}
