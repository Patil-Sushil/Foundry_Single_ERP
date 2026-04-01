package com.kalibyte.foundry.qa.rejection.repository;

import com.kalibyte.foundry.qa.rejection.entity.QaRejection;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QaRejectionRepository extends JpaRepository<QaRejection, Long>, JpaSpecificationExecutor<QaRejection> {
    @EntityGraph(attributePaths = {"inspection", "order", "orderItem", "primaryDefect"})
    Optional<QaRejection> findWithDetailsById(Long id);

    @Override
    @EntityGraph(attributePaths = {"inspection", "order", "orderItem", "primaryDefect"})
    List<QaRejection> findAll(Specification<QaRejection> spec);

    Optional<QaRejection> findByRejectionNumber(String rejectionNumber);
    long countByRejectionNumberStartingWith(String prefix);
    List<QaRejection> findByOrderId(UUID orderId);
}
