package com.kalibyte.foundry.reports.account.service.outstanding;


import com.kalibyte.foundry.reports.account.dto.response.outstanding.CustomerOutstandingReport;

import java.time.LocalDate;

/**
 * Service responsible for generating
 * Customer Outstanding Report.

 * Shows unpaid balances for customers
 * as of a specific date.
 */
public interface CustomerOutstandingReportService {

    CustomerOutstandingReport getCustomerOutstanding(LocalDate asOfDate);

}