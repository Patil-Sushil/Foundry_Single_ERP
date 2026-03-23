package com.kalibyte.foundry.order.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Order Item Response
 */
@Data
@Builder
public class OrderItemResponse {
    private UUID id;
    private String partName;
    private String materialGrade;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;

    //--------------------------------------------
    // PATTERN
    //--------------------------------------------
    private Boolean patternProvidedByCustomer;

    private String patternNumber;
    private String patternName;

    private String receiptName;
    private String receiptType;
}