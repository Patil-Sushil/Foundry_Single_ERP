package com.kalibyte.foundry.billing.invoice.service.impl;

import com.kalibyte.foundry.billing.invoice.entity.Invoice;
import com.kalibyte.foundry.billing.invoice.entity.enums.InvoiceStatus;
import com.kalibyte.foundry.billing.invoice.repository.InvoiceRepository;
import com.kalibyte.foundry.billing.invoice.service.InvoicePaymentService;
import com.kalibyte.foundry.common.exception.ResourceNotFoundException;
import com.kalibyte.foundry.payment.entity.Enums.PaymentMethod;
import com.kalibyte.foundry.payment.entity.Enums.PaymentStatus;
import com.kalibyte.foundry.payment.entity.Payment;
import com.kalibyte.foundry.payment.repository.PaymentRepository;
import com.kalibyte.foundry.payment.util.PaymentNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoicePaymentServiceImpl implements InvoicePaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentNumberGenerator paymentNumberGenerator;

    @Override
    @Transactional
    public void processAutomaticPayment(Invoice invoice, BigDecimal amountPaid) {
        if (amountPaid == null || amountPaid.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        log.info("Processing automatic payment of ₹{} for invoice {}", amountPaid, invoice.getInvoiceNumber());

        PaymentStatus status = amountPaid.compareTo(invoice.getTotalAmount()) >= 0
                ? PaymentStatus.SUCCESS : PaymentStatus.PARTIAL;

        Payment payment = Payment.builder()
                .paymentNumber(paymentNumberGenerator.generate())
                .invoice(invoice)
                .customer(invoice.getCustomer())
                .paymentDate(LocalDate.now())
                .paymentMethod(PaymentMethod.CASH) // Defaulting to CASH for automatic payments
                .amountPaid(amountPaid)
                .status(status)
                .remarks("Automatic payment created during invoice generation")
                .build();

        paymentRepository.save(payment);
        
        // Update invoice status after creating payment
        updateInvoiceStatus(invoice.getId());
    }

    @Override
    @Transactional
    public void updateInvoiceStatus(UUID invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with ID: " + invoiceId));

        BigDecimal totalPaid = calculateTotalPaid(invoiceId);
        BigDecimal totalPending = paymentRepository.getTotalPending(invoiceId);
        
        InvoiceStatus newStatus;
        if (totalPaid.compareTo(invoice.getTotalAmount()) >= 0) {
            newStatus = InvoiceStatus.PAID;
        } else if (totalPaid.compareTo(BigDecimal.ZERO) > 0 || totalPending.compareTo(BigDecimal.ZERO) > 0) {
            newStatus = InvoiceStatus.PARTIALLY_PAID;
        } else {
            newStatus = InvoiceStatus.UNPAID;
        }

        if (invoice.getBillStatus() != newStatus) {
            log.info("Updating invoice {} status from {} to {}", invoice.getInvoiceNumber(), invoice.getBillStatus(), newStatus);
            invoice.setBillStatus(newStatus);
            invoiceRepository.save(invoice);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalPaid(UUID invoiceId) {
        return paymentRepository.getTotalPaid(invoiceId);
    }
}
