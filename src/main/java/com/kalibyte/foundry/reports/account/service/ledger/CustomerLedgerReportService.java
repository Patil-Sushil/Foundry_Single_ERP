package com.kalibyte.foundry.reports.account.service.ledger;

import com.kalibyte.foundry.reports.account.dto.response.ledger.CustomerLedgerReport;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Service for generating Customer Ledger Reports.
 */
public interface CustomerLedgerReportService {

    CustomerLedgerReport getCustomerLedger(
            UUID customerId,
            LocalDate from,
            LocalDate to
    );

}