package com.kalibyte.foundry.inventory.ledger.service;

import com.kalibyte.foundry.common.exception.ResourceNotFoundException;
import com.kalibyte.foundry.inventory.inward.entity.MaterialInward;
import com.kalibyte.foundry.inventory.ledger.dto.response.VendorBalanceResponse;
import com.kalibyte.foundry.inventory.ledger.dto.response.VendorLedgerResponse;
import com.kalibyte.foundry.inventory.ledger.entity.VendorLedger;
import com.kalibyte.foundry.inventory.ledger.entity.enums.LedgerEntryType;
import com.kalibyte.foundry.inventory.ledger.repository.VendorLedgerRepository;
import com.kalibyte.foundry.inventory.vendor.entity.Vendor;
import com.kalibyte.foundry.inventory.vendor.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class VendorLedgerService {

    private final VendorLedgerRepository vendorLedgerRepository;
    private final VendorRepository vendorRepository;

    @Transactional(readOnly = true)
    public Page<VendorLedgerResponse> getVendorLedger(Long vendorId, LocalDate from, LocalDate to, Pageable pageable) {
        return vendorLedgerRepository.findByVendorIdOrderByEntryDateDesc(vendorId, from, to, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public VendorBalanceResponse getVendorBalance(Long vendorId) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with id: " + vendorId));

        BigDecimal totalCredit = vendorLedgerRepository.sumByVendorAndType(vendorId, LedgerEntryType.CREDIT);
        BigDecimal totalDebit = vendorLedgerRepository.sumByVendorAndType(vendorId, LedgerEntryType.DEBIT);
        BigDecimal outstandingBalance = totalCredit.subtract(totalDebit);

        return new VendorBalanceResponse(
                vendor.getId(),
                vendor.getName(),
                totalCredit,
                totalDebit,
                outstandingBalance
        );
    }

    @Transactional
    public VendorLedgerResponse recordPayment(Long vendorId, BigDecimal amount, String description) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with id: " + vendorId));

        VendorLedger ledger = VendorLedger.builder()
                .vendor(vendor)
                .entryType(LedgerEntryType.DEBIT)
                .amount(amount)
                .description(description)
                .entryDate(LocalDate.now())
                .build();

        return toResponse(vendorLedgerRepository.save(ledger));
    }

    @Transactional
    public void recordInwardEntry(MaterialInward inward) {
        VendorLedger ledger = VendorLedger.builder()
                .vendor(inward.getVendor())
                .materialInward(inward)
                .entryType(LedgerEntryType.CREDIT)
                .amount(inward.getTotalAmount())
                .description("Material received - " + inward.getInwardNumber())
                .entryDate(inward.getInwardDate())
                .createdBy(inward.getConfirmedByUserId() != null ? String.valueOf(inward.getConfirmedByUserId()) : null)
                .build();
        
        vendorLedgerRepository.save(ledger);
    }

    private VendorLedgerResponse toResponse(VendorLedger ledger) {
        return new VendorLedgerResponse(
                ledger.getId(),
                ledger.getVendor().getId(),
                ledger.getVendor().getName(),
                ledger.getEntryType(),
                ledger.getAmount(),
                ledger.getDescription(),
                ledger.getEntryDate(),
                ledger.getMaterialInward() != null ? ledger.getMaterialInward().getInwardNumber() : null,
                ledger.getCreatedAt()
        );
    }
}
