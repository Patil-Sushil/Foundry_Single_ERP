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

@Repository
public interface VendorLedgerRepository extends JpaRepository<VendorLedger, Long> {

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
