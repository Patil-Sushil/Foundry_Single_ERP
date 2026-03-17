package com.kalibyte.foundry.reports.account.dto.response.overdueinvoice;

import com.kalibyte.foundry.reports.account.dto.response.overdueinvoice.enums.OverdueSeverity;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents a single overdue invoice entry.
 */
public record OverdueInvoiceItem(

        String invoiceNumber,
        LocalDate invoiceDate,
        LocalDate dueDate,

        long daysOverdue,

        String customerName,
        String customerPhone,

        BigDecimal invoiceAmount,
        BigDecimal paidAmount,
        BigDecimal balanceDue,

        OverdueSeverity severity

) {}
