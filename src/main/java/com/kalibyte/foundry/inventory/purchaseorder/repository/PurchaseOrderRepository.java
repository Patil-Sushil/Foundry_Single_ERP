package com.kalibyte.foundry.inventory.purchaseorder.repository;

import com.kalibyte.foundry.inventory.purchaseorder.entity.PurchaseOrder;
import com.kalibyte.foundry.inventory.purchaseorder.entity.enums.POStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    @Query("SELECT p FROM PurchaseOrder p " +
           "LEFT JOIN FETCH p.vendor " +
           "LEFT JOIN FETCH p.orderItems oi " +
           "LEFT JOIN FETCH oi.item " +
           "WHERE p.id = :id")
    Optional<PurchaseOrder> findWithDetails(@Param("id") Long id);

    List<PurchaseOrder> findByStatusInOrderByPoDateDesc(List<POStatus> statuses);

    @Query("SELECT p FROM PurchaseOrder p WHERE " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:vendorId IS NULL OR p.vendor.id = :vendorId) " +
           "ORDER BY p.poDate DESC")
    Page<PurchaseOrder> findAllFiltered(@Param("status") POStatus status, 
                                        @Param("vendorId") Long vendorId, 
                                        Pageable pageable);

    @Query("SELECT COUNT(p) FROM PurchaseOrder p WHERE YEAR(p.poDate) = :year")
    long countByYear(@Param("year") int year);

    boolean existsByPoNumber(String poNumber);

    /**
     * Returns total raw material purchase cost (COGS).
     */
    @Query("""
    SELECT COALESCE(SUM(poi.orderedQuantity * poi.unitRate), 0)
    FROM PurchaseOrderItem poi
    JOIN poi.purchaseOrder po
    WHERE po.poDate BETWEEN :from AND :to
      AND po.status != 'CANCELLED'
    """)
    BigDecimal getCOGS(@Param("from") LocalDate from, @Param("to") LocalDate to);


}
