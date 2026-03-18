package com.kalibyte.foundry.production.dto.response.report.orderwise;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DailyItemBreakdown {

    private String itemName;

    private Integer readyCores;
    private Integer pouredMoulds;
    private Integer shotBlasting;
    private Integer fettling;
    private Integer dispatched;
}
