package com.kalibyte.foundry.reports.account.dto.response.cashflow;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents daily cash flow movement.

 * inflow  = customer payments received
 * outflow = expenses paid
 * netFlow = inflow - outflow
 */
@Builder
public record CashFlowItem(

        LocalDate date,

        BigDecimal inflow,

        BigDecimal outflow,

        BigDecimal netFlow
) {}