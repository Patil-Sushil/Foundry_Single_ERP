package com.kalibyte.foundry.labors.payout.controller;

import com.kalibyte.foundry.common.response.ApiResponse;
import com.kalibyte.foundry.labors.payout.dto.DisbursePayoutRequest;
import com.kalibyte.foundry.labors.payout.dto.WeeklyPayoutRequest;
import com.kalibyte.foundry.labors.payout.dto.WeeklyPayoutResponse;
import com.kalibyte.foundry.labors.payout.service.WeeklyPayoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/payouts")
@Tag(name = "Labor Payouts", description = "APIs for managing labor weekly payouts")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class PayoutController {

    private final WeeklyPayoutService weeklyPayoutService;

	@PostMapping("/generate")
    @Operation(summary = "Generate weekly payout for a laborer", description = "Only accessible by ADMIN")
    public ResponseEntity<ApiResponse<WeeklyPayoutResponse>> generateWeeklyPayout(@RequestBody @Valid WeeklyPayoutRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Weekly payout generated successfully", weeklyPayoutService.generateWeeklyPayout(request)));
    }

    @GetMapping("/laborer/{laborerId}")
    @Operation(summary = "Get payout history for a laborer", description = "Only accessible by ADMIN")
    public ResponseEntity<ApiResponse<List<WeeklyPayoutResponse>>> getPayoutsByLaborer(@PathVariable Long laborerId) {
        return ResponseEntity.ok(ApiResponse.success(weeklyPayoutService.getPayoutsByLaborer(laborerId)));
    }

    @PostMapping("/{payoutId}/disburse")
    @Operation(summary = "Mark a weekly payout as PAID", description = "Only accessible by ADMIN")
    public ResponseEntity<ApiResponse<WeeklyPayoutResponse>> disbursePayout(
            @PathVariable Long payoutId,
            @RequestBody @Valid DisbursePayoutRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Payout disbursed successfully", weeklyPayoutService.disbursePayout(payoutId, request)));
    }
}
