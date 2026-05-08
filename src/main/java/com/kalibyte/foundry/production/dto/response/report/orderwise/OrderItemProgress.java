package com.kalibyte.foundry.production.dto.response.report.orderwise;

import java.util.UUID;

public record OrderItemProgress(
        String itemName,
        String patternNumber,
        int orderedQuantity,
        UUID orderItemId,
        int totalReadyCores,
        int totalPouredMoulds,
        int totalShotBlasting,
        int totalFettling,
        int totalDispatched,
        int totalRejected,
        int pendingDispatch
) {}