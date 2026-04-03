package com.kalibyte.foundry.labors.attendance.repository;

import com.kalibyte.foundry.labors.attendance.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    
    Optional<Attendance> findByLaborerIdAndWorkDate(Long laborerId, LocalDate workDate);
    
    List<Attendance> findByLaborerIdAndWorkDateBetween(Long laborerId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT a FROM Attendance a JOIN FETCH a.laborer WHERE a.workDate BETWEEN :startDate AND :endDate")
    List<Attendance> findByWorkDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(a.earnedAmount), 0) FROM Attendance a WHERE a.workDate BETWEEN :from AND :to")
    BigDecimal getTotalLaborCost(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
