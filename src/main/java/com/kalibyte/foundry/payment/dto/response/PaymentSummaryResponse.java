package com.kalibyte.foundry.payment.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentSummaryResponse {

    private BigDecimal invoiceAmount;
    private BigDecimal totalPaid;          // only SUCCESS
    private BigDecimal totalPending;       // cheques awaiting clearance
    private BigDecimal remainingAmount;    // invoice - paid - pending
    private int totalTransactions;
    private String invoiceStatus;
}