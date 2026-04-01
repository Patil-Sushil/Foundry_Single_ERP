package com.kalibyte.foundry.reports.expense.dto.response.expensebycategory;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Expense By Category Report
 *
 * Shows expense distribution across categories
 * like Operational, Maintenance, Admin etc.
 */
@Builder
public record ExpenseCategoryReport(

        LocalDate fromDate,

        LocalDate toDate,

        BigDecimal totalExpense,

        Long totalTransactions,

        List<ExpenseCategoryItem> items
) {}
