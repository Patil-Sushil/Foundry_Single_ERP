package com.kalibyte.foundry.production.dto.response.report.orderwise;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderProductionReport {
    private String orderNumber;
    private String customerName;
    private int totalOrderedQuantity;
    private int totalProduced;
    private int totalDispatched;
    private int totalRejected;
    private int pendingDispatch;
    private List<OrderItemProgress> items;

    private Double overallCompletionPercentage;
    private Integer totalAcceptedQty;
    private Integer totalRemainingQty;
    private Integer totalWaitingForShotBlast;
    private Integer totalWaitingForFettling;
    private Integer totalWaitingForInspection;
    private LocalDate expectedCompletionDate;
    private Boolean delayed;
}
