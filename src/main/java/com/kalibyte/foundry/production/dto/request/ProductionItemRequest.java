package com.kalibyte.foundry.production.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ProductionItemRequest {

    @NotNull
    private UUID orderItemId;

    @Min(0)
    private Integer readyCores = 0;

    @Min(0)
    private Integer pouredMoulds = 0;

    @Min(0)
    private Integer shotBlastingQuantity = 0;

    @Min(0)
    private Integer fettlingQuantity = 0;

    @Min(0)
    private Integer dispatchedQuantity = 0;

    private String itemRemark;
}
