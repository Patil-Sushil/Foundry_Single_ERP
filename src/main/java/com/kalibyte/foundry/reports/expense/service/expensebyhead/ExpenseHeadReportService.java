package com.kalibyte.foundry.reports.expense.service.expensebyhead;

import com.kalibyte.foundry.reports.expense.dto.response.expensebyhead.ExpenseHeadReport;

import java.time.LocalDate;

/**
 * Service for generating Expense by Head report.
 */
public interface ExpenseHeadReportService {

    /**
     * Generates expense summary grouped by expense head.
     *
     * @param from start date
     * @param to   end date
     * @return ExpenseHeadReport
     */
    ExpenseHeadReport generate(LocalDate from, LocalDate to);

}
