package com.kalibyte.foundry.reports.account.dto.response.aging;

import lombok.Builder;

import java.math.BigDecimal;

/**
 * Summary totals across all customers for each aging bucket.
 * Used for dashboard summary at the top of the report.
 */
@Builder
public record AgingSummary(

        BigDecimal current,

        BigDecimal days1to30,

        BigDecimal days31to60,

        BigDecimal days61to90,

        BigDecimal days90plus
) {}