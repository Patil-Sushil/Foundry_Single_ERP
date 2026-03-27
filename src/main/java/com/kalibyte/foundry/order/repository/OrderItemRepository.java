package com.kalibyte.foundry.order.repository;

import com.kalibyte.foundry.order.entity.OrderItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, UUID>,
        JpaSpecificationExecutor<OrderItem> {

    // Find all items by order ID
    List<OrderItem> findByOrderId(UUID orderId);

    Page<OrderItem> findByOrderId(UUID orderId, Pageable pageable);

    // Find all items with order details eagerly loaded
    @Query("SELECT oi FROM OrderItem oi " +
            "JOIN FETCH oi.order o " +
            "JOIN FETCH o.customer " +
            "LEFT JOIN FETCH oi.pattern " +
            "LEFT JOIN FETCH oi.patternReceipt " +
            "WHERE oi.id = :id")
    java.util.Optional<OrderItem> findWithDetailsById(@Param("id") UUID id);

    // Find items by part name (search)
    @Query("SELECT oi FROM OrderItem oi " +
            "JOIN FETCH oi.order o " +
            "JOIN FETCH o.customer " +
            "WHERE LOWER(oi.partName) LIKE LOWER(CONCAT('%', :partName, '%'))")
    List<OrderItem> findByPartNameContainingIgnoreCase(@Param("partName") String partName);

    // Find items by customer
    @Query("SELECT oi FROM OrderItem oi " +
            "JOIN oi.order o " +
            "WHERE o.customer.id = :customerId")
    Page<OrderItem> findByCustomerId(@Param("customerId") UUID customerId, Pageable pageable);

    // Count items by order
    long countByOrderId(UUID orderId);

    // Find pending items (produced < quantity)
    @Query("SELECT oi FROM OrderItem oi " +
            "JOIN oi.order o " +
            "WHERE oi.producedQuantity < oi.quantity " +
            "AND o.status NOT IN ('CANCELLED', 'COMPLETED')")
    Page<OrderItem> findPendingItems(Pageable pageable);
}