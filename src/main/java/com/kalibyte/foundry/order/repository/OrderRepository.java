package com.kalibyte.foundry.order.repository;

import com.kalibyte.foundry.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID>,
        JpaSpecificationExecutor<Order> {

    boolean existsByQuotationId(UUID quotationId);

    @Query("""
        SELECT o FROM Order o
        LEFT JOIN FETCH o.customer
        LEFT JOIN FETCH o.quotation
        LEFT JOIN FETCH o.items
        WHERE o.id = :id
    """)
    Optional<Order> findWithDetailsById(@Param("id") UUID id);
}