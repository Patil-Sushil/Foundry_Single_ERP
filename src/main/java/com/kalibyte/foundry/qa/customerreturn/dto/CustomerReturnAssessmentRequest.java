package com.kalibyte.foundry.qa.customerreturn.dto;

import com.kalibyte.foundry.qa.common.enums.QaFinding;
import com.kalibyte.foundry.qa.common.enums.RootCauseCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CustomerReturnAssessmentRequest {
    @NotNull(message = "QA finding is required")
    private QaFinding qaFinding;
    
    @NotNull(message = "Root cause category is required")
    private RootCauseCategory rootCauseCategory;
    
    private String rootCauseDescription;
    
    @NotBlank(message = "Inspector name is required")
    private String inspectorName;
    
    private String remarks;
}
