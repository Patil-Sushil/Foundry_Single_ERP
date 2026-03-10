package com.kalibyte.foundry.accounts.repository;

import com.kalibyte.foundry.accounts.entity.Payment;
import com.kalibyte.foundry.billing.invoice.entity.Invoice;
import com.kalibyte.foundry.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
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
            SELECT e.paymentNumber
            FROM Payment e
            ORDER BY e.createdAt DESC
           """)
    Optional<String> findLastPaymentNumber();
}