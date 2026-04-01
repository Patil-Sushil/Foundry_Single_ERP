package com.kalibyte.foundry.reports.account.dto.response.collectionsummary;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record CollectionSummaryReport(String period,

                                      BigDecimal totalCollection,
                                      BigDecimal previousPeriodCollection,
                                      Double growthPercentage,

                                      List<PaymentMethodSummary> methodWiseBreakdown,
                                      List<TopCustomerCollection> topCustomers) {
}
