package com.kalibyte.foundry.reports.expense.dto.response.revenue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
/**
 * Main DTO representing the Revenue Report.
 *
 * This report provides a consolidated view of:
 * - Total revenue generated
 * - Amount collected
 * - Outstanding balances
 * - Customer contribution
 * - Monthly revenue trends
 */
public record RevenueReport(BigDecimal totalRevenue,
                            BigDecimal totalCollected,
                            BigDecimal totalOutstanding,

                            BigDecimal collectionEfficiencyPercent,

                            BigDecimal averageInvoiceValue,
                            BigDecimal averageDaysToCollect,

                            List<RevenueMonthlyItem> monthlyBreakdown,
                            List<RevenueTopCustomerItem> topCustomers,

                            LocalDateTime reportGeneratedAt,
                            String generatedBy
) {
}
