package com.kalibyte.foundry.reports.account.service.outstanding.impl;

import com.kalibyte.foundry.billing.invoice.repository.InvoiceRepository;
import com.kalibyte.foundry.payment.repository.PaymentRepository;
import com.kalibyte.foundry.reports.account.dto.response.outstanding.CustomerOutstandingItem;
import com.kalibyte.foundry.reports.account.dto.response.outstanding.CustomerOutstandingReport;
import com.kalibyte.foundry.reports.account.service.outstanding.CustomerOutstandingReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation for Customer Outstanding Report.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerOutstandingReportServiceImpl implements CustomerOutstandingReportService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;

    @Override
    public CustomerOutstandingReport getCustomerOutstanding(LocalDate asOfDate) {

        List<Object[]> invoiceRows = invoiceRepository.getCustomerInvoiceTotals();
        List<Object[]> paymentRows = paymentRepository.getCustomerPayments();

        Map<UUID, BigDecimal> paymentMap =
                paymentRows.stream().collect(Collectors.toMap(
                        r -> (UUID) r[0],
                        r -> (BigDecimal) r[1]
                ));

        Map<UUID, LocalDate> lastPaymentMap =
                paymentRows.stream().collect(Collectors.toMap(
                        r -> (UUID) r[0],
                        r -> (LocalDate) r[2]
                ));

        Map<UUID, LocalDate> oldestUnpaidMap =
                invoiceRepository.getOldestUnpaidInvoices()
                        .stream()
                        .collect(Collectors.toMap(
                                r -> (UUID) r[0],
                                r -> (LocalDate) r[1]
                        ));

        List<CustomerOutstandingItem> items =
                invoiceRows.stream()
                        .map(r -> {

                            UUID customerId = (UUID) r[0];
                            String name = (String) r[1];
                            String company = (String) r[2];
                            BigDecimal invoiced = (BigDecimal) r[3];

                            BigDecimal paid =
                                    paymentMap.getOrDefault(customerId, BigDecimal.ZERO);

                            BigDecimal outstanding =
                                    invoiced.subtract(paid);

                            if (outstanding.compareTo(BigDecimal.ZERO) <= 0)
                                return null;

                            return CustomerOutstandingItem.builder()
                                    .customerId(customerId)
                                    .customerName(name)
                                    .companyName(company)
                                    .totalInvoiced(invoiced)
                                    .totalPaid(paid)
                                    .outstanding(outstanding)
                                    .lastPaymentDate(lastPaymentMap.get(customerId))
                                    .oldestUnpaidInvoiceDate(oldestUnpaidMap.get(customerId))
                                    .build();
                        })
                        .filter(Objects::nonNull)
                        .sorted(Comparator.comparing(CustomerOutstandingItem::outstanding).reversed())
                        .toList();

        BigDecimal totalOutstanding =
                items.stream()
                        .map(CustomerOutstandingItem::outstanding)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CustomerOutstandingReport.builder()
                .asOfDate(asOfDate)
                .totalOutstanding(totalOutstanding)
                .customerCount((long) items.size())
                .customers(items)
                .build();
    }
}
