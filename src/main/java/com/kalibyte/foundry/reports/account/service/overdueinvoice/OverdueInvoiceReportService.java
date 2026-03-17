package com.kalibyte.foundry.reports.account.service.overdueinvoice;

import com.kalibyte.foundry.reports.account.dto.response.overdueinvoice.OverdueInvoiceReport;
import com.kalibyte.foundry.reports.account.dto.response.overdueinvoice.enums.OverdueSeverity;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Service responsible for generating overdue invoice reports.
 */
public interface OverdueInvoiceReportService {

    OverdueInvoiceReport generateReport(
            UUID customerId,
            OverdueSeverity severity,
            BigDecimal minAmount,
            int page,
            int size
    );

}
