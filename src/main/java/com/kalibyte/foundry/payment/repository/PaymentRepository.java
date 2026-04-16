package com.kalibyte.foundry.payment.repository;

import com.kalibyte.foundry.billing.invoice.entity.Invoice;
import com.kalibyte.foundry.customer.entity.Customer;
import com.kalibyte.foundry.payment.entity.Enums.PaymentStatus;
import com.kalibyte.foundry.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID>,
        JpaSpecificationExecutor<Payment> {

    List<Payment> findByInvoiceOrderByPaymentDateDesc(Invoice invoice);

    List<Payment> findByCustomerOrderByPaymentDateDesc(Customer customer);

    boolean existsByInstrumentNumberAndStatusNot(String instrumentNumber, PaymentStatus status);

    boolean existsByTransactionIdAndStatusNot(String transactionId, PaymentStatus status);

    @Query("""
       SELECT COALESCE(SUM(p.amountPaid), 0)
       FROM Payment p
       WHERE p.invoice.id = :invoiceId
         AND p.status IN ('SUCCESS', 'PARTIAL')
       """)
    BigDecimal getTotalPaid(@Param("invoiceId") UUID invoiceId);


    /**
     * Count active transactions (SUCCESS + PENDING)
     */
    @Query("""
       SELECT COUNT(p)
       FROM Payment p
       WHERE p.invoice.id = :invoiceId
         AND p.status IN ('SUCCESS', 'PENDING')
       """)
    int getTransactionCount(@Param("invoiceId") UUID invoiceId);

    @Query("""
           SELECT p.paymentNumber
           FROM Payment p
           ORDER BY p.createdAt DESC
           LIMIT 1
           """)
    Optional<String> findLastPaymentNumber();

    // ── DAILY COLLECTION REPORT ──
    @Query(value = """
           SELECT
             p.payment_date,
             SUM(p.amount_paid)             AS total_amount,
             COUNT(p.id)                    AS transaction_count,
             SUM(CASE WHEN p.payment_method = 'CASH'          THEN p.amount_paid ELSE 0 END) AS cash_amount,
             SUM(CASE WHEN p.payment_method = 'UPI'           THEN p.amount_paid ELSE 0 END) AS upi_amount,
             SUM(CASE WHEN p.payment_method = 'BANK_TRANSFER' THEN p.amount_paid ELSE 0 END) AS bank_transfer_amount,
             SUM(CASE WHEN p.payment_method = 'CHEQUE'        THEN p.amount_paid ELSE 0 END) AS cheque_amount,
             SUM(CASE WHEN p.payment_method = 'CARD'          THEN p.amount_paid ELSE 0 END) AS card_amount,
             SUM(CASE WHEN p.payment_method = 'NEFT'          THEN p.amount_paid ELSE 0 END) AS neft_amount,
             SUM(CASE WHEN p.payment_method = 'RTGS'          THEN p.amount_paid ELSE 0 END) AS rtgs_amount,
             SUM(CASE WHEN p.payment_method = 'IMPS'          THEN p.amount_paid ELSE 0 END) AS imps_amount
           FROM payments p
           WHERE p.payment_date BETWEEN :from AND :to
             AND p.status IN ('SUCCESS', 'PARTIAL')
           GROUP BY p.payment_date
           ORDER BY p.payment_date
           """, nativeQuery = true)
    List<Object[]> getDailyCollection(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    @Query("""
           SELECT COALESCE(SUM(p.amountPaid), 0)
           FROM Payment p
           WHERE p.paymentDate BETWEEN :from AND :to
             AND p.status IN ('SUCCESS', 'PARTIAL')
           """)
    BigDecimal getTotalCollection(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    @Query("""
           SELECT p.paymentMethod, SUM(p.amountPaid), COUNT(p)
           FROM Payment p
           WHERE p.paymentDate BETWEEN :from AND :to
             AND p.status IN ('SUCCESS', 'PARTIAL')
           GROUP BY p.paymentMethod
           """)
    List<Object[]> getMethodWiseCollection(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    @Query("""
           SELECT p.customer.id, p.customer.name, SUM(p.amountPaid)
           FROM Payment p
           WHERE p.paymentDate BETWEEN :from AND :to
             AND p.status IN ('SUCCESS', 'PARTIAL')
           GROUP BY p.customer.id, p.customer.name
           ORDER BY SUM(p.amountPaid) DESC
           """)
    List<Object[]> getTopCustomers(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    @Query("""
           SELECT p.customer.id, SUM(p.amountPaid), MAX(p.paymentDate)
           FROM Payment p
           WHERE p.status IN ('SUCCESS', 'PARTIAL')
           GROUP BY p.customer.id
           """)
    List<Object[]> getCustomerPayments();

    @Query("""
           SELECT p
           FROM Payment p
           WHERE p.customer.id = :customerId
             AND p.paymentDate BETWEEN :from AND :to
             AND p.status IN ('SUCCESS', 'PARTIAL')
           ORDER BY p.paymentDate
           """)
    List<Payment> findPaymentsByCustomerAndDateRange(
            @Param("customerId") UUID customerId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    @Query("""
           SELECT p.paymentDate, SUM(p.amountPaid)
           FROM Payment p
           WHERE p.paymentDate BETWEEN :from AND :to
             AND p.status IN ('SUCCESS', 'PARTIAL')
           GROUP BY p.paymentDate
           ORDER BY p.paymentDate
           """)
    List<Object[]> getDailyCashInflow(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    @Query("""
           SELECT COALESCE(SUM(p.amountPaid), 0)
           FROM Payment p
           WHERE p.paymentDate BETWEEN :from AND :to
             AND p.status IN ('SUCCESS', 'PARTIAL')
           """)
    BigDecimal getTotalCollections(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    @Query("""
           SELECT FUNCTION('DATE_TRUNC', 'month', p.paymentDate), SUM(p.amountPaid)
           FROM Payment p
           WHERE p.paymentDate BETWEEN :from AND :to
             AND p.status IN ('SUCCESS', 'PARTIAL')
           GROUP BY FUNCTION('DATE_TRUNC', 'month', p.paymentDate)
           ORDER BY FUNCTION('DATE_TRUNC', 'month', p.paymentDate)
           """)
    List<Object[]> getMonthlyCollections(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    /**
     * Returns total payment collections within the period.
     */
    @Query("""
        SELECT COALESCE(SUM(p.amountPaid),0)
        FROM Payment p
        WHERE p.paymentDate BETWEEN :from AND :to
        """)
    BigDecimal getCollections(LocalDate from, LocalDate to);

    /**
     * Total amount in PENDING status (cheques not yet cleared)
     */
    @Query("""
       SELECT COALESCE(SUM(p.amountPaid), 0)
       FROM Payment p
       WHERE p.invoice.id = :invoiceId
         AND p.status = 'PENDING'
       """)
    BigDecimal getTotalPending(@Param("invoiceId") UUID invoiceId);





}