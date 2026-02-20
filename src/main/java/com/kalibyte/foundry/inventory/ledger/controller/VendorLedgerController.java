package com.kalibyte.foundry.inventory.ledger.controller;

import com.kalibyte.foundry.common.response.ApiResponse;
import com.kalibyte.foundry.inventory.ledger.dto.response.VendorBalanceResponse;
import com.kalibyte.foundry.inventory.ledger.dto.response.VendorLedgerResponse;
import com.kalibyte.foundry.inventory.ledger.service.VendorLedgerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/vendors/{vendorId}/ledger")
@Tag(name = "Vendor Ledger", description = "Vendor Ledger & Payments")
public class VendorLedgerController {

    private final VendorLedgerService vendorLedgerService;

	public VendorLedgerController(VendorLedgerService vendorLedgerService) {
		this.vendorLedgerService = vendorLedgerService;
	}

	@GetMapping
    public ApiResponse<Page<VendorLedgerResponse>> getLedger(
            @PathVariable Long vendorId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            Pageable pageable) {
        return ApiResponse.success("Vendor Ledger retrieved successfully", 
                vendorLedgerService.getVendorLedger(vendorId, from, to, pageable));
    }

    @GetMapping("/balance")
    public ApiResponse<VendorBalanceResponse> getBalance(@PathVariable Long vendorId) {
        return ApiResponse.success("Vendor Balance retrieved successfully", 
                vendorLedgerService.getVendorBalance(vendorId));
    }

    @PostMapping("/payment")
    public ApiResponse<VendorLedgerResponse> recordPayment(
            @PathVariable Long vendorId,
            @RequestParam BigDecimal amount,
            @RequestParam String description) {
        return ApiResponse.success("Payment recorded successfully", 
                vendorLedgerService.recordPayment(vendorId, amount, description));
    }
}
