package com.kalibyte.foundry.qa.inspection.dto;

import com.kalibyte.foundry.qa.common.enums.InspectionResult;
import com.kalibyte.foundry.qa.common.enums.InspectionStage;
import com.kalibyte.foundry.qa.common.enums.InspectionStatus;
import com.kalibyte.foundry.qa.common.enums.InspectionType;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class QaInspectionResponse {
    private Long id;
    private String inspectionNumber;
    private UUID productionEntryId;
    private UUID productionItemId;
    private UUID orderId;
    private String orderNumber;
    private UUID orderItemId;
    private String itemName;
    private Long heatOrderItemId;
    private InspectionStage inspectionStage;
    private InspectionType inspectionType;
    private LocalDate inspectionDate;
    private String inspectorName;
    private Integer totalInspected;
    private Integer totalAccepted;
    private Integer totalRejected;
    private Integer totalReworkable;
    private InspectionResult result;
    private InspectionStatus status;
    private String remarks;
    private List<FindingResponse> findings;
    private LocalDateTime createdAt;
    private String createdBy;
}
