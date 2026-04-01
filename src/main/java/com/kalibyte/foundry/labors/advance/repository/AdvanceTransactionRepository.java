package com.kalibyte.foundry.labors.advance.repository;

import com.kalibyte.foundry.labors.advance.entity.AdvanceTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface AdvanceTransactionRepository extends JpaRepository<AdvanceTransaction, Long> {
    
    List<AdvanceTransaction> findByLaborerId(Long laborerId);

    @Query("SELECT (COALESCE(SUM(CASE WHEN a.transactionType = 'GIVEN' THEN a.amount ELSE 0 END), 0) - " +
           "COALESCE(SUM(CASE WHEN a.transactionType = 'DEDUCTED' THEN a.amount ELSE 0 END), 0)) " +
           "FROM AdvanceTransaction a WHERE a.laborerId = :laborerId")
    BigDecimal getOutstandingBalance(@Param("laborerId") Long laborerId);
}
