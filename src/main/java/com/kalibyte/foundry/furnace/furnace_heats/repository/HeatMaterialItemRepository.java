package com.kalibyte.foundry.furnace.furnace_heats.repository;

import com.kalibyte.foundry.furnace.furnace_heats.entity.HeatMaterialItem;
import com.kalibyte.foundry.furnace.furnace_heats.entity.HeatMaterialType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HeatMaterialItemRepository extends JpaRepository<HeatMaterialItem, Long> {
    List<HeatMaterialItem> findByHeatIdAndMaterialType(Long heatId, HeatMaterialType type);
}
