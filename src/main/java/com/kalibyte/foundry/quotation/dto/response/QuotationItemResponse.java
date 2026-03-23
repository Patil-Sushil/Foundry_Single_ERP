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

    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;


    private Boolean patternProvidedByCustomer;

    // CUSTOMER PATTERN
    private String receiptName;
    private String receiptType;
    private String receiptMaterial;
    private String inwardDate;
    private String outwardDate;

    // COMPANY PATTERN
    private String patternNumber;
    private String patternName;
    private String patternType;
}