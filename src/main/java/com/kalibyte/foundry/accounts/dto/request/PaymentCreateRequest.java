package com.kalibyte.foundry.accounts.dto.request;

import com.kalibyte.foundry.accounts.entity.Enums.PaymentMethod;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentCreateRequest {
    private UUID invoiceId;

    private BigDecimal amountPaid;

    private PaymentMethod paymentMethod;

    private String referenceNumber;

    private String remarks;
}
