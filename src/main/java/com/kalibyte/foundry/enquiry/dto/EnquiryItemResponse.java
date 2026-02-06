package com.kalibyte.foundry.enquiry.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Builder
@Data
public class EnquiryItemResponse {
    private String partName;
    private String metalCategory;
    private String metalType;
    private Integer requiredQuantity;
    private BigDecimal approxPieceWeightKg;
    private BigDecimal totalWeightKg;
    private String castingProcess;
    private Boolean patternAvailable;
    private Boolean machineRequired;

}
