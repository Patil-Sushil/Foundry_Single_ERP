package com.kalibyte.foundry.scrap.dto.request;

import com.kalibyte.foundry.scrap.enums.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScrapEntryRequest {
    private String scrapNumber;
    private LocalDate scrapDate;
    private ScrapSource scrapSource;
    private String sourceReferenceId;
    private String sourceReferenceType;
    private Long heatId;
    private Long inspectionId;
    private Long customerReturnId;
    private Long qaRejectionId;
    private String rejectionNumber;
    private String returnNumber;
    private String grade;
    private BigDecimal totalWeight;
    private BigDecimal totalValue;
    private ConfidenceLevel confidenceLevel;
    private VerificationMethod verificationMethod;
    private PhysicalCondition physicalCondition;
    private String visualGradeAssessment;
    private Boolean requiresTesting;
    private ScrapStatus initialStatus;
    private List<ScrapItemRequest> scrapItems;
    private String remarks;
}
