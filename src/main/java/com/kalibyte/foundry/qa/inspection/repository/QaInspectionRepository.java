package com.kalibyte.foundry.qa.inspection.repository;

import com.kalibyte.foundry.qa.inspection.entity.QaInspection;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
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
}
