package com.kalibyte.foundry.production.dto.response.report.summary;

public record ProductionDashboardSummary(
        int todayProduction,
        int todayDispatch,
        int todayRejected,
        int monthProduction,
        int monthDispatch,
        int monthRejected,
        int totalPendingDispatch,
        int activeOrders
) {}
