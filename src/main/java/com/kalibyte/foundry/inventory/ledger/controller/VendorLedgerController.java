package com.kalibyte.foundry.inventory.ledger.controller;

import com.kalibyte.foundry.common.response.ApiResponse;
import com.kalibyte.foundry.common.response.PageResponse;
import com.kalibyte.foundry.inventory.ledger.dto.response.VendorBalanceResponse;
import com.kalibyte.foundry.inventory.ledger.dto.response.VendorLedgerResponse;
import com.kalibyte.foundry.inventory.ledger.service.VendorLedgerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/vendors")
@Tag(name = "Vendor Ledger", description = "Vendor Ledger & Payments")
public class VendorLedgerController {

    private final VendorLedgerService vendorLedgerService;

	public VendorLedgerController(VendorLedgerService vendorLedgerService) {
		this.vendorLedgerService = vendorLedgerService;
	}

    @GetMapping("/ledger/balances")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    public ApiResponse<List<VendorBalanceResponse>> getAllBalances() {
        return ApiResponse.success("All Vendor Balances retrieved successfully", 
                vendorLedgerService.getAllVendorBalances());
    }

	@GetMapping("/{vendorId}/ledger")
	@PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
	public ApiResponse<PageResponse<VendorLedgerResponse>> getLedger(
            @PathVariable Long vendorId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            Pageable pageable) {
        return ApiResponse.success("Vendor Ledger retrieved successfully", 
                PageResponse.from(vendorLedgerService.getVendorLedger(vendorId, from, to, pageable)));
    }

    @GetMapping("/{vendorId}/ledger/balance")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    public ApiResponse<VendorBalanceResponse> getBalance(@PathVariable Long vendorId) {
        return ApiResponse.success("Vendor Balance retrieved successfully", 
                vendorLedgerService.getVendorBalance(vendorId));
    }

    @PostMapping("/{vendorId}/ledger/payment")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    public ApiResponse<VendorLedgerResponse> recordPayment(
            @PathVariable Long vendorId,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String description) {
        return ApiResponse.success("Payment recorded successfully", 
                vendorLedgerService.recordPayment(vendorId, amount, description));
    }
}
