package com.kalibyte.foundry.reports.expense.service.revenue.impl;

import com.kalibyte.foundry.payment.repository.PaymentRepository;
import com.kalibyte.foundry.billing.invoice.repository.InvoiceRepository;
import com.kalibyte.foundry.reports.expense.dto.response.revenue.RevenueMonthlyItem;
import com.kalibyte.foundry.reports.expense.dto.response.revenue.RevenueReport;
import com.kalibyte.foundry.reports.expense.dto.response.revenue.RevenueTopCustomerItem;
import com.kalibyte.foundry.reports.expense.service.revenue.RevenueReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;

/**
 * Implementation of RevenueReportService.

 * Responsible for generating revenue analytics based on
 * invoice and payment data.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RevenueReportServiceImpl implements RevenueReportService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;

    /**
     * Generates the Revenue Report for the given date range.
     */
    @Override
    @Cacheable(value = "report_revenue", key = "#from + '_' + #to")
    public RevenueReport generateRevenueReport(LocalDate from, LocalDate to, UUID customerId) {

        if (from.isAfter(to)) {
            throw new IllegalArgumentException("Invalid date range");
        }

        BigDecimal totalRevenue = invoiceRepository.getTotalRevenue(from, to);

        BigDecimal totalCollected = paymentRepository.getTotalCollections(from, to);

        BigDecimal outstanding = totalRevenue.subtract(totalCollected);

        BigDecimal efficiency = BigDecimal.ZERO;

        if (totalRevenue.compareTo(BigDecimal.ZERO) > 0) {
            efficiency = totalCollected
                    .divide(totalRevenue, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        Long invoiceCount = invoiceRepository.getInvoiceCount(from, to);

        BigDecimal avgInvoice =
                invoiceCount == 0
                        ? BigDecimal.ZERO
                        : totalRevenue.divide(
                        BigDecimal.valueOf(invoiceCount),
                        2,
                        RoundingMode.HALF_UP
                );

        List<RevenueMonthlyItem> months = buildMonthlyBreakdown(from, to);

        List<RevenueTopCustomerItem> customers =
                buildTopCustomers(totalRevenue, from, to);

        return new RevenueReport(
                totalRevenue,
                totalCollected,
                outstanding,
                efficiency,
                avgInvoice,
                BigDecimal.ZERO,
                months,
                customers,
                LocalDateTime.now(),
                "SYSTEM"
        );
    }

    /**
     * Builds month-wise revenue statistics.
     */
    private List<RevenueMonthlyItem> buildMonthlyBreakdown(LocalDate from, LocalDate to) {

        List<Object[]> invoiceRows = invoiceRepository.getMonthlyInvoiceStats(from, to);
        List<Object[]> paymentRows = paymentRepository.getMonthlyCollections(from, to);

        Map<YearMonth, BigDecimal> payments = new HashMap<>();

        //--------------------------------------------------
        // PAYMENTS LOOP (FIXED)
        //--------------------------------------------------
        for (Object[] row : paymentRows) {

            YearMonth month = extractYearMonth(row[0]); // ✅ FIX

            payments.put(month, (BigDecimal) row[1]);
        }

        //--------------------------------------------------
        // INVOICE LOOP (FIXED)
        //--------------------------------------------------
        List<RevenueMonthlyItem> result = new ArrayList<>();

        BigDecimal previousRevenue = BigDecimal.ZERO;

        for (Object[] row : invoiceRows) {

            YearMonth month = extractYearMonth(row[0]); // ✅ FIX

            BigDecimal invoiced = (BigDecimal) row[1];
            Long invoiceCount = ((Number) row[2]).longValue();

            BigDecimal collected = payments.getOrDefault(month, BigDecimal.ZERO);

            BigDecimal outstanding = invoiced.subtract(collected);

            BigDecimal growth = BigDecimal.ZERO;

            if (previousRevenue.compareTo(BigDecimal.ZERO) > 0) {
                growth = invoiced.subtract(previousRevenue)
                        .divide(previousRevenue, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
            }

            result.add(
                    new RevenueMonthlyItem(
                            month,
                            invoiced,
                            collected,
                            outstanding,
                            invoiceCount,
                            growth
                    )
            );

            previousRevenue = invoiced;
        }

        return result;
    }

    /**
     * Builds the list of top customers by revenue.
     */
    private List<RevenueTopCustomerItem> buildTopCustomers(
            BigDecimal totalRevenue,
            LocalDate from,
            LocalDate to
    ) {

        List<Object[]> rows =
                invoiceRepository.getTopCustomerRevenue(from, to, PageRequest.of(0, 10));

        List<RevenueTopCustomerItem> result = new ArrayList<>();

        for (Object[] row : rows) {

            String name = (String) row[0];

            BigDecimal amount = (BigDecimal) row[1];

            BigDecimal share = BigDecimal.ZERO;

            if (totalRevenue.compareTo(BigDecimal.ZERO) > 0) {

                share = amount
                        .divide(totalRevenue, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
            }

            result.add(
                    new RevenueTopCustomerItem(
                            name,
                            amount,
                            BigDecimal.ZERO,
                            BigDecimal.ZERO,
                            share
                    )
            );
        }

        return result;
    }

//     HELPER METHOD

    private YearMonth extractYearMonth(Object dateObj) {

        if (dateObj instanceof java.sql.Timestamp ts) {
            return YearMonth.from(ts.toLocalDateTime());
        }

        if (dateObj instanceof LocalDate ld) {
            return YearMonth.from(ld);
        }

        if (dateObj instanceof LocalDateTime ldt) {
            return YearMonth.from(ldt);
        }

        throw new RuntimeException("Unsupported date type: " + dateObj.getClass());
    }
}