package com.kalibyte.foundry.order.repository;

import com.kalibyte.foundry.order.entity.Order;
import com.kalibyte.foundry.order.entity.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID>, JpaSpecificationExecutor<Order> {

    boolean existsByQuotationId(UUID quotationId);

    Optional<Order> findByOrderNumber(String orderNumber);

    @Query("SELECT o FROM Order o " +
            "LEFT JOIN FETCH o.customer " +
            "LEFT JOIN FETCH o.quotation " +
            "LEFT JOIN FETCH o.items " +
            "WHERE o.id = :id")
    Optional<Order> findWithDetailsById(@Param("id") UUID id);

    List<Order> findByStatus(OrderStatus status);

    Page<Order> findByCustomerId(UUID customerId, Pageable pageable);

    Page<Order> findByStatusIn(List<OrderStatus> statuses, Pageable pageable);

    long countByStatus(OrderStatus status);

    Optional<Order> findTopByOrderNumberStartingWithOrderByOrderNumberDesc(String prefix);
}