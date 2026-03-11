package com.kalibyte.foundry.furnace.furnace_heats.repository;

import com.kalibyte.foundry.furnace.furnace_heats.entity.FurnaceHeats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FurnaceHeatsRepository extends JpaRepository<FurnaceHeats, Long> {
    @Query("SELECT DISTINCT h FROM FurnaceHeats h LEFT JOIN FETCH h.materialsUsed WHERE h.furnace.id = :furnaceId")
    List<FurnaceHeats> findByFurnaceIdWithMaterials(@Param("furnaceId") Long furnaceId);

    @Query("SELECT DISTINCT h FROM FurnaceHeats h LEFT JOIN FETCH h.materialsUsed WHERE h.order.id = :orderId")
    List<FurnaceHeats> findByOrderIdWithMaterials(@Param("orderId") UUID orderId);

    List<FurnaceHeats> findByFurnaceId(Long furnaceId);
}
