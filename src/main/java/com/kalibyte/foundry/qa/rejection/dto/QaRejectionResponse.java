package com.kalibyte.foundry.qa.rejection.dto;

import com.kalibyte.foundry.qa.common.enums.RejectionDisposition;
import com.kalibyte.foundry.qa.common.enums.RejectionStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class QaRejectionResponse {
    private Long id;
    private String rejectionNumber;
    private Long inspectionId;
    private String inspectionNumber;
    private UUID productionEntryId;
    private UUID productionItemId;
    private UUID orderId;
    private String orderNumber;
    private UUID orderItemId;
    private String itemName;
    private Long heatOrderItemId;
    private Integer rejectedQuantity;
    private BigDecimal rejectedWeight;
    private BigDecimal unitWeight;
    private String materialGrade;
    private Long primaryDefectId;
    private String primaryDefectName;
    private String defectSummary;
    private RejectionDisposition disposition;
    private LocalDate dispositionDate;
    private String dispositionBy;
    private String dispositionRemarks;
    private Long scrapEntryId;
    private RejectionStatus status;
    private LocalDateTime createdAt;
    private String createdBy;
}
