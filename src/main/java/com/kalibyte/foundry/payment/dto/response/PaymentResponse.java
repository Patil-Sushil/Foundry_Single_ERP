package com.kalibyte.foundry.payment.dto.response;

import com.kalibyte.foundry.payment.entity.Enums.PaymentMethod;
import com.kalibyte.foundry.payment.entity.Enums.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {

    private UUID id;
    private String paymentNumber;

    // ── Invoice Info ──
    private UUID invoiceId;
    private String invoiceNumber;

    // ── Customer Info ──
    private UUID customerId;
    private String customerName;

    // ── Core ──
    private BigDecimal amountPaid;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private LocalDate paymentDate;

    // ── Method-specific ──
    private String transactionId;
    private String instrumentNumber;
    private LocalDate instrumentDate;
    private String bankName;
    private String branchName;

    // ── General ──
    private String referenceNumber;
    private String remarks;
    private String receiptUrl;
    private String receivedBy;
    private String cancellationReason;

    // ── Audit ──
    private LocalDateTime createdAt;
}