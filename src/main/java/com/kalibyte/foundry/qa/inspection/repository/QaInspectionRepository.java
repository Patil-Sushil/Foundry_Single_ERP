package com.kalibyte.foundry.qa.inspection.repository;

import com.kalibyte.foundry.qa.inspection.entity.QaInspection;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QaInspectionRepository extends JpaRepository<QaInspection, Long>, JpaSpecificationExecutor<QaInspection> {
    @EntityGraph(attributePaths = {"order", "orderItem", "findings", "findings.defect", "productionEntry", "productionItem", "heatOrderItem"})
    Optional<QaInspection> findWithDetailsById(Long id);

    @Override
    @EntityGraph(attributePaths = {"order", "orderItem", "findings", "findings.defect", "productionEntry", "productionItem", "heatOrderItem"})
    List<QaInspection> findAll(Specification<QaInspection> spec);

    Optional<QaInspection> findByInspectionNumber(String inspectionNumber);
    long countByInspectionNumberStartingWith(String prefix);
    List<QaInspection> findByProductionEntryId(UUID productionEntryId);
    List<QaInspection> findByOrderId(UUID orderId);

    @Query("SELECT COALESCE(SUM(qi.totalInspected), 0), COALESCE(SUM(qi.totalRejected), 0) FROM QaInspection qi WHERE qi.inspectionDate = :date")
    Object[] sumRejectionStatsByDate(@Param("date") java.time.LocalDate date);

    @Query("SELECT COALESCE(SUM(qi.totalInspected), 0), COALESCE(SUM(qi.totalRejected), 0) FROM QaInspection qi WHERE qi.inspectionDate BETWEEN :start AND :end")
    Object[] sumRejectionStatsBetweenDates(@Param("start") java.time.LocalDate start, @Param("end") java.time.LocalDate end);

    @Query("SELECT d.code, COUNT(f) FROM InspectionFinding f JOIN f.defect d JOIN f.inspection qi WHERE qi.inspectionDate BETWEEN :start AND :end GROUP BY d.code ORDER BY COUNT(f) DESC")
    List<Object[]> findTopDefectsByDate(@Param("start") java.time.LocalDate start, @Param("end") java.time.LocalDate end, org.springframework.data.domain.Pageable pageable);
}
