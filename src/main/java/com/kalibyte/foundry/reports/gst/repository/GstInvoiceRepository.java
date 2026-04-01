package com.kalibyte.foundry.reports.gst.repository;

import com.kalibyte.foundry.billing.invoice.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface GstInvoiceRepository extends JpaRepository<Invoice, UUID> {

    // ================================================
    // B2B: Customers WITH valid GSTIN
    // ================================================
    @Query("""
        SELECT i
        FROM Invoice i
        JOIN FETCH i.order o
        JOIN FETCH o.customer c
        LEFT JOIN FETCH i.items
        WHERE i.invoiceDate BETWEEN :from AND :to
        AND i.billStatus <> 'CANCELLED'
        AND c.gstNumber IS NOT NULL
        AND TRIM(c.gstNumber) <> ''
        ORDER BY c.gstNumber, i.invoiceDate
    """)
    List<Invoice> findB2BInvoices(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    // ================================================
    // B2C LARGE: No GSTIN + Interstate + > ₹2,50,000
    // ================================================
    @Query("""
        SELECT i
        FROM Invoice i
        JOIN FETCH i.order o
        JOIN FETCH o.customer c
        LEFT JOIN FETCH i.items
        WHERE i.invoiceDate BETWEEN :from AND :to
        AND i.billStatus <> 'CANCELLED'
        AND (c.gstNumber IS NULL OR TRIM(c.gstNumber) = '')
        AND i.gstType = 'IGST'
        AND i.totalAmount > 250000
        ORDER BY i.invoiceDate
    """)
    List<Invoice> findB2CLargeInvoices(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    // ================================================
    // B2C SMALL: No GSTIN + (Intrastate OR Interstate <= ₹2,50,000)
    // ================================================
    @Query("""
        SELECT i
        FROM Invoice i
        JOIN FETCH i.order o
        JOIN FETCH o.customer c
        LEFT JOIN FETCH i.items
        WHERE i.invoiceDate BETWEEN :from AND :to
        AND i.billStatus <> 'CANCELLED'
        AND (c.gstNumber IS NULL OR TRIM(c.gstNumber) = '')
        AND (i.gstType = 'CGST_SGST' OR (i.gstType = 'IGST' AND i.totalAmount <= 250000))
        ORDER BY i.invoiceDate
    """)
    List<Invoice> findB2CSmallInvoices(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    // ================================================
    // ALL NON-CANCELLED INVOICES (Sales Register)
    // ================================================
    @Query("""
        SELECT i
        FROM Invoice i
        JOIN FETCH i.order o
        JOIN FETCH o.customer c
        LEFT JOIN FETCH i.items
        WHERE i.invoiceDate BETWEEN :from AND :to
        AND i.billStatus <> 'CANCELLED'
        ORDER BY i.invoiceDate, i.invoiceNumber
    """)
    List<Invoice> findAllInvoicesForPeriod(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    // ================================================
// DOCUMENT SUMMARY (FIXED - returns List<Object[]>)
// ================================================
    @Query("""
    SELECT 
        COALESCE(MIN(i.invoiceNumber), 'N/A'),
        COALESCE(MAX(i.invoiceNumber), 'N/A'),
        COUNT(i)
    FROM Invoice i
    WHERE i.invoiceDate BETWEEN :from AND :to
    AND i.billStatus <> 'CANCELLED'
""")
    List<Object[]> getActiveInvoiceSummary(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    @Query("""
        SELECT COUNT(i)
        FROM Invoice i
        WHERE i.invoiceDate BETWEEN :from AND :to
        AND i.billStatus = 'CANCELLED'
    """)
    Long getCancelledInvoiceCount(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    // ================================================
    // MONTHLY BREAKDOWN
    // ================================================
    @Query("""
        SELECT FUNCTION('DATE_TRUNC', 'month', i.invoiceDate),
               COUNT(i),
               COALESCE(SUM(i.subtotal), 0),
               COALESCE(SUM(i.cgst), 0),
               COALESCE(SUM(i.sgst), 0),
               COALESCE(SUM(i.igst), 0),
               COALESCE(SUM(i.totalGst), 0)
        FROM Invoice i
        WHERE i.invoiceDate BETWEEN :from AND :to
        AND i.billStatus <> 'CANCELLED'
        GROUP BY FUNCTION('DATE_TRUNC', 'month', i.invoiceDate)
        ORDER BY FUNCTION('DATE_TRUNC', 'month', i.invoiceDate)
    """)
    List<Object[]> getMonthlyGstBreakdown(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    // ================================================
    // HSN DATA (from invoice items + order items)
    // ================================================
    @Query("""
        SELECT oi.materialGrade,
               oi.partName,
               SUM(ii.quantity),
               SUM(ii.weight),
               SUM(ii.amount),
               ii.gstPercentage,
               SUM(ii.gstAmount),
               COALESCE(SUM(CASE WHEN i.gstType = 'CGST_SGST'
                   THEN ii.gstAmount / 2 ELSE 0 END), 0),
               COALESCE(SUM(CASE WHEN i.gstType = 'CGST_SGST'
                   THEN ii.gstAmount / 2 ELSE 0 END), 0),
               COALESCE(SUM(CASE WHEN i.gstType = 'IGST'
                   THEN ii.gstAmount ELSE 0 END), 0)
        FROM InvoiceItem ii
        JOIN ii.invoice i
        JOIN ii.orderItem oi
        WHERE i.invoiceDate BETWEEN :from AND :to
        AND i.billStatus <> 'CANCELLED'
        GROUP BY oi.materialGrade, oi.partName, ii.gstPercentage
        ORDER BY oi.materialGrade
    """)
    List<Object[]> getHsnSummaryData(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );
}