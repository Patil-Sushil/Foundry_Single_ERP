package com.kalibyte.foundry.inventory.issue.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

public record RecordIssueRequest(
    @NotNull(message = "Department ID is required")
    Long departmentId,

    String purpose,

    LocalDate issueDate,

    @NotEmpty(message = "Items list cannot be empty")
    @Valid
    List<IssueItemRequest> items,

    String notes
) implements Serializable {}
