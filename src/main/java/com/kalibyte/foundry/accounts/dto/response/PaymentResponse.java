package com.kalibyte.foundry.accounts.dto.response;

import com.kalibyte.foundry.accounts.entity.Enums.PaymentMethod;
import com.kalibyte.foundry.accounts.entity.Enums.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {

    private UUID id;

    private String paymentNumber;

    private UUID invoiceId;

    private BigDecimal amountPaid;

    private PaymentMethod paymentMethod;

    private PaymentStatus status;

    private LocalDate paymentDate;

    private String referenceNumber;

    private String remarks;
}
