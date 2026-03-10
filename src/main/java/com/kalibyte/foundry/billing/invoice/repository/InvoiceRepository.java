package com.kalibyte.foundry.billing.invoice.repository;

import com.kalibyte.foundry.billing.invoice.entity.Invoice;
import com.kalibyte.foundry.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    Optional<Invoice> findTopByInvoiceNumberStartingWithOrderByInvoiceNumberDesc(String prefix);

    Optional<Invoice> findByOrder(Order order);
}