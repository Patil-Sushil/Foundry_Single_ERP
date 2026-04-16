package com.kalibyte.foundry.billing.invoice.service;

import com.kalibyte.foundry.billing.invoice.entity.Invoice;

import java.math.BigDecimal;
import java.util.UUID;

public interface InvoicePaymentService {

    /**
     * Automatically creates a payment for an invoice if amountPaid > 0.
     * Use default values for payment method (e.g. CASH or BANK_TRANSFER).
     */
    void processAutomaticPayment(Invoice invoice, BigDecimal amountPaid);

    /**
     * Recalculates and updates the invoice status based on all associated payments.
     */
    void updateInvoiceStatus(UUID invoiceId);

    /**
     * Calculates the total paid amount for an invoice.
     */
    BigDecimal calculateTotalPaid(UUID invoiceId);
}
