package com.kalibyte.foundry.production.dto.response.report.monthly;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MonthlyProductionReport {

    private int month;
    private int year;

    private Integer totalProduction;
    private Integer totalDispatch;

    private List<MonthlyDaySummary> dailyData;
}
