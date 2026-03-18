package com.kalibyte.foundry.production.dto.response.report.monthly;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class MonthlyDaySummary {

    private LocalDate date;
    private Integer produced;
    private Integer dispatched;
}
