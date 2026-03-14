package com.kalibyte.foundry.reports.account.dto.response.ledger;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Builder
public record CustomerLedgerReport(UUID customerId,
                                   String customerName,

                                   LocalDate fromDate,
                                   LocalDate toDate,

                                   BigDecimal openingBalance,
                                   BigDecimal closingBalance,

                                   BigDecimal totalInvoiced,
                                   BigDecimal totalReceived,

                                   List<LedgerTransaction> transactions) {
}
