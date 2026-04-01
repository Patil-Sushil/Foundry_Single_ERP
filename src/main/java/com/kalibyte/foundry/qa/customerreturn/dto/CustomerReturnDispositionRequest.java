package com.kalibyte.foundry.qa.customerreturn.dto;

import com.kalibyte.foundry.qa.common.enums.ReturnDisposition;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CustomerReturnDispositionRequest {
    @NotNull(message = "Disposition is required")
    private ReturnDisposition disposition;
    
    private String remarks;
    
    @NotNull(message = "Performed by is required")
    private String performedBy;
    
    private BigDecimal creditAmount;
    private UUID replacementOrderId;
}
