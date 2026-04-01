package com.kalibyte.foundry.inventory.ledger.dto.response;

import com.kalibyte.foundry.inventory.ledger.entity.enums.LedgerEntryType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record VendorLedgerResponse(
    Long id,
    Long vendorId,
    String vendorName,
    LedgerEntryType entryType,
    BigDecimal amount,
    String description,
    LocalDate entryDate,
    String inwardNumber,
    LocalDateTime createdAt
) {}
