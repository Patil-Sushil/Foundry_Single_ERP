package com.kalibyte.foundry.billing.creditnote.repository;

import com.kalibyte.foundry.billing.creditnote.entity.CreditNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface CreditNoteRepository extends JpaRepository<CreditNote, UUID> {
    Optional<CreditNote> findByCreditNoteNumber(String creditNoteNumber);
    long countByCreditNoteNumberStartingWith(String prefix);
    Optional<CreditNote> findTopByCreditNoteNumberStartingWithOrderByCreditNoteNumberDesc(String prefix);

    @Query("""
        SELECT cn
        FROM CreditNote cn
        JOIN FETCH cn.customer c
        LEFT JOIN FETCH cn.order o
        WHERE cn.issueDate BETWEEN :from AND :to
        AND cn.status <> 'CANCELLED'
        ORDER BY cn.issueDate, cn.creditNoteNumber
    """)
    List<CreditNote> findOutwardCreditNotes(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    @Query("""
        SELECT oi.materialGrade,
               oi.partName,
               SUM(cr.returnedQuantity),
               SUM(cr.returnedWeight),
               SUM(cn.subtotal),
               cn.gstPercentage,
               SUM(cn.totalGst),
               SUM(cn.cgst),
               SUM(cn.sgst),
               SUM(cn.igst)
        FROM CreditNote cn
        JOIN cn.customerReturn cr
        JOIN cr.orderItem oi
        WHERE cn.issueDate BETWEEN :from AND :to
        AND cn.status <> 'CANCELLED'
        GROUP BY oi.materialGrade, oi.partName, cn.gstPercentage
    """)
    List<Object[]> getCreditNoteHsnSummary(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );
}
