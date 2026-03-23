package com.kalibyte.foundry.production.dto.response.report.orderwise;

public record DailyItemBreakdown(
        String itemName,
        int readyCores,
        int pouredMoulds,
        int shotBlasting,
        int fettling,
        int dispatched
) {}
