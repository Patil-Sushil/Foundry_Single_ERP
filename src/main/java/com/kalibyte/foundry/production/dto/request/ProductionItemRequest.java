package com.kalibyte.foundry.production.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ProductionItemRequest {

    @NotNull(message = "Order item ID is required")
    private UUID orderItemId;

    private String patternNumber;

    @Min(value = 0, message = "Ready cores cannot be negative")
    private Integer readyCores;

    @Min(value = 0, message = "Poured moulds cannot be negative")
    private Integer pouredMoulds;

    @Min(value = 0, message = "Shot blasting quantity cannot be negative")
    private Integer shotBlastingQuantity;

    @Min(value = 0, message = "Fettling quantity cannot be negative")
    private Integer fettlingQuantity;

    private String itemRemark;
    private Long heatOrderItemId;
}