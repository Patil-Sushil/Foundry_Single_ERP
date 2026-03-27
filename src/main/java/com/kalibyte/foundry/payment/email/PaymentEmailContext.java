package com.kalibyte.foundry.payment.email;

import com.kalibyte.foundry.payment.entity.Payment;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

/**
 * Holds all data needed to render a payment email template.
 */
@Getter
@Builder
public class PaymentEmailContext {

    private String customerName;
    private String customerEmail;

    // Payment details
    private String paymentNumber;
    private String paymentDate;
    private String paymentMethod;
    private BigDecimal amountPaid;
    private String status;

    // Method-specific
    private String transactionId;
    private String instrumentNumber;
    private String instrumentDate;
    private String bankName;
    private String branchName;
    private String receivedBy;

    // Invoice details
    private String invoiceNumber;
    private BigDecimal invoiceAmount;
    private BigDecimal totalPaid;
    private BigDecimal totalPending;
    private BigDecimal remainingAmount;
    private String invoiceStatus;

    // Event-specific
    private String cancellationReason;
    private String eventDate;

    // Email metadata
    private String subject;
    private EmailEventType eventType;

    // Detail rows for the method-specific section
    private List<DetailRow> methodDetails;

    @Getter
    @Builder
    public static class DetailRow {
        private String label;
        private String value;
    }
}