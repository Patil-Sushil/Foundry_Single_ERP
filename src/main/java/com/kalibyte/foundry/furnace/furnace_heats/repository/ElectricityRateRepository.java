package com.kalibyte.foundry.furnace.furnace_heats.repository;

import com.kalibyte.foundry.furnace.furnace_heats.entity.ElectricityRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ElectricityRateRepository extends JpaRepository<ElectricityRate, Long> {

    Optional<ElectricityRate> findByActiveTrue();
}
