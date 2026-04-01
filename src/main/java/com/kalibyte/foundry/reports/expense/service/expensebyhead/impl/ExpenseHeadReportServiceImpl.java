package com.kalibyte.foundry.reports.expense.service.expensebyhead.impl;

import com.kalibyte.foundry.expenses.repository.ExpenseRepository;
import com.kalibyte.foundry.reports.expense.dto.response.expensebyhead.ExpenseHeadItem;
import com.kalibyte.foundry.reports.expense.dto.response.expensebyhead.ExpenseHeadReport;
import com.kalibyte.foundry.reports.expense.service.expensebyhead.ExpenseHeadReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of ExpenseHeadReportService.
 *
 * Responsible for generating expense report grouped by expense heads.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExpenseHeadReportServiceImpl implements ExpenseHeadReportService {

    private final ExpenseRepository expenseRepository;

    /**
     * Generates expense report grouped by expense head.
     */
    @Override
    public ExpenseHeadReport generate(LocalDate from, LocalDate to) {

        //---------------------------------------------
        // FETCH DATA FROM DATABASE
        //---------------------------------------------

        List<Object[]> rows =
                expenseRepository.getExpenseByHead(from, to);

        //---------------------------------------------
        // MAP DATABASE RESULT TO DTO
        //---------------------------------------------

        List<ExpenseHeadItem> items = rows.stream()
                .map(row -> ExpenseHeadItem.builder()
                        .expenseHeadId((UUID) row[0])
                        .expenseHeadName((String) row[1])
                        .totalAmount((BigDecimal) row[2])
                        .transactionCount(((Number) row[3]).longValue())
                        .build())
                .toList();

        //---------------------------------------------
        // CALCULATE TOTAL EXPENSE
        //---------------------------------------------

        BigDecimal totalExpense = items.stream()
                .map(ExpenseHeadItem::totalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        //---------------------------------------------
        // CALCULATE TOTAL TRANSACTIONS
        //---------------------------------------------

        long totalTransactions = items.stream()
                .mapToLong(ExpenseHeadItem::transactionCount)
                .sum();

        //---------------------------------------------
        // BUILD REPORT OBJECT
        //---------------------------------------------

        return ExpenseHeadReport.builder()
                .fromDate(from)
                .toDate(to)
                .totalExpense(totalExpense)
                .totalTransactions(totalTransactions)
                .items(items)
                .build();
    }
}