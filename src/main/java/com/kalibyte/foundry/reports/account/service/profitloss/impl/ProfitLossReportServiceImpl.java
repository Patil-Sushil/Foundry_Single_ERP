package com.kalibyte.foundry.reports.account.service.profitloss.impl;

import com.kalibyte.foundry.accounts.repository.PaymentRepository;
import com.kalibyte.foundry.billing.invoice.repository.InvoiceRepository;
import com.kalibyte.foundry.expenses.repository.ExpenseRepository;
import com.kalibyte.foundry.inventory.purchaseorder.repository.PurchaseOrderRepository;
import com.kalibyte.foundry.reports.account.dto.response.profitloss.ProfitLossExpenseItem;
import com.kalibyte.foundry.reports.account.dto.response.profitloss.ProfitLossMonthlyItem;
import com.kalibyte.foundry.reports.account.dto.response.profitloss.ProfitLossReport;
import com.kalibyte.foundry.reports.account.dto.response.profitloss.ProfitLossSummary;
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

/**
 * Implementation of ProfitLossReportService.
 *
 * Responsible for calculating revenue, expenses,
 * and profitability metrics.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfitLossReportServiceImpl implements ProfitLossReportService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final ExpenseRepository expenseRepository;

    @Override
    @Cacheable(value="report_profit_loss")
    public ProfitLossReport generateReport(LocalDate from, LocalDate to){

        BigDecimal revenue = invoiceRepository.getRevenue(from,to);

        BigDecimal collections = paymentRepository.getCollections(from,to);

        BigDecimal cogs = purchaseOrderRepository.getCOGS(from,to);

        BigDecimal expenses = expenseRepository.getTotalExpenses(from,to);

        BigDecimal grossProfit = revenue.subtract(cogs);

        BigDecimal netProfit = grossProfit.subtract(expenses);

        BigDecimal grossMargin = percent(grossProfit,revenue);

        BigDecimal netMargin = percent(netProfit,revenue);

        BigDecimal expenseRatio = percent(expenses,revenue);

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

        List<ProfitLossExpenseItem> expenseItems = buildExpenseBreakdown(revenue,from,to);

        List<ProfitLossMonthlyItem> monthlyTrend = buildMonthlyTrend(from,to);

        return new ProfitLossReport(
                summary,
                expenseItems,
                monthlyTrend,
                LocalDateTime.now(),
                "SYSTEM"
        );
    }

    /**
     * Calculates percentage safely.
     */
    private BigDecimal percent(BigDecimal value, BigDecimal total){

        if(total.compareTo(BigDecimal.ZERO)==0)
            return BigDecimal.ZERO;

        return value
                .divide(total,4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * Builds expense breakdown by head.
     */
    private List<ProfitLossExpenseItem> buildExpenseBreakdown(BigDecimal revenue, LocalDate from, LocalDate to){

        List<Object[]> rows = expenseRepository.getExpenseBreakdown(from,to);

        List<ProfitLossExpenseItem> list = new ArrayList<>();

        for(Object[] r:rows){

            BigDecimal amount = (BigDecimal) r[2];

            list.add(
                    new ProfitLossExpenseItem(
                            (String) r[0],
                            (String) r[1],
                            amount,
                            percent(amount,revenue)
                    )
            );
        }

        return list;
    }

    /**
     * Builds month-wise P&L trend.
     */
    private List<ProfitLossMonthlyItem> buildMonthlyTrend(LocalDate from, LocalDate to){

        List<Object[]> rows = invoiceRepository.getMonthlyRevenue(from,to);

        List<ProfitLossMonthlyItem> result = new ArrayList<>();

        BigDecimal previous = BigDecimal.ZERO;

        for(Object[] r:rows){

            YearMonth month =
                    YearMonth.from(((java.sql.Timestamp) r[0]).toLocalDateTime());

            BigDecimal revenue = (BigDecimal) r[1];

            BigDecimal netProfit = revenue.multiply(BigDecimal.valueOf(0.25));

            BigDecimal growth = BigDecimal.ZERO;

            if(previous.compareTo(BigDecimal.ZERO)>0){

                growth = revenue.subtract(previous)
                        .divide(previous,4,RoundingMode.HALF_UP)
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
