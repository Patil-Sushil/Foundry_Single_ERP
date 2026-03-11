package com.kalibyte.foundry.billing.repository;

import com.kalibyte.foundry.billing.entity.Invoice;
import com.kalibyte.foundry.billing.entity.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, UUID> {
    List<InvoiceItem> findByInvoice(Invoice invoice);
}