package com.kalibyte.foundry.reports.account.controller;

import com.kalibyte.foundry.common.response.ApiResponse;
import com.kalibyte.foundry.reports.account.dto.response.aging.AgingReport;
import com.kalibyte.foundry.reports.account.dto.response.cashflow.CashFlowReport;
import com.kalibyte.foundry.reports.account.dto.response.collectionsummary.CollectionSummaryReport;
import com.kalibyte.foundry.reports.account.dto.response.dailycollection.DailyCollectionReport;
import com.kalibyte.foundry.reports.account.dto.response.ledger.CustomerLedgerReport;
import com.kalibyte.foundry.reports.account.dto.response.outstanding.CustomerOutstandingReport;
import com.kalibyte.foundry.reports.account.dto.response.overdueinvoice.OverdueInvoiceReport;
import com.kalibyte.foundry.reports.account.dto.response.overdueinvoice.enums.OverdueSeverity;
import com.kalibyte.foundry.reports.account.dto.response.profitloss.ProfitLossReport;
import com.kalibyte.foundry.reports.account.service.aging.AgingReportService;
import com.kalibyte.foundry.reports.account.service.cashflow.CashFlowReportService;
import com.kalibyte.foundry.reports.account.service.collectionsummary.CollectionSummaryReportService;
import com.kalibyte.foundry.reports.account.service.dailycollection.DailyCollectionReportService;
import com.kalibyte.foundry.reports.account.service.ledger.CustomerLedgerReportService;
import com.kalibyte.foundry.reports.account.service.outstanding.CustomerOutstandingReportService;
import com.kalibyte.foundry.reports.account.service.overdueinvoice.OverdueInvoiceReportService;
import com.kalibyte.foundry.reports.account.service.profitloss.ProfitLossReportService;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.core.Local;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * REST Controller responsible for all Accounts Reports.

 * Provides reporting APIs for:
 * - Daily Collection
 * - Collection Summary
 * - Customer Outstanding
 * - Customer Ledger
 * - Receivables Aging
 * - Cash Flow
 */
@Slf4j
@RestController
@RequestMapping("/api/reports/accounts")
@RequiredArgsConstructor
public class AccountsReportController {

    private final DailyCollectionReportService dailyCollectionReportService;
    private final CollectionSummaryReportService collectionSummaryReportService;
    private final CustomerOutstandingReportService customerOutstandingReportService;
    private final CustomerLedgerReportService customerLedgerReportService;
    private final AgingReportService agingReportService;
    private final CashFlowReportService cashFlowReportService;
    private final OverdueInvoiceReportService overdueInvoiceReportService;
    private final ProfitLossReportService profitLossReportService;

    //------------------------------------------------
    // DAILY COLLECTION REPORT
    //------------------------------------------------

    /**
     * Returns daily payment collection for a given date range.
     */
    @GetMapping("/daily-collection")
    @PreAuthorize("hasAnyRole('ADMIN','PRODUCTION')")
    public ApiResponse<DailyCollectionReport> dailyCollection(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ) {

        return ApiResponse.success(
                dailyCollectionReportService.getDailyCollection(from, to)
        );
    }

    //------------------------------------------------
    // COLLECTION SUMMARY REPORT
    //------------------------------------------------

    /**
     * Returns collection summary including:
     * - total collection
     * - payment method breakdown
     * - top customers
     */
    @GetMapping("/collection-summary")
    @PreAuthorize("hasAnyRole('ADMIN','PRODUCTION')")
    public ApiResponse<CollectionSummaryReport> collectionSummary(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ) {

        return ApiResponse.success(
                collectionSummaryReportService.getCollectionSummary(from, to)
        );
    }

    //------------------------------------------------
    // CUSTOMER OUTSTANDING REPORT
    //------------------------------------------------

    /**
     * Returns customer outstanding balances.
     * If no date is provided, today's date will be used.
     */
    @GetMapping("/customer-outstanding")
    @PreAuthorize("hasAnyRole('ADMIN','PRODUCTION')")
    public ApiResponse<CustomerOutstandingReport> customerOutstanding(
            @RequestParam(required = false) LocalDate asOfDate
    ) {

        if (asOfDate == null) {
            asOfDate = LocalDate.now();
        }

        return ApiResponse.success(
                customerOutstandingReportService.getCustomerOutstanding(asOfDate)
        );
    }

    //------------------------------------------------
    // CUSTOMER LEDGER REPORT
    //------------------------------------------------

    /**
     * Returns detailed ledger for a specific customer.

     * Shows:
     * - invoices
     * - payments
     * - running balance
     */
    @GetMapping("/customer-ledger/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN','PRODUCTION')")
    public ApiResponse<CustomerLedgerReport> customerLedger(
            @PathVariable UUID customerId,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ) {

        return ApiResponse.success(
                customerLedgerReportService.getCustomerLedger(customerId, from, to)
        );
    }

    //------------------------------------------------
    // RECEIVABLES AGING REPORT
    //------------------------------------------------

    /**
     * Generates Receivables Aging Report.

     * Aging buckets:
     * - Current
     * - 1–30 days
     * - 31–60 days
     * - 61–90 days
     * - 90+ days
     */
    @GetMapping("/aging")
    @PreAuthorize("hasAnyRole('ADMIN','PRODUCTION')")
    public ApiResponse<AgingReport> agingReport(
            @RequestParam(required = false) LocalDate asOfDate
    ) {

        if (asOfDate == null) {
            asOfDate = LocalDate.now();
        }

        return ApiResponse.success(
                agingReportService.getReceivablesAging(asOfDate)
        );
    }

    //------------------------------------------------
    // CASH FLOW REPORT
    //------------------------------------------------

    /**
     * Generates Cash Flow Report.

     * Shows:
     * - inflow (customer payments)
     * - outflow (expenses)
     * - net cash flow
     */
    @GetMapping("/cash-flow")
    @PreAuthorize("hasAnyRole('ADMIN','PRODUCTION')")
    public ApiResponse<CashFlowReport> cashFlow(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ) {

        return ApiResponse.success(
                cashFlowReportService.getCashFlow(from, to)
        );
    }

//    ------------------------------------------------
//    OVERDUE INVOICE REPORT
//    ------------------------------------------------

    @GetMapping("/overdue")
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT','MANAGER')")
    public ApiResponse<OverdueInvoiceReport> overdueInvoices(

            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) OverdueSeverity severity,
            @RequestParam(required = false) BigDecimal minAmount,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size

    ){

        OverdueInvoiceReport report =
                overdueInvoiceReportService.generateReport(
                        customerId,
                        severity,
                        minAmount,
                        page,
                        size
                );

        return ApiResponse.success(report);
    }

//    ------------------------------------------------
//    PROFIT & LOSS REPORT
//    ------------------------------------------------
    @GetMapping("/profit-loss")
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT','MANAGER')")
    public ApiResponse<ProfitLossReport> profitLoss(

            @RequestParam LocalDate from,
            @RequestParam LocalDate to

    ){

        ProfitLossReport report =
                profitLossReportService.generateReport(from,to);

        return ApiResponse.success(report);
    }
}