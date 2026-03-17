package com.kalibyte.foundry.reports.account.service.profitloss;

import com.kalibyte.foundry.reports.account.dto.response.profitloss.ProfitLossReport;

import java.time.LocalDate;

public interface ProfitLossReportService {

    ProfitLossReport generateReport(LocalDate from, LocalDate to);

}
