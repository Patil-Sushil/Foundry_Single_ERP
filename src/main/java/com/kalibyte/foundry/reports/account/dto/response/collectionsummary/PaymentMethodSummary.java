package com.kalibyte.foundry.reports.account.dto.response.collectionsummary;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record PaymentMethodSummary( String method,
                                    BigDecimal amount,
                                    Double percentage,
                                    Long count) {
}
