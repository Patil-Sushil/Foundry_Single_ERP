package com.kalibyte.foundry.quotation.dto.request;

import com.kalibyte.foundry.enquiry.entity.enums.MetalType;
import com.kalibyte.foundry.enquiry.entity.enums.PatternProvidedBy;
import com.kalibyte.foundry.pattern.dto.request.PatternReceiptRequest;
import com.kalibyte.foundry.quotation.entity.enums.QuotationPatternStatus;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuotationItemRequest {

    private String partName;
    private String drawingNumber;
    private String materialGrade;

    // Metal & Casting
    private MetalType metalType;
    private String castingProcess;

    private BigDecimal netWeightKg;
    private BigDecimal grossWeightKg;

    private QuotationPatternStatus patternStatus;

    private Integer quantity;
    private BigDecimal unitPrice;

    // Pattern logic
    private PatternProvidedBy patternProvidedBy;
    private UUID patternId;
    private PatternReceiptRequest patternReceipt;
}