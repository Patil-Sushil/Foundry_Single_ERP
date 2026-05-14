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

    List<Order> findByStatusIn(List<OrderStatus> statuses);

    long countByStatus(OrderStatus status);

    Optional<Order> findTopByOrderNumberStartingWithOrderByOrderNumberDesc(String prefix);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderDate = :date")
    Long countOrdersByDate(@Param("date") java.time.LocalDate date);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderDate BETWEEN :start AND :end")
    Long countOrdersBetweenDates(@Param("start") java.time.LocalDate start, @Param("end") java.time.LocalDate end);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.orderDate = :date")
    java.math.BigDecimal sumOrderValueByDate(@Param("date") java.time.LocalDate date);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderDate BETWEEN :start AND :end AND o.orderType = com.kalibyte.foundry.order.entity.enums.OrderType.DIRECT")
    Long countDirectOrdersBetweenDates(@Param("start") java.time.LocalDate start, @Param("end") java.time.LocalDate end);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderDate BETWEEN :start AND :end AND o.orderType = com.kalibyte.foundry.order.entity.enums.OrderType.QUOTATION")
    Long countQuotationOrdersBetweenDates(@Param("start") java.time.LocalDate start, @Param("end") java.time.LocalDate end);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderDate = :date AND o.orderType = com.kalibyte.foundry.order.entity.enums.OrderType.DIRECT")
    Long countDirectOrdersByDate(@Param("date") java.time.LocalDate date);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderDate = :date AND o.orderType = com.kalibyte.foundry.order.entity.enums.OrderType.QUOTATION")
    Long countQuotationOrdersByDate(@Param("date") java.time.LocalDate date);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.orderDate BETWEEN :start AND :end")
    java.math.BigDecimal sumOrderValueBetweenDates(@Param("start") java.time.LocalDate start, @Param("end") java.time.LocalDate end);

    @Query("SELECT o.customer.id as customerId, o.customer.name as customerName, SUM(o.totalAmount) as totalOrderValue " +
           "FROM Order o WHERE o.orderDate BETWEEN :start AND :end " +
           "GROUP BY o.customer.id, o.customer.name " +
           "ORDER BY SUM(o.totalAmount) DESC")
    List<Object[]> findTopCustomersByOrderValueBetweenDates(@Param("start") java.time.LocalDate start, @Param("end") java.time.LocalDate end, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
    List<Object[]> countOrdersByStatus();
}