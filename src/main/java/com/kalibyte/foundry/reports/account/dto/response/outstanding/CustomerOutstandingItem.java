package com.kalibyte.foundry.reports.account.dto.response.outstanding;


import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Builder
public record CustomerOutstandingItem(UUID customerId,
                                      String customerName,
                                      String companyName,

                                      BigDecimal totalInvoiced,
                                      BigDecimal totalPaid,
                                      BigDecimal outstanding,

                                      LocalDate lastPaymentDate,
                                      LocalDate oldestUnpaidInvoiceDate) {
}
