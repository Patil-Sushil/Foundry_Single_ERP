package com.kalibyte.foundry.production.dto.response.report.orderwise;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemProgress {
    private UUID orderId;
    private String orderNumber;
    private String customerName;

    private String itemName;
    private String patternNumber;
    private int orderedQuantity;
    private UUID orderItemId;
    
    private int totalReadyCores;
    private int totalPouredMoulds;
    private int totalShotBlasting;
    private int totalFettling;
    private int totalDispatched;
    private int totalRejected;
    private int pendingDispatch;

    private Integer acceptedQty;
    private Integer inspectedQty;

    private Integer waitingForShotBlast;
    private Integer waitingForFettling;
    private Integer waitingForInspection;

    private Double completionPercentage;

    private Integer remainingQty;

    private Integer etaDays;

    private LocalDate deliveryDate;
    private LocalDate expectedCompletionDate;

    private Boolean delayed;

    private String productionStatus;
}
