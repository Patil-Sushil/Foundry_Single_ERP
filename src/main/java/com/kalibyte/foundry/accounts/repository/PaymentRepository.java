package com.kalibyte.foundry.accounts.repository;

import com.kalibyte.foundry.accounts.entity.Payment;
import com.kalibyte.foundry.billing.invoice.entity.Invoice;
import com.kalibyte.foundry.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findByInvoice(Invoice invoice);

    List<Payment> findByCustomer(Customer customer);

    @Query("""
           SELECT COALESCE(SUM(p.amountPaid),0)
           FROM Payment p
           WHERE p.invoice.id = :invoiceId
           """)
    BigDecimal getTotalPaid(UUID invoiceId);

    @Query("""
        SELECT p.paymentNumber
        FROM Payment p
        ORDER BY p.createdAt DESC
        LIMIT 1
       """)
    Optional<String> findLastPaymentNumber();


    // Query for Daily Collection Report - returns List of Object arrays: [paymentDate, totalAmount, paymentCount]
    @Query(value = """
SELECT
    p.payment_date,

    SUM(p.amount_paid) AS total_amount,

    COUNT(p.id) AS transaction_count,

    SUM(CASE WHEN p.payment_method = 'CASH' THEN p.amount_paid ELSE 0 END) AS cash_amount,

    SUM(CASE WHEN p.payment_method = 'UPI' THEN p.amount_paid ELSE 0 END) AS upi_amount,

    SUM(CASE WHEN p.payment_method = 'BANK_TRANSFER' THEN p.amount_paid ELSE 0 END) AS bank_transfer_amount,

    SUM(CASE WHEN p.payment_method = 'CHEQUE' THEN p.amount_paid ELSE 0 END) AS cheque_amount,

    SUM(CASE WHEN p.payment_method = 'CARD' THEN p.amount_paid ELSE 0 END) AS card_amount

FROM payments p

WHERE p.payment_date BETWEEN :from AND :to
AND p.status = 'SUCCESS'

GROUP BY p.payment_date
ORDER BY p.payment_date
""", nativeQuery = true)
    List<Object[]> getDailyCollection(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

}