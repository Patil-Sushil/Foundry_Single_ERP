package com.kalibyte.foundry.inventory.issue.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MaterialIssueSummary(
    Long id,
    String issueNumber,
    String departmentName,
    String purpose,
    LocalDate issueDate,
    int totalItems,
    BigDecimal totalValue
) {}
