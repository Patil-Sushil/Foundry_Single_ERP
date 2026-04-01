package com.kalibyte.foundry.scrap.controller;

import com.kalibyte.foundry.common.response.ApiResponse;
import com.kalibyte.foundry.scrap.dto.request.ScrapEntryRequest;
import com.kalibyte.foundry.scrap.dto.response.ScrapEntryResponse;
import com.kalibyte.foundry.scrap.enums.ApprovalDecision;
import com.kalibyte.foundry.scrap.enums.ScrapStatus;
import com.kalibyte.foundry.scrap.service.ScrapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scrap")
@Tag(name = "Scrap Management", description = "APIs for managing scrap generation, verification, and approval")
@SecurityRequirement(name = "bearerAuth")
public class ScrapController {

    private final ScrapService scrapService;

    public ScrapController(ScrapService scrapService) {
        this.scrapService = scrapService;
    }

    @GetMapping
    @Operation(summary = "Get all scrap entries", description = "Fetch all scrap entries, optionally filtered by status. Accessible by ADMIN, PRODUCTION, QUALITY.")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION', 'QUALITY')")
    public ResponseEntity<ApiResponse<List<ScrapEntryResponse>>> getAll(
            @Parameter(description = "Optional status to filter scrap entries") @RequestParam(required = false) ScrapStatus status){
        if (status != null) {
            return ResponseEntity.ok(ApiResponse.success("Scrap entries by status", scrapService.getByStatus(status)));
        }
        return ResponseEntity.ok(ApiResponse.success("Scrap entries", scrapService.getAll()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get scrap entry by ID", description = "Fetch a single scrap entry by its unique identifier. Accessible by ADMIN, PRODUCTION, QUALITY.")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION', 'QUALITY')")
    public ResponseEntity<ApiResponse<ScrapEntryResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Scrap entry fetched", scrapService.getById(id)));
    }

    @PostMapping
    @Operation(summary = "Create a new scrap entry", description = "Register a new scrap entry from furnace, inspection, or customer return. Accessible by ADMIN, PRODUCTION.")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION')")
    public ResponseEntity<ApiResponse<ScrapEntryResponse>> create(@Valid @RequestBody ScrapEntryRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Scrap entry created", scrapService.createScrapEntry(request)));
    }

    @PutMapping("/{id}/verify")
    @Operation(summary = "Verify a scrap entry", description = "Verify the weight and grade of a scrap entry before approval. Accessible by ADMIN, METALLURGIST.")
    @PreAuthorize("hasAnyRole('ADMIN', 'METALLURGIST')")
    public ResponseEntity<ApiResponse<ScrapEntryResponse>> verify(
            @PathVariable Long id,
            @RequestParam String verifiedBy,
            @RequestParam(required = false) String notes) {
        return ResponseEntity.ok(ApiResponse.success("Scrap entry verified", scrapService.verifyScrap(id, verifiedBy, notes)));
    }

    @PutMapping("/{id}/approve")
    @Operation(summary = "Approve a scrap entry", description = "Approve a scrap entry for remelting or sale. Triggers automatic inventory inward if remelting is approved. Accessible by ADMIN, METALLURGIST.")
    @PreAuthorize("hasAnyRole('ADMIN', 'METALLURGIST')")
    public ResponseEntity<ApiResponse<ScrapEntryResponse>> approve(
            @PathVariable Long id,
            @RequestParam String approvedBy,
            @RequestParam ApprovalDecision decision,
            @RequestParam(required = false) String notes,
            @RequestParam(required = false) String finalGrade) {
        return ResponseEntity.ok(ApiResponse.success("Scrap entry approved", scrapService.approveScrap(id, approvedBy, decision, notes, finalGrade)));
    }
}
