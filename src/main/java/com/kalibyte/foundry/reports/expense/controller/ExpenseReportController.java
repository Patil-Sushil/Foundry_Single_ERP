package com.kalibyte.foundry.reports.expense.controller;

import com.kalibyte.foundry.common.response.ApiResponse;
import com.kalibyte.foundry.reports.expense.dto.response.expensebycategory.ExpenseCategoryReport;
import com.kalibyte.foundry.reports.expense.dto.response.expensebyhead.ExpenseHeadReport;
import com.kalibyte.foundry.reports.expense.dto.response.revenue.RevenueReport;
import com.kalibyte.foundry.reports.expense.service.expensebycategory.ExpenseCategoryReportService;
import com.kalibyte.foundry.reports.expense.service.expensebyhead.ExpenseHeadReportService;
import com.kalibyte.foundry.reports.expense.service.revenue.RevenueReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Controller for Expense Reports.
 */
@Slf4j
@RestController
@RequestMapping("/api/reports/expenses")
@RequiredArgsConstructor
public class ExpenseReportController {

    private final ExpenseHeadReportService expenseHeadReportService;

    private final ExpenseCategoryReportService expenseCategoryReportService;

    private final RevenueReportService revenueReportService;

    /**
     * Expense by Head Report

     * Example:
     * /api/reports/expenses/by-head?from=2026-03-01&to=2026-03-31
     */
    @GetMapping("/by-head")
    public ApiResponse<ExpenseHeadReport> expenseByHead(

            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ) {

        return ApiResponse.success(
                expenseHeadReportService.generate(from, to)
        );
    }

    /**
     * Expense by Category Report
     */
    @GetMapping("/by-category")
    public ApiResponse<ExpenseCategoryReport> expenseByCategory(

            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ) {

        return ApiResponse.success(
                expenseCategoryReportService.generate(from, to)
        );
    }

    @GetMapping("/revenue")
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT','MANAGER')")
    public ApiResponse<RevenueReport> revenueReport(

            @RequestParam LocalDate from,
            @RequestParam LocalDate to,
            @RequestParam(required = false) UUID customerId

    ){

        RevenueReport report =
                revenueReportService.generateRevenueReport(from,to,customerId);

        return ApiResponse.success(report);
    }



}
