package com.kalibyte.foundry.production.dto.response.report.orderwise;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class DailyBreakdown {

    private LocalDate date;
    private List<DailyItemBreakdown> items;
}
