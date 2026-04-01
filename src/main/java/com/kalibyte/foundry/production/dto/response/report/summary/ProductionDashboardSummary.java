package com.kalibyte.foundry.production.dto.response.report.summary;

public record ProductionDashboardSummary(
        int todayProduction,
        int todayDispatch,
        int monthProduction,
        int monthDispatch,
        int totalPendingDispatch,
        int activeOrders
) {}
