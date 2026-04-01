package com.kalibyte.foundry.reports.account.dto.response.profitloss;

import java.math.BigDecimal;

/**
 * Represents expense breakdown grouped by Expense Head.
 */
public record ProfitLossExpenseItem(

        String expenseHead,
        String category,
        BigDecimal amount,
        BigDecimal percentage

) {}
