package com.kalibyte.foundry.production.dto.response.report.daily;


import java.time.LocalDate;
import java.util.List;

public record DailyProductionReport(
        LocalDate date,
        int totalProduction,
        int totalDispatch,
        List<DailyOrderEntry> orders
) {}
