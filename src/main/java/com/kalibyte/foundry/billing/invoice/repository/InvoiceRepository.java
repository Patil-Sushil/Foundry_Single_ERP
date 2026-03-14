package com.kalibyte.foundry.billing.invoice.repository;

import com.kalibyte.foundry.billing.invoice.entity.Invoice;
import com.kalibyte.foundry.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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


}