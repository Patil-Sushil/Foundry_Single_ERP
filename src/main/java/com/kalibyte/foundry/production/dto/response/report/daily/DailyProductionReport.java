package com.kalibyte.foundry.production.dto.response.report.daily;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class DailyProductionReport {

    private LocalDate date;

    private Integer totalProduction;
    private Integer totalDispatch;

    private List<DailyOrderEntry> orders;
}
