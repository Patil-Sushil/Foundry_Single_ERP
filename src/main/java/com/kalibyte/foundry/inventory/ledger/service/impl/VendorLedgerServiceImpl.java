package com.kalibyte.foundry.inventory.ledger.service.impl;

import com.kalibyte.foundry.common.exception.ResourceNotFoundException;
import com.kalibyte.foundry.inventory.inward.entity.MaterialInward;
import com.kalibyte.foundry.inventory.ledger.dto.response.VendorBalanceResponse;
import com.kalibyte.foundry.inventory.ledger.dto.response.VendorLedgerResponse;
import com.kalibyte.foundry.inventory.ledger.entity.VendorLedger;
import com.kalibyte.foundry.inventory.ledger.entity.enums.LedgerEntryType;
import com.kalibyte.foundry.inventory.ledger.mapper.VendorLedgerMapper;
import com.kalibyte.foundry.inventory.ledger.repository.VendorLedgerRepository;
import com.kalibyte.foundry.inventory.ledger.service.VendorLedgerService;
import com.kalibyte.foundry.inventory.vendor.entity.Vendor;
import com.kalibyte.foundry.inventory.vendor.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VendorLedgerServiceImpl implements VendorLedgerService {

    private final VendorLedgerRepository vendorLedgerRepository;
    private final VendorRepository vendorRepository;
    private final VendorLedgerMapper vendorLedgerMapper;

    @Override
    @Transactional(readOnly = true)
    public List<VendorBalanceResponse> getAllVendorBalances() {
        return vendorLedgerRepository.findAllVendorBalances();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VendorLedgerResponse> getVendorLedger(Long vendorId, LocalDate from, LocalDate to, Pageable pageable) {
        return vendorLedgerRepository.findByVendorIdOrderByEntryDateDesc(vendorId, from, to, pageable)
                .map(vendorLedgerMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public VendorBalanceResponse getVendorBalance(Long vendorId) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with id: " + vendorId));

        BigDecimal totalCredit = vendorLedgerRepository.sumByVendorAndType(vendorId, LedgerEntryType.CREDIT);
        BigDecimal totalDebit = vendorLedgerRepository.sumByVendorAndType(vendorId, LedgerEntryType.DEBIT);
        BigDecimal outstandingBalance = totalCredit.subtract(totalDebit);

        return vendorLedgerMapper.toBalanceResponse(vendor, totalCredit, totalDebit, outstandingBalance);
    }

    @Override
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

        return vendorLedgerMapper.toResponse(vendorLedgerRepository.save(ledger));
    }

    @Override
    @Transactional
    public void recordInwardEntry(MaterialInward inward) {
        VendorLedger ledger = VendorLedger.builder()
                .vendor(inward.getVendor())
                .materialInward(inward)
                .entryType(LedgerEntryType.CREDIT)
                .amount(inward.getTotalAmount())
                .description("Material received - " + inward.getInwardNumber())
                .entryDate(inward.getInwardDate())
                .build();
        
        vendorLedgerRepository.save(ledger);
    }
}
