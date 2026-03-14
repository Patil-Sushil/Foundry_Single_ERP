package com.kalibyte.foundry.reports.account.dto.response.collectionsummary;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record TopCustomerCollection(UUID customerId,
                                    String customerName,
                                    BigDecimal totalPaid) {
}
