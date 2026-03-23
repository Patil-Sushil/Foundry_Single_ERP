package com.kalibyte.foundry.production.service;

import com.kalibyte.foundry.production.dto.response.report.daily.DailyProductionReport;
import com.kalibyte.foundry.production.dto.response.report.monthly.MonthlyProductionReport;
import com.kalibyte.foundry.production.dto.response.report.orderwise.OrderProductionReport;
import com.kalibyte.foundry.production.dto.response.report.summary.ProductionDashboardSummary;

import java.time.LocalDate;
import java.util.UUID;

public interface ProductionReportService {

    OrderProductionReport getOrderReport(UUID orderId);

    DailyProductionReport getDailyReport(LocalDate date);

    MonthlyProductionReport getMonthlyReport(int month, int year);

    ProductionDashboardSummary getDashboardSummary();
}