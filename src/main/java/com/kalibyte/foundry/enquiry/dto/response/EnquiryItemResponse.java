package com.kalibyte.foundry.enquiry.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

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
    private Boolean patternProvidedByCustomer;
    private String patternName;
    private String patternType;
    private String patternMaterial;
    private LocalDate inwardDate;
    private LocalDate outwardDate;
    private Boolean machineRequired;

}
