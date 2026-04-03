package com.kalibyte.foundry.labors.payout.repository;

import com.kalibyte.foundry.labors.payout.entity.WeeklyPayout;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WeeklyPayoutRepository extends JpaRepository<WeeklyPayout, Long> {
    @Query("SELECT wp " +
            "       FROM WeeklyPayout wp" +
            "       JOIN FETCH wp.laborer l" +
            "       WHERE l.id = :laborerId" +
            "       AND wp.weekStartDate = :weekStartDate" +
            "       AND wp.weekEndDate = :weekEndDate")
    Optional<WeeklyPayout> findByLaborerIdAndWeekStartDateAndWeekEndDate(
            @Param("laborerId") Long laborerId,
            @Param("weekStartDate")LocalDate weekStartDate,
            @Param("weekEndDate")LocalDate weekEndDate
    );

    @Query("SELECT wp FROM WeeklyPayout wp JOIN FETCH wp.laborer l WHERE l.id = :laborerId")
    List<WeeklyPayout> findByLaborerId(@Param("laborerId")Long laborerId);

    @Query("SELECT COALESCE(SUM(wp.grossPayout), 0) FROM WeeklyPayout wp WHERE wp.weekEndDate BETWEEN :from AND :to")
    BigDecimal getTotalGrossPayout(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT COALESCE(SUM(wp.netPayout), 0) FROM WeeklyPayout wp WHERE wp.paymentDate BETWEEN :from AND :to AND wp.paymentStatus = 'PAID'")
    BigDecimal getTotalDisbursedPayout(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
