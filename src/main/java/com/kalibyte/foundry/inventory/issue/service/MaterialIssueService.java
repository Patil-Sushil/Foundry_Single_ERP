package com.kalibyte.foundry.inventory.issue.service;

import com.kalibyte.foundry.common.response.PageResponse;
import com.kalibyte.foundry.inventory.issue.dto.request.RecordIssueRequest;
import com.kalibyte.foundry.inventory.issue.dto.response.DepartmentConsumptionReport;
import com.kalibyte.foundry.inventory.issue.dto.response.MaterialIssueResponse;
import com.kalibyte.foundry.inventory.issue.dto.response.MaterialIssueSummary;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface MaterialIssueService {
    MaterialIssueResponse recordIssue(RecordIssueRequest request);
    MaterialIssueResponse getById(Long id);
    PageResponse<MaterialIssueSummary> getAll(Long departmentId, LocalDate from, LocalDate to, Pageable pageable);
    DepartmentConsumptionReport getConsumptionReport(Long departmentId, LocalDate from, LocalDate to);
}
