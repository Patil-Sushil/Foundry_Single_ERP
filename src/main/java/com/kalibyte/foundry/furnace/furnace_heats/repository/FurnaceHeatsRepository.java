package com.kalibyte.foundry.furnace.furnace_heats.repository;

import com.kalibyte.foundry.furnace.furnace_heats.entity.FurnaceHeats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface FurnaceHeatsRepository extends JpaRepository<FurnaceHeats, Long> {
    @Query("SELECT DISTINCT h FROM FurnaceHeats h LEFT JOIN FETCH h.materialsUsed WHERE h.furnace.id = :furnaceId")
    List<FurnaceHeats> findByFurnaceIdWithMaterials(@Param("furnaceId") Long furnaceId);

    @Query("SELECT DISTINCT h FROM FurnaceHeats h LEFT JOIN FETCH h.materialsUsed WHERE h.order.id = :orderId")
    List<FurnaceHeats> findByOrderIdWithMaterials(@Param("orderId") UUID orderId);

    @Query("SELECT COALESCE(SUM(mi.totalCost), 0) FROM HeatMaterialItem mi JOIN mi.heat h JOIN h.furnace f WHERE f.date BETWEEN :from AND :to")
    BigDecimal getTotalMaterialCost(@Param("from") java.time.LocalDate from, @Param("to") java.time.LocalDate to);

    @Query("SELECT h FROM FurnaceHeats h JOIN h.furnace f WHERE f.date BETWEEN :from AND :to")
    List<FurnaceHeats> findHeatsInDateRange(@Param("from") java.time.LocalDate from, @Param("to") java.time.LocalDate to);

    List<FurnaceHeats> findByFurnaceId(Long furnaceId);

    @Query("SELECT COUNT(h) FROM FurnaceHeats h JOIN h.furnace f WHERE f.date = :date")
    Long countHeatsByDate(@Param("date") java.time.LocalDate date);

    @Query("SELECT COUNT(h) FROM FurnaceHeats h JOIN h.furnace f WHERE f.date BETWEEN :start AND :end")
    Long countHeatsBetweenDates(@Param("start") java.time.LocalDate start, @Param("end") java.time.LocalDate end);

    @Query("SELECT COALESCE(AVG(h.powerToWeight), 0) FROM FurnaceHeats h JOIN h.furnace f WHERE f.date = :date")
    java.math.BigDecimal averagePowerToWeightByDate(@Param("date") java.time.LocalDate date);

    @Query("SELECT COALESCE(AVG(h.powerToWeight), 0) FROM FurnaceHeats h JOIN h.furnace f WHERE f.date BETWEEN :start AND :end")
    java.math.BigDecimal averagePowerToWeightBetweenDates(@Param("start") java.time.LocalDate start, @Param("end") java.time.LocalDate end);

    @Query("SELECT COALESCE(SUM(h.liquidMetalWeight), 0) FROM FurnaceHeats h JOIN h.furnace f WHERE f.date = :date")
    java.math.BigDecimal sumLiquidMetalWeightByDate(@Param("date") java.time.LocalDate date);

    @Query("SELECT COALESCE(SUM(h.liquidMetalWeight), 0) FROM FurnaceHeats h JOIN h.furnace f WHERE f.date BETWEEN :start AND :end")
    java.math.BigDecimal sumLiquidMetalWeightBetweenDates(@Param("start") java.time.LocalDate start, @Param("end") java.time.LocalDate end);

    @Query("SELECT COALESCE(SUM(h.totalWeight), 0) FROM FurnaceHeats h JOIN h.furnace f WHERE f.date = :date")
    java.math.BigDecimal sumTotalChargeWeightByDate(@Param("date") java.time.LocalDate date);

    @Query("SELECT COALESCE(SUM(h.totalWeight), 0) FROM FurnaceHeats h JOIN h.furnace f WHERE f.date BETWEEN :start AND :end")
    java.math.BigDecimal sumTotalChargeWeightBetweenDates(@Param("start") java.time.LocalDate start, @Param("end") java.time.LocalDate end);

    @Query("SELECT COALESCE(SUM(h.totalProcessScrap), 0) FROM FurnaceHeats h JOIN h.furnace f WHERE f.date = :date")
    java.math.BigDecimal sumScrapGeneratedByDate(@Param("date") java.time.LocalDate date);

    @Query("SELECT COALESCE(SUM(h.totalProcessScrap), 0) FROM FurnaceHeats h JOIN h.furnace f WHERE f.date BETWEEN :start AND :end")
    java.math.BigDecimal sumScrapGeneratedBetweenDates(@Param("start") java.time.LocalDate start, @Param("end") java.time.LocalDate end);
}
