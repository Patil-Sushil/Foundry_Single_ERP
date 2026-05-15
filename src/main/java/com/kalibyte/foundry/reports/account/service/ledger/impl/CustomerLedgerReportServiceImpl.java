package com.kalibyte.foundry.reports.account.service.ledger.impl;

import com.kalibyte.foundry.payment.entity.Payment;
import com.kalibyte.foundry.payment.repository.PaymentRepository;
import com.kalibyte.foundry.billing.invoice.entity.Invoice;
import com.kalibyte.foundry.billing.invoice.repository.InvoiceRepository;
import com.kalibyte.foundry.customer.entity.Customer;
import com.kalibyte.foundry.customer.repository.CustomerRepository;
import com.kalibyte.foundry.reports.account.dto.response.ledger.CustomerLedgerReport;
import com.kalibyte.foundry.reports.account.dto.response.ledger.LedgerTransaction;
import com.kalibyte.foundry.reports.account.service.ledger.CustomerLedgerReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerLedgerReportServiceImpl implements CustomerLedgerReportService {

    private final CustomerRepository customerRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;

    @Override
    public CustomerLedgerReport getCustomerLedger(UUID customerId, LocalDate from, LocalDate to) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        List<Invoice> invoices =
                invoiceRepository.findInvoicesByCustomerAndDateRange(customerId, from, to);

        List<Payment> payments =
                paymentRepository.findPaymentsByCustomerAndDateRange(customerId, from, to);

        List<LedgerTransaction> transactions = new ArrayList<>();

        invoices.forEach(i ->
                transactions.add(
                        LedgerTransaction.builder()
                                .date(i.getInvoiceDate())
                                .type("INVOICE")
                                .documentNumber(i.getInvoiceNumber())
                                .description("Invoice")
                                .debit(i.getTotalAmount())
                                .credit(BigDecimal.ZERO)
                                .build()
                ));

        payments.forEach(p ->
                transactions.add(
                        LedgerTransaction.builder()
                                .date(p.getPaymentDate())
                                .type("PAYMENT")
                                .documentNumber(p.getPaymentNumber())
                                .description("Payment Received")
                                .debit(BigDecimal.ZERO)
                                .credit(p.getAmountPaid())
                                .build()
                ));

        transactions.sort(Comparator.comparing(LedgerTransaction::date));

        BigDecimal balance = BigDecimal.ZERO;
        List<LedgerTransaction> finalTransactions = new ArrayList<>();

        for (LedgerTransaction t : transactions) {

            balance = balance
                    .add(t.debit())
                    .subtract(t.credit());

            finalTransactions.add(
                    LedgerTransaction.builder()
                            .date(t.date())
                            .type(t.type())
                            .documentNumber(t.documentNumber())
                            .description(t.description())
                            .debit(t.debit())
                            .credit(t.credit())
                            .runningBalance(balance)
                            .build()
            );
        }

        BigDecimal totalInvoices =
                invoices.stream()
                        .map(Invoice::getTotalAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPayments =
                payments.stream()
                        .map(Payment::getAmountPaid)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CustomerLedgerReport.builder()
                .customerId(customerId)
                .customerName(customer.getName())
                .fromDate(from)
                .toDate(to)
                .openingBalance(BigDecimal.ZERO)
                .closingBalance(balance)
                .totalInvoiced(totalInvoices)
                .totalReceived(totalPayments)
                .transactions(finalTransactions)
                .build();
    }
}
