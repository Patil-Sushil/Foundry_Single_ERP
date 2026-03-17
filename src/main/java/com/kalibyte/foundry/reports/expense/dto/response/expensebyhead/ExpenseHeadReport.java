package com.kalibyte.foundry.reports.expense.dto.response.expensebyhead;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Expense by Head Report
 *
 * Provides a summary of expenses grouped by expense head.
 */
@Builder
public record ExpenseHeadReport(

        LocalDate fromDate,

        LocalDate toDate,

        BigDecimal totalExpense,

        Long totalTransactions,

        List<ExpenseHeadItem> items
) {}
