package com.kalibyte.foundry.inventory.item.repository;

import com.kalibyte.foundry.inventory.item.entity.StockAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockAdjustmentRepository extends JpaRepository<StockAdjustment, Long> {
}
