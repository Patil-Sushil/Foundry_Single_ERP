package com.kalibyte.foundry.reports.account.dto.response.overdueinvoice;

import java.math.BigDecimal;

/**
 * Represents summary metrics of overdue invoices.
 */
public record OverdueSummary(

        BigDecimal totalOverdueAmount,
        Long totalInvoices,

        BigDecimal bucket1to30,
        BigDecimal bucket31to60,
        BigDecimal bucket61to90,
        BigDecimal bucket90plus,

        BigDecimal averageDaysOverdue

) {}
