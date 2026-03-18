package com.kalibyte.foundry.production.dto.response.report.orderwise;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class OrderProductionReport {

    private String orderNumber;
    private String customerName;

    private Integer totalOrderedQuantity;

    private Integer totalProduced;
    private Integer totalDispatched;
    private Integer pendingDispatch;

    private List<OrderItemProgress> items;
}
