package com.kalibyte.foundry.reports.expense.dto.response.expensebycategory;

import com.kalibyte.foundry.expenses.entity.enums.ExpenseCategory;
import lombok.Builder;

import java.math.BigDecimal;

/**
 * Represents expense summary for a specific category.
 *
 * Example:
 * OPERATIONAL → 45000 → 6 transactions
 */
@Builder
public record ExpenseCategoryItem(

        ExpenseCategory category,

        BigDecimal totalAmount,

        Long transactionCount
) {}
