package com.kalibyte.foundry.reports.account.dto.response.overdueinvoice;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents overdue totals grouped by customer.
 */
public record OverdueCustomerGroup(

        String customerName,
        BigDecimal totalOverdue,
        Long invoiceCount,
        LocalDate oldestInvoiceDate

) {}
