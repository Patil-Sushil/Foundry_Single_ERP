package com.kalibyte.foundry.reports.account.service.aging.impl;

import com.kalibyte.foundry.billing.invoice.repository.InvoiceRepository;
import com.kalibyte.foundry.reports.account.dto.response.aging.AgingCustomerItem;
import com.kalibyte.foundry.reports.account.dto.response.aging.AgingReport;
import com.kalibyte.foundry.reports.account.dto.response.aging.AgingSummary;
import com.kalibyte.foundry.reports.account.service.aging.AgingReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AgingReportServiceImpl implements AgingReportService {

    private final InvoiceRepository invoiceRepository;

    @Override
    public AgingReport getReceivablesAging(LocalDate asOfDate) {

        List<Object[]> rows = invoiceRepository.getReceivableAging();

        List<AgingCustomerItem> customers = rows.stream()
                .map(r -> {

                    BigDecimal current = (BigDecimal) r[2];
                    BigDecimal d30 = (BigDecimal) r[3];
                    BigDecimal d60 = (BigDecimal) r[4];
                    BigDecimal d90 = (BigDecimal) r[5];
                    BigDecimal d90plus = (BigDecimal) r[6];

                    BigDecimal total = current.add(d30).add(d60).add(d90).add(d90plus);

                    return AgingCustomerItem.builder()
                            .customerId((UUID) r[0])
                            .customerName((String) r[1])
                            .current(current)
                            .days1to30(d30)
                            .days31to60(d60)
                            .days61to90(d90)
                            .days90plus(d90plus)
                            .total(total)
                            .build();
                })
                .toList();

        BigDecimal totalOutstanding =
                customers.stream()
                        .map(AgingCustomerItem::total)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        AgingSummary summary = AgingSummary.builder()
                .current(customers.stream().map(AgingCustomerItem::current).reduce(BigDecimal.ZERO, BigDecimal::add))
                .days1to30(customers.stream().map(AgingCustomerItem::days1to30).reduce(BigDecimal.ZERO, BigDecimal::add))
                .days31to60(customers.stream().map(AgingCustomerItem::days31to60).reduce(BigDecimal.ZERO, BigDecimal::add))
                .days61to90(customers.stream().map(AgingCustomerItem::days61to90).reduce(BigDecimal.ZERO, BigDecimal::add))
                .days90plus(customers.stream().map(AgingCustomerItem::days90plus).reduce(BigDecimal.ZERO, BigDecimal::add))
                .build();

        return AgingReport.builder()
                .asOfDate(asOfDate)
                .totalOutstanding(totalOutstanding)
                .summary(summary)
                .customers(customers)
                .build();
    }
}