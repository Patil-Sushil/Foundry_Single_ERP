package com.kalibyte.foundry.reports.account.service.dailycollection.impl;


import com.kalibyte.foundry.accounts.repository.PaymentRepository;
import com.kalibyte.foundry.reports.account.dto.response.dailycollection.DailyCollectionItem;
import com.kalibyte.foundry.reports.account.dto.response.dailycollection.DailyCollectionReport;
import com.kalibyte.foundry.reports.account.service.dailycollection.DailyCollectionReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Implementation of DailyCollectionReportService.
 */
@Service
@RequiredArgsConstructor
public class DailyCollectionReportServiceImpl implements DailyCollectionReportService {

    private final PaymentRepository paymentRepository;

    /**
     * Fetch daily payment collection from database
     * and convert it into report DTO.
     */
    @Override
    public DailyCollectionReport getDailyCollection(LocalDate from, LocalDate to) {

        List<Object[]> rows = paymentRepository.getDailyCollection(from, to);

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

        BigDecimal totalCollection = items.stream()
                .map(DailyCollectionItem::totalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalTransactions = items.stream()
                .mapToLong(DailyCollectionItem::transactionCount)
                .sum();

        return DailyCollectionReport.builder()
                .fromDate(from)
                .toDate(to)
                .totalCollection(totalCollection)
                .totalTransactions(totalTransactions)
                .dailyBreakdown(items)
                .build();
    }
}
