package com.kalibyte.foundry.enquiry.dto.response;

import com.kalibyte.foundry.enquiry.entity.enums.PatternProvidedBy;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Enquiry Item Response

 * NOTE:
 * - We only expose patternProvidedByCustomer (NOT full pattern details)
 * - Pattern details will be used internally or in later modules (Quotation/Order)
 */
@Builder
@Data
public class EnquiryItemResponse {

    private String partName;
    private String metalCategory;
    private String metalType;
    private String materialGrade;
    private Integer requiredQuantity;
    private BigDecimal approxPieceWeightKg;
    private BigDecimal totalWeightKg;
    private String castingProcess;

    private PatternProvidedBy patternProvidedBy;

    private Boolean machineRequired;
}