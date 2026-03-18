package com.kalibyte.foundry.production.repository;

import com.kalibyte.foundry.production.entity.ProductionEntry;
import com.kalibyte.foundry.production.entity.enums.ProductionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductionEntryRepository extends JpaRepository<ProductionEntry, UUID> {

    //------------------------------------------------
    // FETCH WITH ITEMS
    //------------------------------------------------

    @Query("""
        SELECT pe FROM ProductionEntry pe
        LEFT JOIN FETCH pe.productionItems pi
        LEFT JOIN FETCH pe.order o
        WHERE pe.id = :id AND pe.isDeleted = false
    """)
    Optional<ProductionEntry> findWithItems(@Param("id") UUID id);

    //------------------------------------------------
    // ORDER BASED
    //------------------------------------------------

    @Query("""
        SELECT pe FROM ProductionEntry pe
        WHERE pe.order.id = :orderId
        AND pe.isDeleted = false
        ORDER BY pe.reportDate ASC
    """)
    List<ProductionEntry> findByOrder(@Param("orderId") UUID orderId);

    //------------------------------------------------
    // DATE RANGE
    //------------------------------------------------

    @Query("""
        SELECT pe FROM ProductionEntry pe
        WHERE pe.reportDate BETWEEN :from AND :to
        AND pe.isDeleted = false
    """)
    List<ProductionEntry> findByDateRange(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    //------------------------------------------------
    // DAILY
    //------------------------------------------------

    @Query("""
        SELECT pe FROM ProductionEntry pe
        WHERE pe.reportDate = :date
        AND pe.isDeleted = false
    """)
    List<ProductionEntry> findByDate(@Param("date") LocalDate date);

    //------------------------------------------------
    // STATUS
    //------------------------------------------------

    @Query("""
        SELECT pe FROM ProductionEntry pe
        WHERE pe.status = :status
        AND pe.isDeleted = false
    """)
    List<ProductionEntry> findByStatus(@Param("status") ProductionStatus status);

    //------------------------------------------------
    // DUPLICATE CHECK (Order + Date + Shift)
    //------------------------------------------------

    boolean existsByOrderIdAndReportDateAndShiftAndIsDeletedFalse(
            UUID orderId,
            LocalDate reportDate,
            Enum shift
    );

}
