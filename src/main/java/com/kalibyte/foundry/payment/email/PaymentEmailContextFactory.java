package com.kalibyte.foundry.payment.email;

import com.kalibyte.foundry.billing.invoice.entity.Invoice;
import com.kalibyte.foundry.customer.entity.Customer;
import com.kalibyte.foundry.payment.entity.Enums.PaymentMethod;
import com.kalibyte.foundry.payment.entity.Payment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates PaymentEmailContext from domain entities, keeping the
 * service layer clean.
 */
public final class PaymentEmailContextFactory {

    private PaymentEmailContextFactory() {
    }

    public static PaymentEmailContext create(
            Payment payment,
            EmailEventType eventType,
            BigDecimal totalPaid,
            BigDecimal totalPending) {

        Invoice invoice = payment.getInvoice();
        Customer customer = payment.getCustomer();

        BigDecimal remaining = invoice.getTotalAmount()
                .subtract(totalPaid)
                .subtract(totalPending);

        return PaymentEmailContext.builder()
                // Customer
                .customerName(customer.getName())
                .customerEmail(customer.getEmail())
                // Payment
                .paymentNumber(payment.getPaymentNumber())
                .paymentDate(payment.getPaymentDate().toString())
                .paymentMethod(
                        payment.getPaymentMethod().getDisplayName())
                .amountPaid(payment.getAmountPaid())
                .status(payment.getStatus().getDisplayName())
                // Method-specific
                .transactionId(payment.getTransactionId())
                .instrumentNumber(payment.getInstrumentNumber())
                .instrumentDate(payment.getInstrumentDate() != null
                        ? payment.getInstrumentDate().toString()
                        : null)
                .bankName(payment.getBankName())
                .branchName(payment.getBranchName())
                .receivedBy(payment.getReceivedBy())
                // Invoice
                .invoiceNumber(invoice.getInvoiceNumber())
                .invoiceAmount(invoice.getTotalAmount())
                .totalPaid(totalPaid)
                .totalPending(totalPending)
                .remainingAmount(remaining)
                .invoiceStatus(invoice.getBillStatus().name())
                // Event
                .cancellationReason(payment.getCancellationReason())
                .eventDate(LocalDate.now().toString())
                .eventType(eventType)
                .subject(buildSubject(eventType, payment))
                .methodDetails(buildMethodDetails(payment))
                .build();
    }

    // ── SUBJECT LINE ──
    private static String buildSubject(
            EmailEventType event, Payment payment) {

        String instrumentType = payment.getPaymentMethod()
                == PaymentMethod.CHEQUE ? "Cheque" : "DD";

        return switch (event) {
            case PAYMENT_CREATED ->
                    "Payment Received — " + payment.getPaymentNumber();
            case CHEQUE_CLEARED ->
                    "✅ " + instrumentType + " Cleared — "
                            + payment.getPaymentNumber();
            case CHEQUE_BOUNCED ->
                    "❌ " + instrumentType + " Bounced — "
                            + payment.getPaymentNumber()
                            + " | Action Required";
            case PAYMENT_CANCELLED ->
                    "Payment Cancelled — " + payment.getPaymentNumber();
        };
    }

    // ── METHOD-SPECIFIC DETAIL ROWS ──
    private static List<PaymentEmailContext.DetailRow> buildMethodDetails(
            Payment payment) {

        List<PaymentEmailContext.DetailRow> rows = new ArrayList<>();

        switch (payment.getPaymentMethod()) {
            case UPI, NEFT, RTGS, IMPS, CARD, BANK_TRANSFER -> {
                if (payment.getTransactionId() != null) {
                    rows.add(detailRow(
                            payment.getPaymentMethod().getDisplayName()
                                    + " Transaction ID",
                            payment.getTransactionId()));
                }
                if (payment.getReferenceNumber() != null) {
                    rows.add(detailRow("Reference Number",
                            payment.getReferenceNumber()));
                }
            }
            case CHEQUE, DEMAND_DRAFT -> {
                String type = payment.getPaymentMethod()
                        == PaymentMethod.CHEQUE
                        ? "Cheque" : "DD";
                rows.add(detailRow(type + " Number",
                        payment.getInstrumentNumber()));
                rows.add(detailRow(type + " Date",
                        payment.getInstrumentDate() != null
                                ? payment.getInstrumentDate().toString()
                                : "N/A"));
                rows.add(detailRow("Bank",
                        payment.getBankName()));
                rows.add(detailRow("Branch",
                        payment.getBranchName() != null
                                ? payment.getBranchName() : "N/A"));
            }
            case CASH -> {
                if (payment.getReceivedBy() != null) {
                    rows.add(detailRow("Received By",
                            payment.getReceivedBy()));
                }
            }
        }
        return rows;
    }

    private static PaymentEmailContext.DetailRow detailRow(
            String label, String value) {
        return PaymentEmailContext.DetailRow.builder()
                .label(label)
                .value(value)
                .build();
    }
}