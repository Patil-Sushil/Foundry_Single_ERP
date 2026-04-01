package com.kalibyte.foundry.production.dto.response.entry;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ProductionItemResponse {

    private UUID id;

    private UUID orderItemId;

    private String itemName;
    private String patternNumber;

    private Integer orderedQuantity;

    //------------------------------------------------
    // TODAY VALUES
    //------------------------------------------------
    private Integer readyCores;
    private Integer pouredMoulds;
    private Integer shotBlastingQuantity;
    private Integer fettlingQuantity;
    private Integer dispatchedQuantity;

    //------------------------------------------------
    // CUMULATIVE (calculated in service)
    //------------------------------------------------
    private Integer totalReadyCores;
    private Integer totalPouredMoulds;
    private Integer totalShotBlasting;
    private Integer totalFettling;
    private Integer totalDispatched;

    //------------------------------------------------
    // PENDING
    //------------------------------------------------
    private Integer pendingCores;
    private Integer pendingPouring;
    private Integer pendingShotBlasting;
    private Integer pendingFettling;
    private Integer pendingDispatch;

    private String itemRemark;
    private Long heatOrderItemId;
}
