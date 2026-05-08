package com.kalibyte.foundry.production.dto.response.report.daily;

public record DailyOrderEntry(
        String orderNumber,
        String customerName,
        int produced,
        int dispatched,
        int rejected
) {}
