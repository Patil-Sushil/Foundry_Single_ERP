package com.kalibyte.foundry.reports.dto.response.accounts;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Builder
public record DailyCollectionReport(LocalDate fromDate,
                                    LocalDate toDate,

                                    BigDecimal totalCollection,
                                    Long totalTransactions,

                                    List<DailyCollectionItem> dailyBreakdown) {
}
