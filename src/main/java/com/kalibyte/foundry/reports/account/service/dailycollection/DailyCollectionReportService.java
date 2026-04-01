package com.kalibyte.foundry.reports.account.service.dailycollection;

import com.kalibyte.foundry.reports.account.dto.response.dailycollection.DailyCollectionReport;

import java.time.LocalDate;

/**
 * Service responsible for generating
 * Daily Collection Report.
 *
 * Business Purpose:
 * Shows how much payment was collected
 * each day within a date range.
 */
public interface DailyCollectionReportService {

    /**
     * Generate daily collection report.
     *
     * @param from start date
     * @param to end date
     * @return DailyCollectionReport
     */
    DailyCollectionReport getDailyCollection(LocalDate from, LocalDate to);

}