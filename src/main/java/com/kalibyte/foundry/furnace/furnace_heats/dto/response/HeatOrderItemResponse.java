package com.kalibyte.foundry.furnace.furnace_heats.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HeatOrderItemResponse {
    private Long id;
    private UUID orderItemId;
    private String partName;
    private String drawingNumber;
    private Integer quantityProduced;
    private BigDecimal weightProduced;
    private BigDecimal pieceWeight;
    private String stockItemName;
    private String stockItemCode;
    private String remarks;
    private LocalDateTime createdAt;
}
