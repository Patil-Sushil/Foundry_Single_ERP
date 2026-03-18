package com.kalibyte.foundry.production.dto.response.report.orderwise;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class OrderItemProgress {

    private String itemName;
    private String patternNumber;

    private Integer orderedQuantity;
    private UUID orderItemId;

    private Integer totalReadyCores;
    private Integer totalPouredMoulds;
    private Integer totalShotBlasting;
    private Integer totalFettling;
    private Integer totalDispatched;

    private Integer pendingDispatch;
}
