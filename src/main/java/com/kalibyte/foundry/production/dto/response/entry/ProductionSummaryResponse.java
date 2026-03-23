package com.kalibyte.foundry.production.dto.response.entry;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductionSummaryResponse {

    private Integer totalEntries;

    private Integer totalReadyCores;
    private Integer totalPouredMoulds;
    private Integer totalShotBlasting;
    private Integer totalFettling;
    private Integer totalDispatched;

    private Integer totalPendingDispatch;
}
