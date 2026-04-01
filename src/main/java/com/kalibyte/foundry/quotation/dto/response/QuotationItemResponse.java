package com.kalibyte.foundry.quotation.dto.response;

import com.kalibyte.foundry.quotation.entity.enums.QuotationPatternStatus;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuotationItemResponse {

    private String partName;
    private String drawingNumber;
    private String materialGrade;

    // Metal & Casting
    private String metalType;
    private String metalCategory;
    private String castingProcess;

    private BigDecimal netWeightKg;
    private BigDecimal grossWeightKg;

    private QuotationPatternStatus patternStatus;
    private Boolean patternProvidedByCustomer;

    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;

    // Pattern details
    private String receiptName;
    private String receiptType;
    private String receiptMaterial;
    private String inwardDate;
    private String outwardDate;

    private String patternNumber;
    private String patternName;
    private String patternType;
}