package com.kalibyte.foundry.reports.service;

import com.kalibyte.foundry.reports.dto.response.accounts.DailyCollectionReport;

import java.time.LocalDate;

public interface AccountsReportService {

    DailyCollectionReport getDailyCollection(LocalDate from, LocalDate to);

}