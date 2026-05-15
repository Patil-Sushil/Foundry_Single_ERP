package com.kalibyte.foundry.inventory.ledger.service;

import com.kalibyte.foundry.inventory.inward.entity.MaterialInward;
import com.kalibyte.foundry.inventory.ledger.dto.response.VendorBalanceResponse;
import com.kalibyte.foundry.inventory.ledger.dto.response.VendorLedgerResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface VendorLedgerService {
    List<VendorBalanceResponse> getAllVendorBalances();
    Page<VendorLedgerResponse> getVendorLedger(Long vendorId, LocalDate from, LocalDate to, Pageable pageable);
    VendorBalanceResponse getVendorBalance(Long vendorId);
    VendorLedgerResponse recordPayment(Long vendorId, BigDecimal amount, String description);
    void recordInwardEntry(MaterialInward inward);
}
