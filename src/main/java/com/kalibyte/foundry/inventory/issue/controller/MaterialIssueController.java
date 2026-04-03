package com.kalibyte.foundry.inventory.issue.controller;

import com.kalibyte.foundry.common.response.ApiResponse;
import com.kalibyte.foundry.common.response.PageResponse;
import com.kalibyte.foundry.common.security.UserPrincipal;
import com.kalibyte.foundry.inventory.issue.dto.request.RecordIssueRequest;
import com.kalibyte.foundry.inventory.issue.dto.response.DepartmentConsumptionReport;
import com.kalibyte.foundry.inventory.issue.dto.response.MaterialIssueResponse;
import com.kalibyte.foundry.inventory.issue.dto.response.MaterialIssueSummary;
import com.kalibyte.foundry.inventory.issue.service.MaterialIssueService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/material-issues")
@Tag(name = "Material Issue", description = "Material Issue Management APIs")
public class MaterialIssueController {

    private final MaterialIssueService materialIssueService;

	public MaterialIssueController(MaterialIssueService materialIssueService) {
		this.materialIssueService = materialIssueService;
	}

	@PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MaterialIssueResponse> recordIssue(
            @Valid @RequestBody RecordIssueRequest request) {
        return ApiResponse.success("Material Issue recorded successfully", materialIssueService.recordIssue(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<MaterialIssueResponse> getById(@PathVariable Long id) {
        return ApiResponse.success("Material Issue retrieved successfully", 
                materialIssueService.getById(id));
    }

    @GetMapping
    public ApiResponse<PageResponse<MaterialIssueSummary>> getAll(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            Pageable pageable) {
        return ApiResponse.success("Material Issues retrieved successfully",
                materialIssueService.getAll(departmentId, from, to, pageable));
    }

    @GetMapping("/consumption-report")
    public ApiResponse<DepartmentConsumptionReport> getReport(
            @RequestParam Long departmentId,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to) {
        return ApiResponse.success("Consumption Report generated successfully", 
                materialIssueService.getConsumptionReport(departmentId, from, to));
    }
}
