package com.kalibyte.foundry.billing.deliveryChallan.repository;

import com.kalibyte.foundry.billing.deliveryChallan.entity.DeliveryChallan;
import com.kalibyte.foundry.order.entity.Order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeliveryChallanRepository extends JpaRepository<DeliveryChallan, UUID> {

    Optional<DeliveryChallan> findTopByDcNumberStartingWithOrderByDcNumberDesc(String prefix);

    List<DeliveryChallan> findByOrder(Order order);
    @Query("""
        SELECT DISTINCT dc FROM DeliveryChallan dc
        LEFT JOIN FETCH dc.items
        """)
    Page<DeliveryChallan> findAllWithItems(Pageable pageable);
    @Query("""
        SELECT dc FROM DeliveryChallan dc
        LEFT JOIN FETCH dc.items
        WHERE dc.id = :id
        """)
    Optional<DeliveryChallan> findByIdWithItems(UUID id);

}