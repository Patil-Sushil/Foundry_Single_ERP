package com.kalibyte.foundry.reports.account.dto.response.cashflow;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Final Cash Flow report response.
 */
@Builder
public record CashFlowReport(

        LocalDate fromDate,

        LocalDate toDate,

        BigDecimal totalInflow,

        BigDecimal totalOutflow,

        BigDecimal netCashFlow,

        List<CashFlowItem> dailyCashFlow
) {}