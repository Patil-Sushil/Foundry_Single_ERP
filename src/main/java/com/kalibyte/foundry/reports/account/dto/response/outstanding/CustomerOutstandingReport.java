package com.kalibyte.foundry.reports.account.dto.response.outstanding;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Builder
public record CustomerOutstandingReport(LocalDate asOfDate,

                                        BigDecimal totalOutstanding,
                                        Long customerCount,

                                        List<CustomerOutstandingItem> customers) {
}
