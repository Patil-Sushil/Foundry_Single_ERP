package com.kalibyte.foundry.reports.account.service.overdueinvoice.impl;

import com.kalibyte.foundry.billing.invoice.entity.Invoice;
import com.kalibyte.foundry.billing.invoice.repository.InvoiceRepository;
import com.kalibyte.foundry.reports.account.dto.response.overdueinvoice.OverdueCustomerGroup;
import com.kalibyte.foundry.reports.account.dto.response.overdueinvoice.OverdueInvoiceItem;
import com.kalibyte.foundry.reports.account.dto.response.overdueinvoice.OverdueInvoiceReport;
import com.kalibyte.foundry.reports.account.dto.response.overdueinvoice.OverdueSummary;
import com.kalibyte.foundry.reports.account.dto.response.overdueinvoice.enums.OverdueSeverity;
import com.kalibyte.foundry.reports.account.service.overdueinvoice.OverdueInvoiceReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of OverdueInvoiceReportService.
 *
 * Generates the complete overdue invoice analytics report.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OverdueInvoiceReportServiceImpl implements OverdueInvoiceReportService {

    private final InvoiceRepository invoiceRepository;

    @Override
    @Cacheable(value = "report_overdue")
    public OverdueInvoiceReport generateReport(
            UUID customerId,
            OverdueSeverity severity,
            BigDecimal minAmount,
            int page,
            int size
    ) {

        Page<Invoice> invoicesPage =
                invoiceRepository.findOverdueInvoices(PageRequest.of(page, size));

        List<OverdueInvoiceItem> items = new ArrayList<>();

        BigDecimal totalOverdue = BigDecimal.ZERO;

        BigDecimal bucket1 = BigDecimal.ZERO;
        BigDecimal bucket2 = BigDecimal.ZERO;
        BigDecimal bucket3 = BigDecimal.ZERO;
        BigDecimal bucket4 = BigDecimal.ZERO;

        long totalDays = 0;

        for (Invoice invoice : invoicesPage.getContent()) {

            long daysOverdue =
                    ChronoUnit.DAYS.between(
                            invoice.getDueDate(),
                            LocalDate.now()
                    );

            BigDecimal paid =
                    invoice.getTotalAmount().subtract(invoice.getTotalAmount());

            BigDecimal balance =
                    invoice.getTotalAmount().subtract(paid);

            OverdueSeverity level = classify(daysOverdue);

            items.add(
                    new OverdueInvoiceItem(
                            invoice.getInvoiceNumber(),
                            invoice.getInvoiceDate(),
                            invoice.getDueDate(),
                            daysOverdue,
                            invoice.getCustomer().getName(),
                            invoice.getCustomer().getPhone(),
                            invoice.getTotalAmount(),
                            paid,
                            balance,
                            level
                    )
            );

            totalOverdue = totalOverdue.add(balance);

            totalDays += daysOverdue;

            if(daysOverdue<=30) bucket1 = bucket1.add(balance);
            else if(daysOverdue<=60) bucket2 = bucket2.add(balance);
            else if(daysOverdue<=90) bucket3 = bucket3.add(balance);
            else bucket4 = bucket4.add(balance);
        }

        BigDecimal avgDays =
                invoicesPage.getTotalElements()==0
                        ? BigDecimal.ZERO
                        : BigDecimal.valueOf(totalDays)
                        .divide(BigDecimal.valueOf(invoicesPage.getTotalElements()));

        OverdueSummary summary = new OverdueSummary(
                totalOverdue,
                invoicesPage.getTotalElements(),
                bucket1,
                bucket2,
                bucket3,
                bucket4,
                avgDays
        );

        List<OverdueCustomerGroup> customers =
                buildCustomerGrouping();

        return new OverdueInvoiceReport(
                summary,
                items,
                customers,
                LocalDateTime.now(),
                "SYSTEM"
        );
    }

    /**
     * Determines severity level based on days overdue.
     */
    private OverdueSeverity classify(long days){

        if(days<=30) return OverdueSeverity.LOW;
        if(days<=60) return OverdueSeverity.MEDIUM;
        if(days<=90) return OverdueSeverity.HIGH;

        return OverdueSeverity.CRITICAL;
    }

    /**
     * Builds overdue totals grouped by customer.
     */
    private List<OverdueCustomerGroup> buildCustomerGrouping(){

        List<Object[]> rows =
                invoiceRepository.getCustomerOverdueSummary();

        List<OverdueCustomerGroup> result = new ArrayList<>();

        for(Object[] r:rows){

            result.add(
                    new OverdueCustomerGroup(
                            (String) r[0],
                            (BigDecimal) r[1],
                            ((Number) r[2]).longValue(),
                            (LocalDate) r[3]
                    )
            );
        }

        return result;
    }
}
