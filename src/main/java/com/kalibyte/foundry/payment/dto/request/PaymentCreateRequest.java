package com.kalibyte.foundry.payment.dto.request;

import com.kalibyte.foundry.payment.entity.Enums.PaymentMethod;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCreateRequest {

    @NotNull(message = "Invoice ID is required")
    private UUID invoiceId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Digits(integer = 12, fraction = 2, message = "Amount format is invalid")
    private BigDecimal amountPaid;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    // ── UPI / CARD / NEFT / RTGS / IMPS fields ──
    @Size(max = 100, message = "Transaction ID must not exceed 100 characters")
    private String transactionId;

    // ── CHEQUE / DD fields ──
    @Size(max = 20, message = "Instrument number must not exceed 20 characters")
    private String instrumentNumber;

    private LocalDate instrumentDate;

    @Size(max = 100, message = "Bank name must not exceed 100 characters")
    private String bankName;

    @Size(max = 100, message = "Branch name must not exceed 100 characters")
    private String branchName;

    // ── General fields ──
    private LocalDate paymentDate;  // null = today

    @Size(max = 100, message = "Reference number must not exceed 100 characters")
    private String referenceNumber;

    @Size(max = 500, message = "Remarks must not exceed 500 characters")
    private String remarks;

    @Size(max = 500, message = "Receipt URL must not exceed 500 characters")
    private String receiptUrl;

    @Size(max = 100, message = "Received by must not exceed 100 characters")
    private String receivedBy;
}