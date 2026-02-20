package com.kalibyte.foundry.inventory.ledger.dto.response;

import java.math.BigDecimal;

public record VendorBalanceResponse(
    Long vendorId,
    String vendorName,
    BigDecimal totalCredit,
    BigDecimal totalDebit,
    BigDecimal outstandingBalance
) {}
