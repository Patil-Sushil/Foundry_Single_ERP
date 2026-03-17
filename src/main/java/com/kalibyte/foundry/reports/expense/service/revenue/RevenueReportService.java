package com.kalibyte.foundry.reports.expense.service.revenue;

import com.kalibyte.foundry.reports.expense.dto.response.revenue.RevenueReport;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Service responsible for generating Revenue Reports.
 */
public interface RevenueReportService {

    RevenueReport generateRevenueReport(
            LocalDate from,
            LocalDate to,
            UUID customerId
    );

}
