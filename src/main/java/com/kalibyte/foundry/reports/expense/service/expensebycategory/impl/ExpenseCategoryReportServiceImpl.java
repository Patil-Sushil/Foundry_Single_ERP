package com.kalibyte.foundry.reports.expense.service.expensebycategory.impl;

import com.kalibyte.foundry.expenses.entity.enums.ExpenseCategory;
import com.kalibyte.foundry.expenses.repository.ExpenseRepository;
import com.kalibyte.foundry.reports.expense.dto.response.expensebycategory.ExpenseCategoryItem;
import com.kalibyte.foundry.reports.expense.dto.response.expensebycategory.ExpenseCategoryReport;
import com.kalibyte.foundry.reports.expense.service.expensebycategory.ExpenseCategoryReportService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Implementation of ExpenseCategoryReportService.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExpenseCategoryReportServiceImpl implements ExpenseCategoryReportService {

    private final ExpenseRepository expenseRepository;

    @Override
    public ExpenseCategoryReport generate(LocalDate from, LocalDate to) {

        //-----------------------------------------
        // FETCH DATA FROM DATABASE
        //-----------------------------------------

        List<Object[]> rows =
                expenseRepository.getExpenseByCategory(from, to);

        //-----------------------------------------
        // MAP DATABASE RESULT
        //-----------------------------------------

        List<ExpenseCategoryItem> items = rows.stream()
                .map(row -> ExpenseCategoryItem.builder()
                        .category((ExpenseCategory) row[0])
                        .totalAmount((BigDecimal) row[1])
                        .transactionCount(((Number) row[2]).longValue())
                        .build())
                .toList();

        //-----------------------------------------
        // CALCULATE TOTAL EXPENSE
        //-----------------------------------------

        BigDecimal totalExpense = items.stream()
                .map(ExpenseCategoryItem::totalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        //-----------------------------------------
        // CALCULATE TOTAL TRANSACTIONS
        //-----------------------------------------

        long totalTransactions = items.stream()
                .mapToLong(ExpenseCategoryItem::transactionCount)
                .sum();

        //-----------------------------------------
        // BUILD REPORT
        //-----------------------------------------

        return ExpenseCategoryReport.builder()
                .fromDate(from)
                .toDate(to)
                .totalExpense(totalExpense)
                .totalTransactions(totalTransactions)
                .items(items)
                .build();
    }
}