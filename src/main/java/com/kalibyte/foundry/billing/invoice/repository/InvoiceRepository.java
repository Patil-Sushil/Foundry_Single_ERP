package com.kalibyte.foundry.billing.invoice.repository;

import com.kalibyte.foundry.billing.invoice.entity.Invoice;
import com.kalibyte.foundry.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    //------------------------------------------------
    // BASIC QUERIES
    //------------------------------------------------

    Optional<Invoice> findTopByInvoiceNumberStartingWithOrderByInvoiceNumberDesc(String prefix);

    Optional<Invoice> findByOrder(Order order);

    Page<Invoice> findAll(Pageable pageable);

    //------------------------------------------------
    // FETCH INVOICE WITH ORDER + CUSTOMER
    //------------------------------------------------

    /**
     * Used when invoice + customer details are required together.
     * Prevents LazyInitializationException.
     */
    @Query("""
        SELECT i
        FROM Invoice i
        JOIN FETCH i.order o
        JOIN FETCH o.customer
        WHERE i.id = :id
        """)
    Optional<Invoice> findByIdWithCustomer(UUID id);

    //------------------------------------------------
    // CUSTOMER INVOICE TOTALS (Outstanding Report)
    //------------------------------------------------

    /**
     * Returns total invoiced amount per customer.
     */
    @Query("""
        SELECT
        i.order.customer.id,
        i.order.customer.name,
        i.order.customer.companyName,
        SUM(i.totalAmount),
        MIN(i.invoiceDate)
        FROM Invoice i
        WHERE i.billStatus <> 'CANCELLED'
        GROUP BY
        i.order.customer.id,
        i.order.customer.name,
        i.order.customer.companyName
        """)
    List<Object[]> getCustomerInvoiceTotals();

    //------------------------------------------------
    // OLDEST UNPAID INVOICE (Outstanding Report)
    //------------------------------------------------

    /**
     * Returns oldest unpaid invoice date for each customer.
     */
    @Query("""
        SELECT
        i.order.customer.id,
        MIN(i.invoiceDate)
        FROM Invoice i
        WHERE i.billStatus IN ('UNPAID','PARTIALLY_PAID')
        GROUP BY i.order.customer.id
        """)
    List<Object[]> getOldestUnpaidInvoices();

    //------------------------------------------------
    // INVOICES BY CUSTOMER AND DATE RANGE
    // (Used in Ledger Report)
    //------------------------------------------------

    @Query("""
        SELECT i
        FROM Invoice i
        WHERE i.order.customer.id = :customerId
        AND i.invoiceDate BETWEEN :from AND :to
        ORDER BY i.invoiceDate
        """)
    List<Invoice> findInvoicesByCustomerAndDateRange(
            UUID customerId,
            java.time.LocalDate from,
            java.time.LocalDate to
    );


    /**
     * Retrieves receivable aging buckets grouped by customer.
     *
     * Business Logic:
     * - Only unpaid or partially paid invoices are considered.
     * - The invoice outstanding amount is grouped into aging buckets
     *   based on the difference between the due date and today's date.
     *
     * Aging Buckets:
     * - Current     : Invoice not yet due
     * - 1–30 days   : Overdue by 1 to 30 days
     * - 31–60 days  : Overdue by 31 to 60 days
     * - 61–90 days  : Overdue by 61 to 90 days
     * - 90+ days    : Overdue by more than 90 days
     *
     * Returns raw aggregated data for further transformation in the service layer.
     */
    @Query(value = """
            SELECT 
            c.id,
            c.name,
            
            SUM(CASE WHEN CURRENT_DATE <= i.due_date THEN i.total_amount ELSE 0 END) AS current_amount,
            
            SUM(CASE WHEN CURRENT_DATE - i.due_date BETWEEN 1 AND 30 
                     THEN i.total_amount ELSE 0 END) AS days1to30,
            
            SUM(CASE WHEN CURRENT_DATE - i.due_date BETWEEN 31 AND 60 
                     THEN i.total_amount ELSE 0 END) AS days31to60,
            
            SUM(CASE WHEN CURRENT_DATE - i.due_date BETWEEN 61 AND 90 
                     THEN i.total_amount ELSE 0 END) AS days61to90,
            
            SUM(CASE WHEN CURRENT_DATE - i.due_date > 90 
                     THEN i.total_amount ELSE 0 END) AS days90plus
            
            FROM invoices i
            JOIN orders o ON i.order_id = o.id
            JOIN customer c ON o.customer_id = c.id
            
            WHERE i.bill_status IN ('UNPAID','PARTIALLY_PAID')
            
            GROUP BY c.id, c.name
            """, nativeQuery = true)
    List<Object[]> getReceivableAging();



    /**
     * Calculates total invoiced revenue for the given date range.
     *
     * @param from start date
     * @param to   end date
     * @return total invoice amount
     */
    @Query("""
        SELECT COALESCE(SUM(i.totalAmount),0)
        FROM Invoice i
        WHERE i.invoiceDate BETWEEN :from AND :to
        """)
    BigDecimal getTotalRevenue(LocalDate from, LocalDate to);

    /**
     * Returns the number of invoices generated within the given period.
     */
    @Query("""
            SELECT COUNT(i)
            FROM Invoice i
            WHERE i.invoiceDate BETWEEN :from AND :to
            """)
    Long getInvoiceCount(LocalDate from, LocalDate to);

    /**
     * Returns monthly aggregated invoice statistics.
     *
     * Data returned:
     * - Month
     * - Total invoice amount
     * - Invoice count
     */
    @Query("""
        SELECT FUNCTION('DATE_TRUNC','month',i.invoiceDate),
               SUM(i.totalAmount),
               COUNT(i.id)
        FROM Invoice i
        WHERE i.invoiceDate BETWEEN :from AND :to
        GROUP BY FUNCTION('DATE_TRUNC','month',i.invoiceDate)
        ORDER BY FUNCTION('DATE_TRUNC','month',i.invoiceDate)
        """)
    List<Object[]> getMonthlyInvoiceStats(LocalDate from, LocalDate to);

    /**
     * Returns customers ranked by revenue generated.
     *
     * Limited using Pageable to fetch top N customers.
     */
    @Query("""
        SELECT c.name, SUM(i.totalAmount)
        FROM Invoice i
        JOIN i.order.customer c
        WHERE i.invoiceDate BETWEEN :from AND :to
        GROUP BY c.name
        ORDER BY SUM(i.totalAmount) DESC
        """)
    List<Object[]> getTopCustomerRevenue(LocalDate from, LocalDate to, Pageable pageable);

    /**
     * Returns invoices whose due date has passed.
     */
    @Query("""
        SELECT i
        FROM Invoice i
        WHERE i.dueDate < CURRENT_DATE
        AND i.billStatus <> 'PAID'
    """)
    Page<Invoice> findOverdueInvoices(Pageable pageable);

    /**
     * Returns overdue totals grouped by customer.
     */
    @Query("""
        SELECT c.name,
               SUM(i.totalAmount),
               COUNT(i),
               MIN(i.invoiceDate)
        FROM Invoice i
        JOIN i.order.customer c
        WHERE i.dueDate < CURRENT_DATE
        AND i.billStatus <> 'PAID'
        GROUP BY c.name
        ORDER BY SUM(i.totalAmount) DESC
        """)
    List<Object[]> getCustomerOverdueSummary();

    /**
     * Returns total invoiced revenue for the given period.
     */
    @Query("""
        SELECT COALESCE(SUM(i.totalAmount),0)
        FROM Invoice i
        WHERE i.invoiceDate BETWEEN :from AND :to
        """)
    BigDecimal getRevenue(LocalDate from, LocalDate to);

    /**
     * Returns month-wise revenue totals for trend analysis.
     * Result: [month, totalRevenue]
     */
    @Query("""
        SELECT FUNCTION('DATE_TRUNC','month',i.invoiceDate),
               SUM(i.totalAmount)
        FROM Invoice i
        WHERE i.invoiceDate BETWEEN :from AND :to
        GROUP BY FUNCTION('DATE_TRUNC','month',i.invoiceDate)
        ORDER BY FUNCTION('DATE_TRUNC','month',i.invoiceDate)
        """)
    List<Object[]> getMonthlyRevenue(LocalDate from, LocalDate to);

    /**
     * Returns invoice GST details for sales register.
     */
    @Query("""
        SELECT i.invoiceNumber,
               i.invoiceDate,
               c.name,
               c.gstNumber,
               i.totalAmount,
               i.cgst,
               i.sgst,
               i.igst
        FROM Invoice i
        JOIN i.order.customer c
        WHERE i.invoiceDate BETWEEN :from AND :to
        """)
    List<Object[]> getGstSales(LocalDate from, LocalDate to);

    /**
     * Calculates total GST collected from sales.
     */
    @Query("""
        SELECT COALESCE(SUM(i.cgst),0),
               COALESCE(SUM(i.sgst),0),
               COALESCE(SUM(i.igst),0),
               COALESCE(SUM(i.totalAmount),0)
        FROM Invoice i
        WHERE i.invoiceDate BETWEEN :from AND :to
        """)
    Object[] getOutputTaxSummary(LocalDate from, LocalDate to);

    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i WHERE i.billStatus <> com.kalibyte.foundry.billing.invoice.entity.enums.InvoiceStatus.PAID AND i.billStatus <> com.kalibyte.foundry.billing.invoice.entity.enums.InvoiceStatus.CANCELLED")
    BigDecimal sumTotalReceivables();

    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.billStatus <> com.kalibyte.foundry.billing.invoice.entity.enums.InvoiceStatus.PAID AND i.billStatus <> com.kalibyte.foundry.billing.invoice.entity.enums.InvoiceStatus.CANCELLED AND i.dueDate < :today")
    Long countOverdueInvoices(@Param("today") LocalDate today);

    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i WHERE i.billStatus <> com.kalibyte.foundry.billing.invoice.entity.enums.InvoiceStatus.PAID AND i.billStatus <> com.kalibyte.foundry.billing.invoice.entity.enums.InvoiceStatus.CANCELLED AND i.dueDate < :today")
    BigDecimal sumOverdueInvoicesValue(@Param("today") LocalDate today);
}