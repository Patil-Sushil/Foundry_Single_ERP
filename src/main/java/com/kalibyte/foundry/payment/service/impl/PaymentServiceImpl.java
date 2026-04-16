package com.kalibyte.foundry.payment.service.impl;

import com.kalibyte.foundry.billing.invoice.entity.Invoice;
import com.kalibyte.foundry.billing.invoice.entity.enums.InvoiceStatus;
import com.kalibyte.foundry.billing.invoice.repository.InvoiceRepository;
import com.kalibyte.foundry.billing.invoice.service.InvoicePaymentService;
import com.kalibyte.foundry.common.email.EmailService;
import com.kalibyte.foundry.common.exception.BusinessException;
import com.kalibyte.foundry.common.exception.ResourceNotFoundException;
import com.kalibyte.foundry.customer.entity.Customer;
import com.kalibyte.foundry.payment.dto.request.PaymentCancelRequest;
import com.kalibyte.foundry.payment.dto.request.PaymentCreateRequest;
import com.kalibyte.foundry.payment.dto.request.PaymentFilterRequest;
import com.kalibyte.foundry.payment.dto.response.PaymentResponse;
import com.kalibyte.foundry.payment.dto.response.PaymentSummaryResponse;
import com.kalibyte.foundry.payment.email.EmailEventType;
import com.kalibyte.foundry.payment.email.PaymentEmailContext;
import com.kalibyte.foundry.payment.email.PaymentEmailContextFactory;
import com.kalibyte.foundry.payment.email.PaymentEmailTemplateBuilder;
import com.kalibyte.foundry.payment.entity.Enums.PaymentMethod;
import com.kalibyte.foundry.payment.entity.Enums.PaymentStatus;
import com.kalibyte.foundry.payment.entity.Payment;
import com.kalibyte.foundry.payment.mapper.PaymentMapper;
import com.kalibyte.foundry.payment.repository.PaymentRepository;
import com.kalibyte.foundry.payment.service.PaymentService;
import com.kalibyte.foundry.payment.specification.PaymentSpecification;
import com.kalibyte.foundry.payment.util.PaymentNumberGenerator;
import com.kalibyte.foundry.payment.validator.PaymentValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentNumberGenerator paymentNumberGenerator;
    private final PaymentValidator paymentValidator;
    private final EmailService emailService;
    private final PaymentMapper paymentMapper;
    private final InvoicePaymentService invoicePaymentService;

    // ══════════════════════════════════════════════════
    //  CREATE PAYMENT
    // ══════════════════════════════════════════════════
    @Override
    @Transactional
    public PaymentResponse createPayment(PaymentCreateRequest request) {

        paymentValidator.validateMethodSpecificFields(request);
        paymentValidator.sanitize(request);

        Invoice invoice = invoiceRepository.findById(request.getInvoiceId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Invoice not found with ID: " + request.getInvoiceId()));

        if (invoice.getBillStatus() == InvoiceStatus.PAID) {
            throw new BusinessException("Invoice is already fully paid");
        }
        if (invoice.getBillStatus() == InvoiceStatus.CANCELLED) {
            throw new BusinessException(
                    "Cannot record payment against a cancelled invoice");
        }

        BigDecimal alreadyPaid = paymentRepository.getTotalPaid(invoice.getId());
        BigDecimal pendingAmount = paymentRepository.getTotalPending(invoice.getId());
        BigDecimal committed = alreadyPaid.add(pendingAmount);
        BigDecimal remaining = invoice.getTotalAmount().subtract(committed);

        if (request.getAmountPaid().compareTo(remaining) > 0) {
            throw new BusinessException(String.format(
                    "Payment of ₹%s exceeds available balance of ₹%s "
                            + "(Paid: ₹%s, Pending clearance: ₹%s)",
                    request.getAmountPaid(), remaining,
                    alreadyPaid, pendingAmount));
        }

        checkDuplicate(request);

        PaymentStatus initialStatus = resolveInitialStatus(request.getPaymentMethod());
        if (initialStatus == PaymentStatus.SUCCESS && request.getAmountPaid().compareTo(invoice.getTotalAmount()) < 0) {
            initialStatus = PaymentStatus.PARTIAL;
        }
        Customer customer = invoice.getCustomer();

        Payment payment = Payment.builder()
                .paymentNumber(paymentNumberGenerator.generate())
                .invoice(invoice)
                .customer(customer)
                .paymentDate(request.getPaymentDate() != null
                        ? request.getPaymentDate() : LocalDate.now())
                .paymentMethod(request.getPaymentMethod())
                .amountPaid(request.getAmountPaid())
                .status(initialStatus)
                .transactionId(request.getTransactionId())
                .instrumentNumber(request.getInstrumentNumber())
                .instrumentDate(request.getInstrumentDate())
                .bankName(request.getBankName())
                .branchName(request.getBranchName())
                .referenceNumber(request.getReferenceNumber())
                .remarks(request.getRemarks())
                .receiptUrl(request.getReceiptUrl())
                .receivedBy(request.getReceivedBy())
                .build();

        paymentRepository.save(payment);

        log.info("Payment {} created | Invoice: {} | Method: {} | Amount: ₹{} | Status: {}",
                payment.getPaymentNumber(), invoice.getInvoiceNumber(),
                payment.getPaymentMethod().getDisplayName(),
                payment.getAmountPaid(),
                payment.getStatus().getDisplayName());

        invoicePaymentService.updateInvoiceStatus(invoice.getId());
        sendPaymentEmail(payment, EmailEventType.PAYMENT_CREATED);

        return paymentMapper.toResponse(payment);
    }

    // ══════════════════════════════════════════════════
    //  CONFIRM CHEQUE CLEARED
    // ══════════════════════════════════════════════════
    @Override
    @Transactional
    public PaymentResponse confirmChequeCleared(UUID paymentId) {

        Payment payment = findPaymentOrThrow(paymentId);

        if (payment.getPaymentMethod() != PaymentMethod.CHEQUE
                && payment.getPaymentMethod() != PaymentMethod.DEMAND_DRAFT) {
            throw new BusinessException(
                    "Only Cheque/DD payments need clearance confirmation");
        }
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BusinessException(
                    "Only PENDING payments can be confirmed. Current status: "
                            + payment.getStatus().getDisplayName());
        }

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setRemarks(appendRemark(payment.getRemarks(),
                "Cheque cleared on " + LocalDate.now()));
        paymentRepository.save(payment);

        log.info("Payment {} — Cheque {} CLEARED ✅",
                payment.getPaymentNumber(), payment.getInstrumentNumber());

        invoicePaymentService.updateInvoiceStatus(payment.getInvoice().getId());
        sendPaymentEmail(payment, EmailEventType.CHEQUE_CLEARED);

        return paymentMapper.toResponse(payment);
    }

    // ══════════════════════════════════════════════════
    //  MARK CHEQUE AS BOUNCED
    // ══════════════════════════════════════════════════
    @Override
    @Transactional
    public PaymentResponse markAsBounced(UUID paymentId, String reason) {

        Payment payment = findPaymentOrThrow(paymentId);

        if (payment.getPaymentMethod() != PaymentMethod.CHEQUE
                && payment.getPaymentMethod() != PaymentMethod.DEMAND_DRAFT) {
            throw new BusinessException(
                    "Only Cheque/DD payments can be marked as bounced");
        }
        if (payment.getStatus() != PaymentStatus.PENDING
                && payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new BusinessException(
                    "Only active payments can be marked as bounced. Current status: "
                            + payment.getStatus().getDisplayName());
        }

        payment.setStatus(PaymentStatus.BOUNCED);
        payment.setCancellationReason(reason);
        payment.setRemarks(appendRemark(payment.getRemarks(),
                "BOUNCED on " + LocalDate.now() + ". Reason: " + reason));
        paymentRepository.save(payment);

        log.warn("Payment {} — Cheque {} BOUNCED ❌ | Reason: {}",
                payment.getPaymentNumber(),
                payment.getInstrumentNumber(), reason);

        invoicePaymentService.updateInvoiceStatus(payment.getInvoice().getId());
        sendPaymentEmail(payment, EmailEventType.CHEQUE_BOUNCED);

        return paymentMapper.toResponse(payment);
    }

    // ══════════════════════════════════════════════════
    //  CANCEL PAYMENT
    // ══════════════════════════════════════════════════
    @Override
    @Transactional
    public PaymentResponse cancelPayment(
            UUID paymentId, PaymentCancelRequest request) {

        Payment payment = findPaymentOrThrow(paymentId);

        if (payment.getStatus() == PaymentStatus.CANCELLED) {
            throw new BusinessException("Payment is already cancelled");
        }
        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            throw new BusinessException("Cannot cancel a refunded payment");
        }

        payment.setStatus(PaymentStatus.CANCELLED);
        payment.setCancellationReason(request.getReason());
        payment.setRemarks(appendRemark(payment.getRemarks(),
                "CANCELLED on " + LocalDate.now()
                        + ". Reason: " + request.getReason()));
        paymentRepository.save(payment);

        log.info("Payment {} CANCELLED | Reason: {}",
                payment.getPaymentNumber(), request.getReason());

        invoicePaymentService.updateInvoiceStatus(payment.getInvoice().getId());
        sendPaymentEmail(payment, EmailEventType.PAYMENT_CANCELLED);

        return paymentMapper.toResponse(payment);
    }

    // ══════════════════════════════════════════════════
    //  GET / SEARCH
    // ══════════════════════════════════════════════════
    @Override
    public PaymentResponse getPayment(UUID id) {
        return paymentMapper.toResponse(findPaymentOrThrow(id));
    }

    @Override
    public List<PaymentResponse> getPaymentsByInvoice(UUID invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Invoice not found with ID: " + invoiceId));
        return paymentMapper.toResponseList(
                paymentRepository.findByInvoiceOrderByPaymentDateDesc(invoice));
    }

    @Override
    public Page<PaymentResponse> searchPayments(PaymentFilterRequest filter) {
        Sort sort = "asc".equalsIgnoreCase(filter.getSortDir())
                ? Sort.by(filter.getSortBy()).ascending()
                : Sort.by(filter.getSortBy()).descending();
        Pageable pageable = PageRequest.of(
                filter.getPage(), filter.getSize(), sort);
        return paymentRepository
                .findAll(PaymentSpecification.withFilters(filter), pageable)
                .map(paymentMapper::toResponse);
    }

    @Override
    public PaymentSummaryResponse getInvoicePaymentSummary(UUID invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Invoice not found with ID: " + invoiceId));
        BigDecimal totalPaid = paymentRepository.getTotalPaid(invoiceId);
        BigDecimal totalPending = paymentRepository.getTotalPending(invoiceId);
        int txnCount = paymentRepository.getTransactionCount(invoiceId);
        return PaymentSummaryResponse.builder()
                .invoiceAmount(invoice.getTotalAmount())
                .totalPaid(totalPaid)
                .totalPending(totalPending)
                .remainingAmount(invoice.getTotalAmount()
                        .subtract(totalPaid).subtract(totalPending))
                .totalTransactions(txnCount)
                .invoiceStatus(invoice.getBillStatus().name())
                .build();
    }

    // ══════════════════════════════════════════════════
    //  UNIFIED EMAIL SENDER
    // ══════════════════════════════════════════════════

    private void sendPaymentEmail(Payment payment, EmailEventType eventType) {
        try {
            BigDecimal totalPaid = paymentRepository
                    .getTotalPaid(payment.getInvoice().getId());
            BigDecimal totalPending = paymentRepository
                    .getTotalPending(payment.getInvoice().getId());

            PaymentEmailContext ctx = PaymentEmailContextFactory.create(
                    payment, eventType, totalPaid, totalPending);

            String htmlBody = PaymentEmailTemplateBuilder.build(ctx);

            emailService.sendHtmlEmail(
                    ctx.getCustomerEmail(),
                    ctx.getSubject(),
                    htmlBody);

            log.info("📧 {} email sent to {} for {}",
                    eventType, ctx.getCustomerEmail(),
                    payment.getPaymentNumber());

        } catch (Exception e) {
            log.error("📧 Failed to send {} email for {}: {}",
                    eventType, payment.getPaymentNumber(),
                    e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════
    //  PRIVATE HELPERS
    // ══════════════════════════════════════════════════

    private PaymentStatus resolveInitialStatus(PaymentMethod method) {
        return switch (method) {
            case CHEQUE, DEMAND_DRAFT -> PaymentStatus.PENDING;
            default -> PaymentStatus.SUCCESS;
        };
    }

    private void checkDuplicate(PaymentCreateRequest request) {
        if (request.getTransactionId() != null
                && paymentRepository.existsByTransactionIdAndStatusNot(
                request.getTransactionId(), PaymentStatus.CANCELLED)) {
            throw new BusinessException(
                    "Duplicate transaction ID: " + request.getTransactionId());
        }
        if (request.getInstrumentNumber() != null
                && paymentRepository.existsByInstrumentNumberAndStatusNot(
                request.getInstrumentNumber(), PaymentStatus.CANCELLED)) {
            throw new BusinessException(
                    "Duplicate instrument number: "
                            + request.getInstrumentNumber());
        }
    }

    private Payment findPaymentOrThrow(UUID id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found with ID: " + id));
    }

    private String appendRemark(String existing, String newRemark) {
        if (existing == null || existing.isBlank()) return newRemark;
        return existing + " | " + newRemark;
    }
}