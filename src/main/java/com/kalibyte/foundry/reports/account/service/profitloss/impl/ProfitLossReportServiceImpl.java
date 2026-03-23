package com.kalibyte.foundry.reports.account.service.profitloss.impl;

import com.kalibyte.foundry.accounts.repository.PaymentRepository;
import com.kalibyte.foundry.billing.invoice.repository.InvoiceRepository;
import com.kalibyte.foundry.expenses.entity.enums.ExpenseCategory;
import com.kalibyte.foundry.expenses.repository.ExpenseRepository;
import com.kalibyte.foundry.inventory.purchaseorder.repository.PurchaseOrderRepository;
import com.kalibyte.foundry.reports.account.dto.response.profitloss.*;
import com.kalibyte.foundry.reports.account.service.profitloss.ProfitLossReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfitLossReportServiceImpl implements ProfitLossReportService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final ExpenseRepository expenseRepository;

    //--------------------------------------------------
    // MAIN REPORT
    //--------------------------------------------------
    @Override
    @Cacheable(value = "report_profit_loss")
    public ProfitLossReport generateReport(LocalDate from, LocalDate to) {

        BigDecimal revenue = safe(invoiceRepository.getRevenue(from, to));
        BigDecimal collections = safe(paymentRepository.getCollections(from, to));
        BigDecimal cogs = safe(purchaseOrderRepository.getCOGS(from, to));
        BigDecimal expenses = safe(expenseRepository.getTotalExpenses(from, to));

        BigDecimal grossProfit = revenue.subtract(cogs);
        BigDecimal netProfit = grossProfit.subtract(expenses);

        BigDecimal grossMargin = percent(grossProfit, revenue);
        BigDecimal netMargin = percent(netProfit, revenue);
        BigDecimal expenseRatio = percent(expenses, revenue);

        ProfitLossSummary summary = new ProfitLossSummary(
                revenue,
                collections,
                cogs,
                grossProfit,
                expenses,
                netProfit,
                grossMargin,
                netMargin,
                expenseRatio
        );

        List<ProfitLossExpenseItem> expenseItems =
                buildExpenseBreakdown(revenue, from, to);

        List<ProfitLossMonthlyItem> monthlyTrend =
                buildMonthlyTrend(from, to);

        return new ProfitLossReport(
                summary,
                expenseItems,
                monthlyTrend,
                LocalDateTime.now(),
                "SYSTEM"
        );
    }

    //--------------------------------------------------
    // SAFE NULL HANDLING
    //--------------------------------------------------
    private BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    //--------------------------------------------------
    // PERCENT CALCULATION
    //--------------------------------------------------
    private BigDecimal percent(BigDecimal value, BigDecimal total) {

        if (total == null || total.compareTo(BigDecimal.ZERO) == 0)
            return BigDecimal.ZERO;

        return value
                .divide(total, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    //--------------------------------------------------
    // EXPENSE BREAKDOWN
    //--------------------------------------------------
    private List<ProfitLossExpenseItem> buildExpenseBreakdown(
            BigDecimal revenue,
            LocalDate from,
            LocalDate to) {

        List<Object[]> rows = expenseRepository.getExpenseBreakdown(from, to);

        List<ProfitLossExpenseItem> list = new ArrayList<>();

        for (Object[] r : rows) {

            Object categoryObj = r[0];

            String category;

            // HANDLE BOTH CASES (String OR Enum)
            if (categoryObj instanceof String) {
                category = (String) categoryObj;
            } else if (categoryObj instanceof ExpenseCategory) {
                category = ((ExpenseCategory) categoryObj).name();
            } else {
                category = "UNKNOWN";
            }

            String subCategory = r[1] != null ? r[1].toString() : "-";
            BigDecimal amount = safe((BigDecimal) r[2]);

            list.add(
                    new ProfitLossExpenseItem(
                            category,
                            subCategory,
                            amount,
                            percent(amount, revenue)
                    )
            );
        }

        return list;
    }

    //--------------------------------------------------
    // MONTHLY TREND
    //--------------------------------------------------
    private List<ProfitLossMonthlyItem> buildMonthlyTrend(
            LocalDate from,
            LocalDate to) {

        List<Object[]> rows = invoiceRepository.getMonthlyRevenue(from, to);

        List<ProfitLossMonthlyItem> result = new ArrayList<>();

        BigDecimal previous = BigDecimal.ZERO;

        for (Object[] r : rows) {

            Object dateObj = r[0];
            YearMonth month;

            // HANDLE ALL POSSIBLE TYPES
            if (dateObj instanceof java.sql.Timestamp ts) {
                month = YearMonth.from(ts.toLocalDateTime());
            } else if (dateObj instanceof LocalDate ld) {
                month = YearMonth.from(ld);
            } else if (dateObj instanceof LocalDateTime ldt) {
                month = YearMonth.from(ldt);
            } else {
                throw new RuntimeException("Unsupported date type: " + dateObj.getClass());
            }

            BigDecimal revenue = safe((BigDecimal) r[1]);

            // temporary logic
            BigDecimal netProfit = revenue.multiply(BigDecimal.valueOf(0.25));

            BigDecimal growth = BigDecimal.ZERO;

            if (previous.compareTo(BigDecimal.ZERO) > 0) {
                growth = revenue.subtract(previous)
                        .divide(previous, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
            }

            result.add(
                    new ProfitLossMonthlyItem(
                            month,
                            revenue,
                            BigDecimal.ZERO,
                            BigDecimal.ZERO,
                            netProfit,
                            growth
                    )
            );

            previous = revenue;
        }

        return result;
    }
}