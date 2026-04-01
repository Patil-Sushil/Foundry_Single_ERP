package com.kalibyte.foundry.billing.deliveryChallan.repository;

import com.kalibyte.foundry.billing.deliveryChallan.entity.DeliveryChallan;
import com.kalibyte.foundry.billing.deliveryChallan.entity.DeliveryChallanItem;
import com.kalibyte.foundry.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface DeliveryChallanItemRepository extends JpaRepository<DeliveryChallanItem, UUID> {

    List<DeliveryChallanItem> findByDeliveryChallan_Order(Order order);

    @Query("""
        SELECT COALESCE(SUM(i.quantity), 0)
        FROM DeliveryChallanItem i
        WHERE i.orderItem.id = :orderItemId
        AND i.deliveryChallan.isDeleted = false
    """)
    Integer getTotalDispatchedQuantity(UUID orderItemId);

    List<DeliveryChallanItem> findByDeliveryChallan(DeliveryChallan dc);

}