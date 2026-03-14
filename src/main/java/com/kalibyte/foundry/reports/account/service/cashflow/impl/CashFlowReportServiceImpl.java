package com.kalibyte.foundry.reports.account.service.cashflow.impl;

import com.kalibyte.foundry.accounts.repository.PaymentRepository;
import com.kalibyte.foundry.expenses.repository.ExpenseRepository;
import com.kalibyte.foundry.reports.account.dto.response.cashflow.CashFlowItem;
import com.kalibyte.foundry.reports.account.dto.response.cashflow.CashFlowReport;
import com.kalibyte.foundry.reports.account.service.cashflow.CashFlowReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CashFlowReportServiceImpl implements CashFlowReportService {

    private final PaymentRepository paymentRepository;
    private final ExpenseRepository expenseRepository;

    @Override
    public CashFlowReport getCashFlow(LocalDate from, LocalDate to) {

        List<Object[]> inflowRows =
                paymentRepository.getDailyCashInflow(from, to);

        List<Object[]> outflowRows =
                expenseRepository.getDailyCashOutflow(from, to);

        Map<LocalDate, BigDecimal> inflowMap =
                inflowRows.stream().collect(Collectors.toMap(
                        r -> (LocalDate) r[0],
                        r -> (BigDecimal) r[1]
                ));

        Map<LocalDate, BigDecimal> outflowMap =
                outflowRows.stream().collect(Collectors.toMap(
                        r -> (LocalDate) r[0],
                        r -> (BigDecimal) r[1]
                ));

        List<CashFlowItem> items = new ArrayList<>();

        LocalDate date = from;

        while (!date.isAfter(to)) {

            BigDecimal inflow = inflowMap.getOrDefault(date, BigDecimal.ZERO);
            BigDecimal outflow = outflowMap.getOrDefault(date, BigDecimal.ZERO);

            items.add(
                    CashFlowItem.builder()
                            .date(date)
                            .inflow(inflow)
                            .outflow(outflow)
                            .netFlow(inflow.subtract(outflow))
                            .build()
            );

            date = date.plusDays(1);
        }

        BigDecimal totalInflow =
                items.stream().map(CashFlowItem::inflow)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalOutflow =
                items.stream().map(CashFlowItem::outflow)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CashFlowReport.builder()
                .fromDate(from)
                .toDate(to)
                .totalInflow(totalInflow)
                .totalOutflow(totalOutflow)
                .netCashFlow(totalInflow.subtract(totalOutflow))
                .dailyCashFlow(items)
                .build();
    }
}
