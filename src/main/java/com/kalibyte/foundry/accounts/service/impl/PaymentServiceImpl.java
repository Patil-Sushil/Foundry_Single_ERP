package com.kalibyte.foundry.accounts.service.impl;

import com.kalibyte.foundry.accounts.dto.request.PaymentCreateRequest;
import com.kalibyte.foundry.accounts.dto.response.PaymentResponse;
import com.kalibyte.foundry.accounts.entity.Enums.PaymentStatus;
import com.kalibyte.foundry.accounts.entity.Payment;
import com.kalibyte.foundry.accounts.mapper.PaymentMapper;
import com.kalibyte.foundry.accounts.repository.PaymentRepository;
import com.kalibyte.foundry.accounts.service.PaymentService;
import com.kalibyte.foundry.accounts.util.PaymentNumberGenerator;
import com.kalibyte.foundry.billing.Enums.InvoiceStatus;
import com.kalibyte.foundry.billing.invoice.entity.Invoice;
import com.kalibyte.foundry.billing.invoice.repository.InvoiceRepository;
import com.kalibyte.foundry.common.email.EmailService;
import com.kalibyte.foundry.common.exception.BusinessException;
import com.kalibyte.foundry.common.exception.ResourceNotFoundException;
import com.kalibyte.foundry.customer.entity.Customer;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentNumberGenerator paymentNumberGenerator;
    private final EmailService emailService;
    private final PaymentMapper paymentMapper;

    @Override
    @Transactional
    public PaymentResponse createPayment(PaymentCreateRequest request) {

        Invoice invoice = invoiceRepository.findById(request.getInvoiceId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Invoice not found"));

        //------------------------------------------------
        // TOTAL PAID
        //------------------------------------------------

        BigDecimal alreadyPaid =
                paymentRepository.getTotalPaid(invoice.getId());

        //------------------------------------------------
        // CHECK OVERPAYMENT
        //------------------------------------------------

        BigDecimal remaining =
                invoice.getTotalAmount().subtract(alreadyPaid);

        if(request.getAmountPaid().compareTo(remaining) > 0){

            throw new BusinessException(
                    "Payment exceeds invoice amount. Remaining amount is "
                            + remaining
            );
        }

        //------------------------------------------------
        // CREATE PAYMENT
        //------------------------------------------------

        Customer customer = invoice.getCustomer();

        Payment payment = Payment.builder()
                .paymentNumber(paymentNumberGenerator.generate())
                .invoice(invoice)
                .customer(customer)
                .paymentDate(LocalDate.now())
                .paymentMethod(request.getPaymentMethod())
                .amountPaid(request.getAmountPaid())
                .referenceNumber(request.getReferenceNumber())
                .remarks(request.getRemarks())
                .status(PaymentStatus.SUCCESS)
                .build();

        paymentRepository.save(payment);

        //------------------------------------------------
        // UPDATE INVOICE STATUS
        //------------------------------------------------

        BigDecimal totalPaid = paymentRepository.getTotalPaid(invoice.getId());

        if (totalPaid.compareTo(BigDecimal.ZERO) == 0) {
            invoice.setBillStatus(InvoiceStatus.UNPAID);
        }
        else if (totalPaid.compareTo(invoice.getTotalAmount()) < 0) {
            invoice.setBillStatus(InvoiceStatus.PARTIALLY_PAID);
        }
        else {
            invoice.setBillStatus(InvoiceStatus.PAID);
        }

        invoiceRepository.save(invoice);

        //------------------------------------------------
        // TOTAL AFTER PAYMENT
        //------------------------------------------------

        BigDecimal newTotal =
                alreadyPaid.add(request.getAmountPaid());

        BigDecimal newRemaining =
                invoice.getTotalAmount().subtract(newTotal);

        //------------------------------------------------
        // SEND EMAIL
        //------------------------------------------------

        sendPaymentEmail(
                customer.getEmail(),
                invoice.getInvoiceNumber(),
                invoice.getTotalAmount(),
                request.getAmountPaid(),
                newRemaining
        );

        return paymentMapper.toResponse(payment);
    }

    private void sendPaymentEmail(
            String email,
            String invoiceNumber,
            BigDecimal invoiceAmount,
            BigDecimal paid,
            BigDecimal remaining
    ){

        String subject = "Payment Received";

        String body = """
                Dear Customer,

                We received your payment.

                Invoice Number: %s
                Invoice Amount: ₹%s
                Paid Now: ₹%s
                Remaining Balance: ₹%s

                Thank you.
                """.formatted(
                invoiceNumber,
                invoiceAmount,
                paid,
                remaining
        );

        emailService.sendEmail(email, subject, body);
    }

    @Override
    public PaymentResponse getPayment(UUID id) {

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Payment not found"));

        return paymentMapper.toResponse(payment);
    }

    @Override
    public List<PaymentResponse> getPaymentsByInvoice(UUID invoiceId) {

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Invoice not found"));

        return paymentRepository.findByInvoice(invoice)
                .stream()
                .map(paymentMapper::toResponse)
                .toList();
    }

    @Override
    public List<PaymentResponse> getAllPayments() {

        return paymentRepository.findAll()
                .stream()
                .map(paymentMapper::toResponse)
                .toList();
    }
}