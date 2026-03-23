package com.kalibyte.foundry.production.dto.response.report.monthly;

import lombok.Builder;
import lombok.Data;

import java.util.List;

public record MonthlyProductionReport(
        int month,
        int year,
        int totalProduction,
        int totalDispatch,
        List<MonthlyDaySummary> dailyData
) {}
