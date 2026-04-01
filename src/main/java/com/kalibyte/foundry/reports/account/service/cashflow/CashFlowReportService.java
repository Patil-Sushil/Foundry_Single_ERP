package com.kalibyte.foundry.reports.account.service.cashflow;

import com.kalibyte.foundry.reports.account.dto.response.cashflow.CashFlowReport;

import java.time.LocalDate;

public interface CashFlowReportService {

    CashFlowReport getCashFlow(LocalDate from, LocalDate to);

}
