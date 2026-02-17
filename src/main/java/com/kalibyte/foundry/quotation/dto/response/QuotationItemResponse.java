package com.kalibyte.foundry.quotation.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class QuotationItemResponse {
    private UUID id;
    private String partName;
    private String drawingNumber;
    private String materialGrade;
    private BigDecimal netWeightKg;
    private BigDecimal grossWeightKg;
    private String patternStatus;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
}