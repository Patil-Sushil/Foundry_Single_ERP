package com.kalibyte.foundry.reports.account.service.aging;

import com.kalibyte.foundry.reports.account.dto.response.aging.AgingReport;

import java.time.LocalDate;

public interface AgingReportService {

    AgingReport getReceivablesAging(LocalDate asOfDate);

}
