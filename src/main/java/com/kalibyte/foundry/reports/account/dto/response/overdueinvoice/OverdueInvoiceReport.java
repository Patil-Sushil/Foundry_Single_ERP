package com.kalibyte.foundry.reports.account.dto.response.overdueinvoice;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Main response object for Overdue Invoice Report.
 */
public record OverdueInvoiceReport(

        OverdueSummary summary,

        List<OverdueInvoiceItem> invoices,

        List<OverdueCustomerGroup> customerGroups,

        LocalDateTime generatedAt,
        String generatedBy

) {}
