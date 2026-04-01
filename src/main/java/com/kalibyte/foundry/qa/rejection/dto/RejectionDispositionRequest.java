package com.kalibyte.foundry.qa.rejection.dto;

import com.kalibyte.foundry.qa.common.enums.RejectionDisposition;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RejectionDispositionRequest {
    @NotNull(message = "Disposition is required")
    private RejectionDisposition disposition;
    
    private String remarks;
    
    private String performedBy;
}
