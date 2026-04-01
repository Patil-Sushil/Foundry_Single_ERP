package com.kalibyte.foundry.reports.expense.dto.response.expensebyhead;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Represents a single expense head summary row.
 *
 * Example:
 * Diesel → 25000 → 5 transactions
 */
@Builder
public record ExpenseHeadItem(

        UUID expenseHeadId,

        String expenseHeadName,

        BigDecimal totalAmount,

        Long transactionCount
) {}
