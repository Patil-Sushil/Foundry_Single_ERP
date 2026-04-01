package com.kalibyte.foundry.reports.account.dto.response.ledger;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record LedgerTransaction(LocalDate date,
                                String type,
                                String documentNumber,
                                String description,

                                BigDecimal debit,
                                BigDecimal credit,
                                BigDecimal runningBalance) {
}
