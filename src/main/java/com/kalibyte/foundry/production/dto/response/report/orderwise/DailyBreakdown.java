package com.kalibyte.foundry.production.dto.response.report.orderwise;

import java.time.LocalDate;
import java.util.List;

public record DailyBreakdown(
        LocalDate date,
        List<DailyItemBreakdown> items
) {}
