package com.kalibyte.foundry.furnace.furnace_heats.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HeatOrderItemRequest {
    private Long id;
    private UUID orderItemId;
    private Integer quantityProduced;
    private BigDecimal weightProduced;
    private BigDecimal pieceWeight;
    private String stockItemName;
    private String stockItemCode;
    private String remarks;
}
