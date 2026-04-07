package com.kalibyte.foundry.furnace.furnace_heats.repository;

import com.kalibyte.foundry.furnace.furnace_heats.entity.ElectricityRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ElectricityRateRepository extends JpaRepository<ElectricityRate, Long> {

    Optional<ElectricityRate> findByActiveTrue();

    /**
     * Find all electricity rates that were effective during a given date range.
     * 
     * A rate is applicable if:
     * - effectiveFrom <= endDate AND
     * - (effectiveTo IS NULL OR effectiveTo >= startDate)
     * 
     * This captures all rates that overlap with the query range.
     */
    @Query("""
        SELECT er FROM ElectricityRate er
        WHERE er.effectiveFrom <= :endDate
          AND (er.effectiveTo IS NULL OR er.effectiveTo >= :startDate)
        ORDER BY er.effectiveFrom ASC
    """)
    List<ElectricityRate> findRatesEffectiveBetween(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * Find the electricity rate that was effective on a specific date.
     * 
     * Used to get the correct rate for a single heat's date.
     */
    @Query("""
        SELECT er FROM ElectricityRate er
        WHERE er.effectiveFrom <= :date
          AND (er.effectiveTo IS NULL OR er.effectiveTo >= :date)
        ORDER BY er.effectiveFrom DESC
    """)
    Optional<ElectricityRate> findRateEffectiveOn(@Param("date") LocalDate date);

    /**
     * Get all rates ordered by effective date for historical view.
     */
    List<ElectricityRate> findAllByOrderByEffectiveFromDesc();
}
