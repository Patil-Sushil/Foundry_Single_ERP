package com.kalibyte.foundry.production.dto.response.report.orderwise;

import java.util.List;

public record OrderProductionReport(
        String orderNumber,
        String customerName,
        int totalOrderedQuantity,
        int totalProduced,
        int totalDispatched,
        int pendingDispatch,
        List<OrderItemProgress> items
) {}
