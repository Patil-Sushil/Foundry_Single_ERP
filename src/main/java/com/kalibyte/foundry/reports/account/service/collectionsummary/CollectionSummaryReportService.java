package com.kalibyte.foundry.reports.account.service.collectionsummary;

import com.kalibyte.foundry.reports.account.dto.response.collectionsummary.CollectionSummaryReport;

import java.time.LocalDate;

/**
 * Service for generating payment collection summary.
 */
public interface CollectionSummaryReportService {

    CollectionSummaryReport getCollectionSummary(LocalDate from, LocalDate to);

}
