package com.kalibyte.foundry.qa.inspection.dto;

import com.kalibyte.foundry.qa.common.enums.FindingDisposition;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class FindingRequest {
    @NotNull(message = "Defect ID is required")
    private Long defectId;
    
    @NotNull(message = "Quantity affected is required")
    @Min(value = 1, message = "Quantity affected must be at least 1")
    private Integer quantityAffected;
    
    @NotNull(message = "Disposition is required")
    private FindingDisposition disposition;
    
    private String reworkInstruction;
    private List<String> photoUrls;
    private String remarks;
}
