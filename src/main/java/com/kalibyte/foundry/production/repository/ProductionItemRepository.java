package com.kalibyte.foundry.production.repository;

import com.kalibyte.foundry.production.entity.ProductionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductionItemRepository extends JpaRepository<ProductionItem, UUID> {

    @Query("""
        SELECT pi FROM ProductionItem pi
        WHERE pi.productionEntry.id = :entryId
        AND pi.isDeleted = false
    """)
    List<ProductionItem> findByEntry(@Param("entryId") UUID entryId);

    @Query("""
        SELECT pi FROM ProductionItem pi
        WHERE pi.orderItem.id = :orderItemId
        AND pi.isDeleted = false
    """)
    List<ProductionItem> findByOrderItem(@Param("orderItemId") UUID orderItemId);

    @Query("""
        SELECT COALESCE(SUM(pi.dispatchedQuantity), 0)
        FROM ProductionItem pi
        WHERE pi.orderItem.id = :orderItemId
        AND pi.isDeleted = false
    """)
    int getTotalDispatched(@Param("orderItemId") UUID orderItemId);

    @Query("""
        SELECT COALESCE(SUM(pi.acceptedQuantity), 0)
        FROM ProductionItem pi
        WHERE pi.orderItem.id = :orderItemId
        AND pi.isDeleted = false
    """)
    int getTotalAcceptedQuantity(@Param("orderItemId") UUID orderItemId);

    @Query("""
        SELECT COALESCE(SUM(pi.acceptedQuantity), 0) / 7.0
        FROM ProductionItem pi
        WHERE pi.orderItem.id = :orderItemId
        AND pi.productionEntry.reportDate >= :startDate
        AND pi.isDeleted = false
    """)
    Double getAverageAcceptedQuantity(@Param("orderItemId") UUID orderItemId, @Param("startDate") java.time.LocalDate startDate);

    @Query("""
        SELECT 
            COALESCE(SUM(pi.pouredMoulds) - SUM(pi.shotBlastingQuantity), 0),
            COALESCE(SUM(pi.shotBlastingQuantity) - SUM(pi.fettlingQuantity), 0),
            COALESCE(SUM(pi.fettlingQuantity) - SUM(pi.inspectedQuantity), 0)
        FROM ProductionItem pi
        WHERE pi.isDeleted = false
    """)
    List<Object[]> getOverallWipTotals();

    // ── Pipeline totals for an order item ──
    @Query("""
        SELECT
            COALESCE(SUM(pi.readyCores), 0),
            COALESCE(SUM(pi.pouredMoulds), 0),
            COALESCE(SUM(pi.shotBlastingQuantity), 0),
            COALESCE(SUM(pi.fettlingQuantity), 0),
            COALESCE(SUM(pi.dispatchedQuantity), 0),
            COALESCE(SUM(pi.rejectedQuantity), 0),
            COALESCE(SUM(pi.inspectedQuantity), 0),
            COALESCE(SUM(pi.acceptedQuantity), 0)
        FROM ProductionItem pi
        WHERE pi.orderItem.id = :orderItemId
        AND pi.isDeleted = false
    """)
    List<Object[]> getPipelineTotalsRaw(@Param("orderItemId") UUID orderItemId);

    // ── Pipeline totals EXCLUDING a specific entry (for update) ──
    @Query("""
        SELECT
            COALESCE(SUM(pi.readyCores), 0),
            COALESCE(SUM(pi.pouredMoulds), 0),
            COALESCE(SUM(pi.shotBlastingQuantity), 0),
            COALESCE(SUM(pi.fettlingQuantity), 0),
            COALESCE(SUM(pi.dispatchedQuantity), 0),
            COALESCE(SUM(pi.rejectedQuantity), 0),
            COALESCE(SUM(pi.inspectedQuantity), 0),
            COALESCE(SUM(pi.acceptedQuantity), 0)
        FROM ProductionItem pi
        WHERE pi.orderItem.id = :orderItemId
        AND pi.productionEntry.id != :excludeEntryId
        AND pi.isDeleted = false
    """)
    List<Object[]> getPipelineTotalsExcluding(
            @Param("orderItemId") UUID orderItemId,
            @Param("excludeEntryId") UUID excludeEntryId
    );
    }