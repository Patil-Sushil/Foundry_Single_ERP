package com.kalibyte.foundry.quotation.dto.request;

import com.kalibyte.foundry.quotation.entity.enums.PatternStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class QuotationItemRequest {

    private String partName;
    private String drawingNumber;
    private String materialGrade;
    private BigDecimal netWeightKg;
    private BigDecimal grossWeightKg;
    private PatternStatus patternStatus;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
}
