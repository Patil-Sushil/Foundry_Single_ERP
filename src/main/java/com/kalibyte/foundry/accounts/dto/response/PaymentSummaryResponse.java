package com.kalibyte.foundry.accounts.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentSummaryResponse {

    private BigDecimal invoiceAmount;

    private BigDecimal totalPaid;

    private BigDecimal remainingAmount;
}