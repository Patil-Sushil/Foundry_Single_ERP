package com.kalibyte.foundry.qa.inspection.dto;

import com.kalibyte.foundry.qa.common.enums.InspectionStage;
import com.kalibyte.foundry.qa.common.enums.InspectionType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class QaInspectionRequest {
    @NotNull(message = "Production entry ID is required")
    private UUID productionEntryId;
    
    @NotNull(message = "Production item ID is required")
    private UUID productionItemId;
    
    @NotNull(message = "Order ID is required")
    private UUID orderId;
    
    @NotNull(message = "Order item ID is required")
    private UUID orderItemId;
    
    private Long heatOrderItemId;
    
    @NotNull(message = "Inspection stage is required")
    private InspectionStage inspectionStage;
    
    @NotNull(message = "Inspection type is required")
    private InspectionType inspectionType;
    
    @NotNull(message = "Inspection date is required")
    private LocalDate inspectionDate;
    
    @NotBlank(message = "Inspector name is required")
    private String inspectorName;
    
    @NotNull(message = "Total inspected quantity is required")
    @Min(value = 1, message = "Total inspected must be at least 1")
    private Integer totalInspected;
    
    private String remarks;
    
    private List<FindingRequest> findings;
}
