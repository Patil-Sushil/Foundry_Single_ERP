package com.kalibyte.foundry.production.dto.response.report.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WipDashboardResponse {
    private Integer totalWaitingForShotBlast;
    private Integer totalWaitingForFettling;
    private Integer totalWaitingForInspection;
}
