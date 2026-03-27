package com.kalibyte.foundry.payment.service.impl;

import com.kalibyte.foundry.billing.invoice.entity.Invoice;
import com.kalibyte.foundry.billing.invoice.entity.enums.InvoiceStatus;
import com.kalibyte.foundry.billing.invoice.repository.InvoiceRepository;
import com.kalibyte.foundry.common.email.EmailService;
import com.kalibyte.foundry.common.exception.BusinessException;
import com.kalibyte.foundry.common.exception.ResourceNotFoundException;
import com.kalibyte.foundry.customer.entity.Customer;
import com.kalibyte.foundry.payment.dto.request.PaymentCancelRequest;
import com.kalibyte.foundry.payment.dto.request.PaymentCreateRequest;
import com.kalibyte.foundry.payment.dto.request.PaymentFilterRequest;
import com.kalibyte.foundry.payment.dto.response.PaymentResponse;
import com.kalibyte.foundry.payment.dto.response.PaymentSummaryResponse;
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

    // ══════════════════════════════════════════════════
    //  CREATE PAYMENT
    // ══════════════════════════════════════════════════
    @Override
    @Transactional
    public PaymentResponse createPayment(PaymentCreateRequest request) {

        paymentValidator.validateMethodSpecificFields(request);
        paymentValidator.sanitize(request);

        Invoice invoice = invoiceRepository.findById(request.getInvoiceId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Invoice not found with ID: " + request.getInvoiceId()));

        if (invoice.getBillStatus() == InvoiceStatus.PAID) {
            throw new BusinessException("Invoice is already fully paid");
        }
        if (invoice.getBillStatus() == InvoiceStatus.CANCELLED) {
            throw new BusinessException(
                    "Cannot record payment against a cancelled invoice");
        }

        BigDecimal alreadyPaid =
                paymentRepository.getTotalPaid(invoice.getId());
        BigDecimal pendingAmount =
                paymentRepository.getTotalPending(invoice.getId());
        BigDecimal committed = alreadyPaid.add(pendingAmount);
        BigDecimal remaining = invoice.getTotalAmount().subtract(committed);

        if (request.getAmountPaid().compareTo(remaining) > 0) {
            throw new BusinessException(
                    String.format(
                            "Payment of ₹%s exceeds available balance of ₹%s "
                                    + "(Paid: ₹%s, Pending clearance: ₹%s)",
                            request.getAmountPaid(), remaining,
                            alreadyPaid, pendingAmount));
        }

        checkDuplicate(request);

        PaymentStatus initialStatus =
                resolveInitialStatus(request.getPaymentMethod());

        Customer customer = invoice.getCustomer();

        Payment payment = Payment.builder()
                .paymentNumber(paymentNumberGenerator.generate())
                .invoice(invoice)
                .customer(customer)
                .paymentDate(request.getPaymentDate() != null
                        ? request.getPaymentDate()
                        : LocalDate.now())
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
                payment.getPaymentNumber(),
                invoice.getInvoiceNumber(),
                payment.getPaymentMethod().getDisplayName(),
                payment.getAmountPaid(),
                payment.getStatus().getDisplayName());

        recalculateInvoiceStatus(invoice);

        // ── SEND EMAIL: Payment Created ──
        sendPaymentCreatedEmail(payment);

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
                payment.getPaymentNumber(),
                payment.getInstrumentNumber());

        recalculateInvoiceStatus(payment.getInvoice());

        // ── SEND EMAIL: Cheque Cleared ──
        sendChequeClearedEmail(payment);

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
                payment.getInstrumentNumber(),
                reason);

        recalculateInvoiceStatus(payment.getInvoice());

        // ── SEND EMAIL: Cheque Bounced ──
        sendChequeBounceEmail(payment);

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
            throw new BusinessException(
                    "Cannot cancel a refunded payment");
        }

        payment.setStatus(PaymentStatus.CANCELLED);
        payment.setCancellationReason(request.getReason());
        payment.setRemarks(appendRemark(payment.getRemarks(),
                "CANCELLED on " + LocalDate.now()
                        + ". Reason: " + request.getReason()));
        paymentRepository.save(payment);

        log.info("Payment {} CANCELLED | Reason: {}",
                payment.getPaymentNumber(),
                request.getReason());

        recalculateInvoiceStatus(payment.getInvoice());

        // ── SEND EMAIL: Payment Cancelled ──
        sendPaymentCancelledEmail(payment);

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
                .orElseThrow(() ->
                        new ResourceNotFoundException(
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
                .orElseThrow(() ->
                        new ResourceNotFoundException(
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

    // ══════════════════════════════════════════════════════════════
    //
    //   📧 EMAIL NOTIFICATIONS — ALL EVENTS
    //
    // ══════════════════════════════════════════════════════════════

    // ── 1. PAYMENT CREATED ──
    private void sendPaymentCreatedEmail(Payment payment) {
        try {
            Customer customer = payment.getCustomer();
            Invoice invoice = payment.getInvoice();

            BigDecimal totalPaid =
                    paymentRepository.getTotalPaid(invoice.getId());
            BigDecimal totalPending =
                    paymentRepository.getTotalPending(invoice.getId());
            BigDecimal remaining = invoice.getTotalAmount()
                    .subtract(totalPaid).subtract(totalPending);

            String methodDetail = buildMethodDetail(payment);

            String pendingNote = payment.getStatus() == PaymentStatus.PENDING
                    ? """

                    ⏳ IMPORTANT: This payment is currently PENDING clearance.
                    The amount will be credited to your account once the
                    %s is processed by the bank.
                    """.formatted(payment.getPaymentMethod().getDisplayName())
                    : "";

            String body = """
                    Dear %s,

                    Thank you! We have received your payment.

                    ════════════════════════════════════════
                    PAYMENT RECEIPT
                    ════════════════════════════════════════

                    Payment Number  : %s
                    Payment Date    : %s
                    Payment Method  : %s
                    Amount Paid     : ₹%s
                    Status          : %s

                    %s
                    ════════════════════════════════════════
                    INVOICE DETAILS
                    ════════════════════════════════════════

                    Invoice Number  : %s
                    Invoice Amount  : ₹%s
                    Total Paid      : ₹%s
                    Pending Clear.  : ₹%s
                    Balance Due     : ₹%s
                    %s
                    ════════════════════════════════════════

                    If you have any questions, please contact
                    our accounts team.

                    Warm Regards,
                    Accounts Department
                    Kalibyte Foundry
                    """.formatted(
                    customer.getName(),
                    payment.getPaymentNumber(),
                    payment.getPaymentDate(),
                    payment.getPaymentMethod().getDisplayName(),
                    payment.getAmountPaid(),
                    payment.getStatus().getDisplayName(),
                    methodDetail,
                    invoice.getInvoiceNumber(),
                    invoice.getTotalAmount(),
                    totalPaid,
                    totalPending,
                    remaining,
                    pendingNote);

            emailService.sendEmail(
                    customer.getEmail(),
                    " Payment Received — " + payment.getPaymentNumber(),
                    body);

            log.info(" Payment created email sent to {} for {}",
                    customer.getEmail(), payment.getPaymentNumber());

        } catch (Exception e) {
            log.error(" Failed to send payment created email for {}: {}",
                    payment.getPaymentNumber(), e.getMessage());
        }
    }

    // ── 2. CHEQUE CLEARED ──
    private void sendChequeClearedEmail(Payment payment) {
        try {
            Customer customer = payment.getCustomer();
            Invoice invoice = payment.getInvoice();

            BigDecimal totalPaid =
                    paymentRepository.getTotalPaid(invoice.getId());
            BigDecimal remaining =
                    invoice.getTotalAmount().subtract(totalPaid);

            String instrumentType =
                    payment.getPaymentMethod() == PaymentMethod.CHEQUE
                            ? "Cheque" : "Demand Draft";

            String body = """
                    Dear %s,

                    Great news! Your %s has been successfully cleared.

                    ════════════════════════════════════════
                    ✅ %s CLEARANCE CONFIRMATION
                    ════════════════════════════════════════

                    Payment Number  : %s
                    %s Number      : %s
                    %s Date        : %s
                    Amount          : ₹%s
                    Bank            : %s
                    Branch          : %s
                    Cleared On      : %s

                    ════════════════════════════════════════
                    INVOICE STATUS
                    ════════════════════════════════════════

                    Invoice Number  : %s
                    Invoice Amount  : ₹%s
                    Total Paid      : ₹%s
                    Balance Due     : ₹%s
                    Invoice Status  : %s

                    ════════════════════════════════════════

                    The payment has been credited to your account.

                    Warm Regards,
                    Accounts Department
                    Kalibyte Foundry
                    """.formatted(
                    customer.getName(),
                    instrumentType,
                    instrumentType.toUpperCase(),
                    payment.getPaymentNumber(),
                    instrumentType, payment.getInstrumentNumber(),
                    instrumentType, payment.getInstrumentDate(),
                    payment.getAmountPaid(),
                    payment.getBankName(),
                    payment.getBranchName() != null
                            ? payment.getBranchName() : "N/A",
                    LocalDate.now(),
                    invoice.getInvoiceNumber(),
                    invoice.getTotalAmount(),
                    totalPaid,
                    remaining,
                    invoice.getBillStatus().name());

            emailService.sendEmail(
                    customer.getEmail(),
                    "✅ " + instrumentType + " Cleared — "
                            + payment.getPaymentNumber(),
                    body);

            log.info("📧 Cheque cleared email sent to {} for {}",
                    customer.getEmail(), payment.getPaymentNumber());

        } catch (Exception e) {
            log.error("📧 Failed to send cheque cleared email for {}: {}",
                    payment.getPaymentNumber(), e.getMessage());
        }
    }

    // ── 3. CHEQUE BOUNCED ──
    private void sendChequeBounceEmail(Payment payment) {
        try {
            Customer customer = payment.getCustomer();
            Invoice invoice = payment.getInvoice();

            BigDecimal totalPaid =
                    paymentRepository.getTotalPaid(invoice.getId());
            BigDecimal remaining =
                    invoice.getTotalAmount().subtract(totalPaid);

            String instrumentType =
                    payment.getPaymentMethod() == PaymentMethod.CHEQUE
                            ? "Cheque" : "Demand Draft";

            String body = """
                    Dear %s,

                    We regret to inform you that your %s has been
                    returned unpaid (bounced) by the bank.

                    ════════════════════════════════════════
                    ❌ %s BOUNCE NOTIFICATION
                    ════════════════════════════════════════

                    Payment Number  : %s
                    %s Number      : %s
                    %s Date        : %s
                    Amount          : ₹%s
                    Bank            : %s
                    Branch          : %s
                    Bounce Reason   : %s
                    Bounced On      : %s

                    ════════════════════════════════════════
                    IMPACT ON YOUR ACCOUNT
                    ════════════════════════════════════════

                    Invoice Number  : %s
                    Invoice Amount  : ₹%s
                    Total Paid      : ₹%s
                    Amount Now Due  : ₹%s

                    ════════════════════════════════════════
                    ⚠️ ACTION REQUIRED
                    ════════════════════════════════════════

                    The bounced amount of ₹%s has been reversed
                    from your account. Please arrange an
                    alternative payment at the earliest to
                    avoid any service disruption.

                    Accepted payment methods:
                    • UPI
                    • NEFT / RTGS / IMPS
                    • Cash
                    • New Cheque / Demand Draft

                    ════════════════════════════════════════

                    For any queries, please contact our
                    accounts team immediately.

                    Regards,
                    Accounts Department
                    Kalibyte Foundry
                    """.formatted(
                    customer.getName(),
                    instrumentType,
                    instrumentType.toUpperCase(),
                    payment.getPaymentNumber(),
                    instrumentType, payment.getInstrumentNumber(),
                    instrumentType, payment.getInstrumentDate(),
                    payment.getAmountPaid(),
                    payment.getBankName(),
                    payment.getBranchName() != null
                            ? payment.getBranchName() : "N/A",
                    payment.getCancellationReason(),
                    LocalDate.now(),
                    invoice.getInvoiceNumber(),
                    invoice.getTotalAmount(),
                    totalPaid,
                    remaining,
                    payment.getAmountPaid());

            emailService.sendEmail(
                    customer.getEmail(),
                    "❌ " + instrumentType + " Bounced — "
                            + payment.getPaymentNumber()
                            + " | Action Required",
                    body);

            log.warn("📧 Cheque bounce email sent to {} for {}",
                    customer.getEmail(), payment.getPaymentNumber());

        } catch (Exception e) {
            log.error("📧 Failed to send bounce email for {}: {}",
                    payment.getPaymentNumber(), e.getMessage());
        }
    }

    // ── 4. PAYMENT CANCELLED ──
    private void sendPaymentCancelledEmail(Payment payment) {
        try {
            Customer customer = payment.getCustomer();
            Invoice invoice = payment.getInvoice();

            BigDecimal totalPaid =
                    paymentRepository.getTotalPaid(invoice.getId());
            BigDecimal remaining =
                    invoice.getTotalAmount().subtract(totalPaid);

            String methodDetail = buildMethodDetail(payment);

            String body = """
                    Dear %s,

                    This is to inform you that the following payment
                    has been cancelled.

                    ════════════════════════════════════════
                    🚫 PAYMENT CANCELLATION NOTICE
                    ════════════════════════════════════════

                    Payment Number  : %s
                    Payment Date    : %s
                    Payment Method  : %s
                    Amount          : ₹%s
                    %s
                    Cancel Reason   : %s
                    Cancelled On    : %s

                    ════════════════════════════════════════
                    UPDATED ACCOUNT STATUS
                    ════════════════════════════════════════

                    Invoice Number  : %s
                    Invoice Amount  : ₹%s
                    Total Paid      : ₹%s
                    Balance Due     : ₹%s
                    Invoice Status  : %s

                    ════════════════════════════════════════

                    If this cancellation was not expected, or if
                    you need to make an alternative payment, please
                    contact our accounts team.

                    Regards,
                    Accounts Department
                    Kalibyte Foundry
                    """.formatted(
                    customer.getName(),
                    payment.getPaymentNumber(),
                    payment.getPaymentDate(),
                    payment.getPaymentMethod().getDisplayName(),
                    payment.getAmountPaid(),
                    methodDetail,
                    payment.getCancellationReason(),
                    LocalDate.now(),
                    invoice.getInvoiceNumber(),
                    invoice.getTotalAmount(),
                    totalPaid,
                    remaining,
                    invoice.getBillStatus().name());

            emailService.sendEmail(
                    customer.getEmail(),
                    "🚫 Payment Cancelled — "
                            + payment.getPaymentNumber(),
                    body);

            log.info("📧 Payment cancelled email sent to {} for {}",
                    customer.getEmail(), payment.getPaymentNumber());

        } catch (Exception e) {
            log.error("📧 Failed to send cancellation email for {}: {}",
                    payment.getPaymentNumber(), e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════
    //  PRIVATE HELPERS
    // ══════════════════════════════════════════════════

    private void recalculateInvoiceStatus(Invoice invoice) {

        BigDecimal totalPaid =
                paymentRepository.getTotalPaid(invoice.getId());
        BigDecimal totalPending =
                paymentRepository.getTotalPending(invoice.getId());

        InvoiceStatus newStatus;

        if (totalPaid.compareTo(invoice.getTotalAmount()) >= 0) {
            newStatus = InvoiceStatus.PAID;
        } else if (totalPaid.compareTo(BigDecimal.ZERO) > 0
                || totalPending.compareTo(BigDecimal.ZERO) > 0) {
            newStatus = InvoiceStatus.PARTIALLY_PAID;
        } else {
            newStatus = InvoiceStatus.UNPAID;
        }

        InvoiceStatus oldStatus = invoice.getBillStatus();

        if (oldStatus != newStatus) {
            invoice.setBillStatus(newStatus);
            invoiceRepository.save(invoice);
            log.info("Invoice {} status: {} → {}",
                    invoice.getInvoiceNumber(), oldStatus, newStatus);
        }
    }

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
                    "Duplicate transaction ID: "
                            + request.getTransactionId());
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
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Payment not found with ID: " + id));
    }

    private String appendRemark(String existing, String newRemark) {
        if (existing == null || existing.isBlank()) {
            return newRemark;
        }
        return existing + " | " + newRemark;
    }

    private String buildMethodDetail(Payment payment) {
        return switch (payment.getPaymentMethod()) {
            case UPI ->
                    "UPI Txn ID      : " + payment.getTransactionId();
            case NEFT ->
                    "NEFT Txn ID     : " + payment.getTransactionId();
            case RTGS ->
                    "RTGS Txn ID     : " + payment.getTransactionId();
            case IMPS ->
                    "IMPS Txn ID     : " + payment.getTransactionId();
            case CARD ->
                    "Card Txn ID     : " + payment.getTransactionId();
            case BANK_TRANSFER ->
                    "Transfer Txn ID : " + payment.getTransactionId();
            case CHEQUE ->
                    """
                    Cheque Number   : %s
                    Cheque Date     : %s
                    Bank            : %s
                    Branch          : %s"""
                            .formatted(
                                    payment.getInstrumentNumber(),
                                    payment.getInstrumentDate(),
                                    payment.getBankName(),
                                    nvl(payment.getBranchName()));
            case DEMAND_DRAFT ->
                    """
                    DD Number       : %s
                    DD Date         : %s
                    Bank            : %s
                    Branch          : %s"""
                            .formatted(
                                    payment.getInstrumentNumber(),
                                    payment.getInstrumentDate(),
                                    payment.getBankName(),
                                    nvl(payment.getBranchName()));
            case CASH ->
                    payment.getReceivedBy() != null
                            ? "Received By     : " + payment.getReceivedBy()
                            : "";
        };
    }

    private String nvl(String value) {
        return value != null ? value : "N/A";
    }
}