package com.kalibyte.foundry.furnace.furnace_heats.repository;

import com.kalibyte.foundry.furnace.furnace_heats.entity.FurnaceHeats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FurnaceHeatsRepository extends JpaRepository<FurnaceHeats, Long> {
    List<FurnaceHeats> findByFurnaceId(Long furnaceId);
}
