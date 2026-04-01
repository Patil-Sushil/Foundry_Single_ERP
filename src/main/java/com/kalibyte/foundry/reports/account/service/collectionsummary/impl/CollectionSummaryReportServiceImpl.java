package com.kalibyte.foundry.reports.account.service.collectionsummary.impl;

import com.kalibyte.foundry.payment.repository.PaymentRepository;
import com.kalibyte.foundry.reports.account.dto.response.collectionsummary.CollectionSummaryReport;
import com.kalibyte.foundry.reports.account.dto.response.collectionsummary.PaymentMethodSummary;
import com.kalibyte.foundry.reports.account.dto.response.collectionsummary.TopCustomerCollection;
import com.kalibyte.foundry.reports.account.service.collectionsummary.CollectionSummaryReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of CollectionSummaryReportService.
 */
@Service
@RequiredArgsConstructor
public class CollectionSummaryReportServiceImpl implements CollectionSummaryReportService {

    private final PaymentRepository paymentRepository;

    @Override
    public CollectionSummaryReport getCollectionSummary(LocalDate from, LocalDate to) {

        BigDecimal current = paymentRepository.getTotalCollection(from, to);

        long days = java.time.temporal.ChronoUnit.DAYS.between(from, to);

        LocalDate previousFrom = from.minusDays(days + 1);
        LocalDate previousTo = from.minusDays(1);

        BigDecimal previous = paymentRepository.getTotalCollection(previousFrom, previousTo);

        double growth = 0;

        if (previous.compareTo(BigDecimal.ZERO) > 0) {
            growth = current.subtract(previous)
                    .divide(previous, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        }

        List<Object[]> methodRows = paymentRepository.getMethodWiseCollection(from, to);

        List<PaymentMethodSummary> methods = methodRows.stream()
                .map(r -> PaymentMethodSummary.builder()
                        .method(r[0].toString())
                        .amount((BigDecimal) r[1])
                        .count(((Number) r[2]).longValue())
                        .build())
                .toList();

        List<Object[]> customerRows = paymentRepository.getTopCustomers(from, to);

        List<TopCustomerCollection> customers = customerRows.stream()
                .limit(5)
                .map(r -> TopCustomerCollection.builder()
                        .customerId((UUID) r[0])
                        .customerName((String) r[1])
                        .totalPaid((BigDecimal) r[2])
                        .build())
                .toList();

        String period = from.getMonth() + " " + from.getYear();

        return CollectionSummaryReport.builder()
                .period(period)
                .totalCollection(current)
                .previousPeriodCollection(previous)
                .growthPercentage(growth)
                .methodWiseBreakdown(methods)
                .topCustomers(customers)
                .build();
    }
}