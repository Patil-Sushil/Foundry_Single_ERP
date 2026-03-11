package com.kalibyte.foundry.inventory.ledger.repository;

import com.kalibyte.foundry.inventory.ledger.entity.VendorLedger;
import com.kalibyte.foundry.inventory.ledger.entity.enums.LedgerEntryType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import com.kalibyte.foundry.inventory.ledger.dto.response.VendorBalanceResponse;

@Repository
public interface VendorLedgerRepository extends JpaRepository<VendorLedger, Long> {

    @Query("SELECT new com.kalibyte.foundry.inventory.ledger.dto.response.VendorBalanceResponse(" +
           "v.id, v.name, " +
           "COALESCE(SUM(CASE WHEN vl.entryType = com.kalibyte.foundry.inventory.ledger.entity.enums.LedgerEntryType.CREDIT THEN vl.amount ELSE 0 END), 0), " +
           "COALESCE(SUM(CASE WHEN vl.entryType = com.kalibyte.foundry.inventory.ledger.entity.enums.LedgerEntryType.DEBIT THEN vl.amount ELSE 0 END), 0), " +
           "COALESCE(SUM(CASE WHEN vl.entryType = com.kalibyte.foundry.inventory.ledger.entity.enums.LedgerEntryType.CREDIT THEN vl.amount ELSE 0 END), 0) - " +
           "COALESCE(SUM(CASE WHEN vl.entryType = com.kalibyte.foundry.inventory.ledger.entity.enums.LedgerEntryType.DEBIT THEN vl.amount ELSE 0 END), 0)) " +
           "FROM Vendor v " +
           "LEFT JOIN VendorLedger vl ON v.id = vl.vendor.id " +
           "GROUP BY v.id, v.name")
    List<VendorBalanceResponse> findAllVendorBalances();

    @Query("SELECT vl FROM VendorLedger vl WHERE vl.vendor.id = :vendorId " +
           "AND (CAST(:from AS date) IS NULL OR vl.entryDate >= :from) " +
           "AND (CAST(:to AS date) IS NULL OR vl.entryDate <= :to) " +
           "ORDER BY vl.entryDate DESC")
    Page<VendorLedger> findByVendorIdOrderByEntryDateDesc(@Param("vendorId") Long vendorId, 
                                                          @Param("from") LocalDate from, 
                                                          @Param("to") LocalDate to, 
                                                          Pageable pageable);

    @Query("SELECT COALESCE(SUM(vl.amount), 0) FROM VendorLedger vl " +
           "WHERE vl.vendor.id = :vendorId AND vl.entryType = :entryType")
    BigDecimal sumByVendorAndType(@Param("vendorId") Long vendorId, 
                                  @Param("entryType") LedgerEntryType entryType);
}
