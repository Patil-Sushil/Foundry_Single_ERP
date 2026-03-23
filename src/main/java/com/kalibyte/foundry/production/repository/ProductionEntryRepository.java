package com.kalibyte.foundry.production.repository;

import com.kalibyte.foundry.production.entity.ProductionEntry;
import com.kalibyte.foundry.production.entity.enums.ProductionShift;
import com.kalibyte.foundry.production.entity.enums.ProductionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductionEntryRepository
        extends JpaRepository<ProductionEntry, UUID>,
        JpaSpecificationExecutor<ProductionEntry> {

    // ── FETCH WITH ITEMS ────────────────────────────

    @Query("""
    SELECT pe FROM ProductionEntry pe
    LEFT JOIN FETCH pe.productionItems pi
    LEFT JOIN FETCH pe.order o
    LEFT JOIN FETCH o.customer
    WHERE pe.id = :id
    AND pe.isDeleted = false
    AND (pi.isDeleted = false OR pi.isDeleted IS NULL)
""")
    Optional<ProductionEntry> findWithItems(@Param("id") UUID id);

    // ── ORDER BASED ─────────────────────────────────

    @Query("""
        SELECT pe FROM ProductionEntry pe
        LEFT JOIN FETCH pe.order o
        LEFT JOIN FETCH o.customer
        WHERE o.id = :orderId
        AND pe.isDeleted = false
        ORDER BY pe.reportDate ASC
    """)
    List<ProductionEntry> findByOrder(@Param("orderId") UUID orderId);

    // ── DATE (WITH ORDER+CUSTOMER — avoids N+1) ────

    @Query("""
        SELECT pe FROM ProductionEntry pe
        LEFT JOIN FETCH pe.order o
        LEFT JOIN FETCH o.customer
        WHERE pe.reportDate = :date
        AND pe.isDeleted = false
    """)
    List<ProductionEntry> findByDateWithOrder(@Param("date") LocalDate date);

    // ── DATE RANGE (WITH ORDER+CUSTOMER) ────────────

    @Query("""
        SELECT pe FROM ProductionEntry pe
        LEFT JOIN FETCH pe.order o
        LEFT JOIN FETCH o.customer
        WHERE pe.reportDate BETWEEN :from AND :to
        AND pe.isDeleted = false
    """)
    List<ProductionEntry> findByDateRangeWithOrder(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    // ── STATUS ──────────────────────────────────────

    @Query("""
        SELECT pe FROM ProductionEntry pe
        WHERE pe.status = :status
        AND pe.isDeleted = false
    """)
    List<ProductionEntry> findByStatus(@Param("status") ProductionStatus status);

    // ── DUPLICATE CHECK — FIX: was Enum, now ProductionShift ──

    boolean existsByOrderIdAndReportDateAndShiftAndIsDeletedFalse(
            UUID orderId,
            LocalDate reportDate,
            ProductionShift shift
    );

//     ── PAGINATED WITH SPEC (WITH ORDER+CUSTOMER) ────────────
    @EntityGraph(attributePaths = {"order", "order.customer"})
    Page<ProductionEntry> findAll(Specification<ProductionEntry> spec, Pageable pageable);


    // ── DASHBOARD HELPERS ───────────────────────────

    @Query("""
        SELECT COUNT(DISTINCT pe.order.id)
        FROM ProductionEntry pe
        WHERE pe.status = :status
        AND pe.isDeleted = false
    """)
    long countDistinctOrdersByStatus(@Param("status") ProductionStatus status);

    @Query("""
    SELECT COALESCE(SUM(oi.quantity), 0) - COALESCE(SUM(pi.dispatchedQuantity), 0)
    FROM OrderItem oi
    JOIN Order o ON oi.order.id = o.id
    LEFT JOIN ProductionItem pi ON pi.orderItem.id = oi.id AND pi.isDeleted = false
    WHERE o.status IN ('CREATED', 'IN_PROGRESS', 'CONFIRMED')
""")
    int calculateTotalPendingDispatch();


//    ── ENTRY NUMBER SEQUENCE HELPERS ─────────────────
    long countByEntryNumberStartingWith(String prefix);
}
