package com.kalibyte.foundry.labors.advance.controller;

import com.kalibyte.foundry.common.response.ApiResponse;
import com.kalibyte.foundry.labors.advance.dto.AdvanceTransactionRequestDTO;
import com.kalibyte.foundry.labors.advance.dto.AdvanceTransactionResponseDTO;
import com.kalibyte.foundry.labors.advance.service.AdvanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/advances")
@RequiredArgsConstructor
@Tag(name = "Labor Advances", description = "APIs for managing labor cash advances")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdvanceController {

    private final AdvanceService advanceService;

    @PostMapping("/grant")
    @Operation(summary = "Grant a cash advance", description = "Only accessible by ADMIN")
    public ResponseEntity<ApiResponse<AdvanceTransactionResponseDTO>> grantAdvance(@RequestBody AdvanceTransactionRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success("Advance granted successfully", advanceService.grantAdvance(request)));
    }

    @GetMapping("/balance/{laborerId}")
    @Operation(summary = "Get outstanding advance balance", description = "Only accessible by ADMIN")
    public ResponseEntity<ApiResponse<BigDecimal>> getOutstandingBalance(@PathVariable Long laborerId) {
        return ResponseEntity.ok(ApiResponse.success(advanceService.getOutstandingBalance(laborerId)));
    }

    @GetMapping("/laborer/{laborerId}")
    @Operation(summary = "Get advance transaction history", description = "Only accessible by ADMIN")
    public ResponseEntity<ApiResponse<List<AdvanceTransactionResponseDTO>>> getTransactionsByLaborer(@PathVariable Long laborerId) {
        return ResponseEntity.ok(ApiResponse.success(advanceService.getTransactionsByLaborer(laborerId)));
    }
}
