package com.kalibyte.foundry.quotation.dto.request;

import com.kalibyte.foundry.enquiry.entity.enums.PatternProvidedBy;
import com.kalibyte.foundry.pattern.dto.request.PatternReceiptRequest;
import com.kalibyte.foundry.quotation.entity.enums.PatternStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Quotation Item Request

 * RULE:
 * - ALWAYS requires full pattern details
 */
@Getter
@Setter
public class QuotationItemRequest {

    private String partName;
    private String drawingNumber;
    private String materialGrade;

    private BigDecimal netWeightKg;
    private BigDecimal grossWeightKg;

    private PatternStatus patternStatus;

    private int quantity;
    private BigDecimal unitPrice;

    //--------------------------------------------
    // PATTERN (MANDATORY)
    //--------------------------------------------

    private PatternProvidedBy patternProvidedBy;

    private UUID patternId; // if company pattern

    private PatternReceiptRequest patternReceipt; // if customer pattern
}