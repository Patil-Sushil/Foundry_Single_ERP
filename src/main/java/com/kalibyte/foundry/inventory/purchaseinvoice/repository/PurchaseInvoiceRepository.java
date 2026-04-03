package com.kalibyte.foundry.inventory.purchaseinvoice.repository;

import com.kalibyte.foundry.inventory.purchaseinvoice.entity.PurchaseInvoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PurchaseInvoiceRepository extends JpaRepository<PurchaseInvoice, Long> {

    boolean existsByVendorIdAndVendorInvoiceNumber(Long vendorId, String vendorInvoiceNumber);

    List<PurchaseInvoice> findByMaterialInwardId(Long materialInwardId);

    List<PurchaseInvoice> findByPurchaseOrderId(Long purchaseOrderId);

    List<PurchaseInvoice> findByVendorIdAndMaterialInwardIsNull(Long vendorId);

    @Query("""
        SELECT pi FROM PurchaseInvoice pi
        JOIN FETCH pi.vendor v
        LEFT JOIN pi.purchaseOrder po
        LEFT JOIN pi.materialInward mi
        WHERE (:vendorId IS NULL OR v.id = :vendorId)
          AND (:verified IS NULL OR pi.isVerified = :verified)
          AND (:from IS NULL OR pi.vendorInvoiceDate >= :from)
          AND (:to IS NULL OR pi.vendorInvoiceDate <= :to)
          AND (:hasInward IS NULL
               OR (:hasInward = true AND mi IS NOT NULL)
               OR (:hasInward = false AND mi IS NULL))
        ORDER BY pi.vendorInvoiceDate DESC
    """)
    Page<PurchaseInvoice> findAllFiltered(
        @Param("vendorId") Long vendorId,
        @Param("verified") Boolean verified,
        @Param("from") LocalDate from,
        @Param("to") LocalDate to,
        @Param("hasInward") Boolean hasInward,
        Pageable pageable
    );

    @Query("""
        SELECT pi FROM PurchaseInvoice pi
        JOIN FETCH pi.vendor v
        LEFT JOIN FETCH pi.materialInward mi
        LEFT JOIN FETCH pi.purchaseOrder po
        WHERE pi.vendorInvoiceDate BETWEEN :from AND :to
        ORDER BY pi.vendorInvoiceDate
    """)
    List<PurchaseInvoice> findForGstReport(
        @Param("from") LocalDate from,
        @Param("to") LocalDate to
    );
}
