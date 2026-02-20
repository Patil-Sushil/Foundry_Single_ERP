package com.kalibyte.foundry.inventory.issue.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record MaterialIssueResponse(
    Long id,
    String issueNumber,
    String departmentName,
    String purpose,
    LocalDate issueDate,
    List<IssuedItemDetail> items,
    BigDecimal totalValue,
    LocalDateTime createdAt
) {}
