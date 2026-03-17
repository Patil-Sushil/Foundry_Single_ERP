package com.kalibyte.foundry.reports.expense.service.expensebycategory;

import com.kalibyte.foundry.reports.expense.dto.response.expensebycategory.ExpenseCategoryReport;

import java.time.LocalDate;

/**
 * Service for generating Expense by Category report.
 */
public interface ExpenseCategoryReportService {

    /**
     * Generates expense summary grouped by category.
     *
     * @param from start date
     * @param to end date
     */
    ExpenseCategoryReport generate(LocalDate from, LocalDate to);

}
