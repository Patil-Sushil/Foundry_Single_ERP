package com.kalibyte.foundry.payment.entity;

import com.kalibyte.foundry.billing.invoice.entity.Invoice;
import com.kalibyte.foundry.common.base.BaseEntity;
import com.kalibyte.foundry.customer.entity.Customer;
import com.kalibyte.foundry.payment.entity.Enums.PaymentMethod;
import com.kalibyte.foundry.payment.entity.Enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_payment_invoice", columnList = "invoice_id"),
        @Index(name = "idx_payment_customer", columnList = "customer_id"),
        @Index(name = "idx_payment_date", columnList = "payment_date"),
        @Index(name = "idx_payment_status", columnList = "status"),
        @Index(name = "idx_payment_method", columnList = "payment_method"),
        @Index(name = "idx_payment_number", columnList = "payment_number", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends BaseEntity {

    @Column(name = "payment_number", nullable = false, unique = true, updatable = false)
    private String paymentNumber;

    // ── INVOICE ──────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false, updatable = false)
    private Invoice invoice;

    // ── CUSTOMER ─────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false, updatable = false)
    private Customer customer;

    // ── CORE DATA ────────────────────────────────────
    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "amount_paid", precision = 14, scale = 2, nullable = false)
    private BigDecimal amountPaid;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    // ── UPI / CARD / NEFT / RTGS / IMPS ──────────────
    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    // ── CHEQUE / DD ──────────────────────────────────
    @Column(name = "instrument_number", length = 20)
    private String instrumentNumber;          // cheque no / DD no

    @Column(name = "instrument_date")
    private LocalDate instrumentDate;         // cheque date / DD date

    @Column(name = "bank_name", length = 100)
    private String bankName;                  // drawn-on bank

    @Column(name = "branch_name", length = 100)
    private String branchName;

    // ── GENERAL ──────────────────────────────────────
    @Column(name = "reference_number", length = 100)
    private String referenceNumber;           // any external ref

    @Column(name = "remarks", length = 500)
    private String remarks;

    @Column(name = "receipt_url", length = 500)
    private String receiptUrl;                // link to uploaded receipt/proof

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @Column(name = "received_by", length = 100)
    private String receivedBy;                // cashier / salesperson name
}