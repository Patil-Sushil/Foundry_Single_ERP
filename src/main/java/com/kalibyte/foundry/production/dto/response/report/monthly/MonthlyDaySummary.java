package com.kalibyte.foundry.production.dto.response.report.monthly;

import java.time.LocalDate;

public record MonthlyDaySummary(
        LocalDate date,
        int produced,
        int dispatched
) {}
