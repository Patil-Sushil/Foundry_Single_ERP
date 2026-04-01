package com.kalibyte.foundry.inventory.inward.dto.response;

import com.kalibyte.foundry.inventory.inward.entity.enums.InwardStatus;
import java.math.BigDecimal;
import java.time.LocalDate;

public record InwardSummary(
    Long id,
    String inwardNumber,
    String vendorName,
    InwardStatus status,
    LocalDate inwardDate,
    int totalItems,
    BigDecimal grandTotal
) {}
