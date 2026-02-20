package com.kalibyte.foundry.inventory.purchaseorder.repository;

import com.kalibyte.foundry.inventory.purchaseorder.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
