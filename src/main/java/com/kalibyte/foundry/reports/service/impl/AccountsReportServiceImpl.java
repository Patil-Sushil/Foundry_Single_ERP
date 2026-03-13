package com.kalibyte.foundry.reports.service.impl;

import com.kalibyte.foundry.accounts.repository.PaymentRepository;
import com.kalibyte.foundry.reports.dto.response.accounts.DailyCollectionItem;
import com.kalibyte.foundry.reports.dto.response.accounts.DailyCollectionReport;
import com.kalibyte.foundry.reports.service.AccountsReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountsReportServiceImpl implements AccountsReportService {

    private final PaymentRepository paymentRepository;

    @Override
    public DailyCollectionReport getDailyCollection(LocalDate from, LocalDate to) {

        List<Object[]> rows =
                paymentRepository.getDailyCollection(from, to);

        List<DailyCollectionItem> items = rows.stream()
                .map(r -> DailyCollectionItem.builder()
                        .date((LocalDate) r[0])
                        .totalAmount((BigDecimal) r[1])
                        .transactionCount(((Number) r[2]).longValue())
                        .cashAmount((BigDecimal) r[3])
                        .upiAmount((BigDecimal) r[4])
                        .bankTransferAmount((BigDecimal) r[5])
                        .chequeAmount((BigDecimal) r[6])
                        .cardAmount((BigDecimal) r[7])
                        .build())
                .toList();

        //------------------------------------------------
        // TOTAL COLLECTION
        //------------------------------------------------

        BigDecimal totalAmount = items.stream()
                .map(DailyCollectionItem::totalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        //------------------------------------------------
        // TOTAL TRANSACTIONS
        //------------------------------------------------

        long totalTransactions = items.stream()
                .mapToLong(DailyCollectionItem::transactionCount)
                .sum();

        //------------------------------------------------
        // BUILD REPORT
        //------------------------------------------------

        return DailyCollectionReport.builder()
                .fromDate(from)
                .toDate(to)
                .totalCollection(totalAmount)
                .totalTransactions(totalTransactions)
                .dailyBreakdown(items)
                .build();
    }
}