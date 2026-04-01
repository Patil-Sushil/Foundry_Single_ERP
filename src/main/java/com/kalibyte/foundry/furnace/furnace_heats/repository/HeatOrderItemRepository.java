package com.kalibyte.foundry.furnace.furnace_heats.repository;

import com.kalibyte.foundry.furnace.furnace_heats.entity.HeatOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HeatOrderItemRepository extends JpaRepository<HeatOrderItem, Long> {
    List<HeatOrderItem> findByHeatId(Long heatId);
    List<HeatOrderItem> findByOrderItemId(java.util.UUID orderItemId);
}
