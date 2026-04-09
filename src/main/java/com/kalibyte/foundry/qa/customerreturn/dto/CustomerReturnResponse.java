package com.kalibyte.foundry.qa.customerreturn.dto;

import com.kalibyte.foundry.qa.common.enums.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CustomerReturnResponse {
    private Long id;
    private String returnNumber;
    private UUID customerId;
    private String customerName;
    private UUID orderId;
    private String orderNumber;
    private UUID orderItemId;
    private String itemName;
    private UUID productionEntryId;
    private Long heatOrderItemId;
    private LocalDate returnDate;
    private Integer returnedQuantity;
    private BigDecimal returnedWeight;
    private String materialGrade;
    private ComplaintCategory complaintCategory;
    private String complaintDescription;
    private String customerReferenceNo;
    private LocalDate qaAssessmentDate;
    private String qaInspectorName;
    private QaFinding qaFinding;
    private String qaRemarks;
    private RootCauseCategory rootCauseCategory;
    private String rootCauseDescription;
    private ReturnDisposition disposition;
    private LocalDate dispositionDate;
    private String dispositionBy;
    private BigDecimal creditAmount;
    private UUID replacementOrderId;
    private UUID creditNoteId;
    private Long scrapEntryId;
    private Long inspectionId;
    private String inspectionNumber;
    private ReturnStatus status;
    private LocalDateTime createdAt;
    private String createdBy;
}
