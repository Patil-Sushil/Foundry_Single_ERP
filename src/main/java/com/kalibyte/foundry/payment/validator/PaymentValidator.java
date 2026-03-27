package com.kalibyte.foundry.payment.validator;

import com.kalibyte.foundry.common.exception.BusinessException;
import com.kalibyte.foundry.payment.dto.request.PaymentCreateRequest;
import com.kalibyte.foundry.payment.entity.Enums.PaymentMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

@Component
public class PaymentValidator {

    /**
     * Validates method-specific fields based on the selected payment method.
     */
    public void validateMethodSpecificFields(PaymentCreateRequest request) {

        PaymentMethod method = request.getPaymentMethod();

        // ── Transaction ID required methods ──
        if (method.isTransactionIdRequired()) {
            if (!StringUtils.hasText(request.getTransactionId())) {
                throw new BusinessException(
                        "Transaction ID is required for " + method.getDisplayName() + " payments"
                );
            }
        }

        // ── Instrument (Cheque/DD) required methods ──
        if (method.isInstrumentNumberRequired()) {
            if (!StringUtils.hasText(request.getInstrumentNumber())) {
                throw new BusinessException(
                        "Instrument number (Cheque No / DD No) is required for " + method.getDisplayName() + " payments"
                );
            }
        }

        if (method.isInstrumentDateRequired()) {
            if (request.getInstrumentDate() == null) {
                throw new BusinessException(
                        "Instrument date is required for " + method.getDisplayName() + " payments"
                );
            }
        }

        // ── Bank name required for cheque/DD ──
        if (method == PaymentMethod.CHEQUE || method == PaymentMethod.DEMAND_DRAFT) {
            if (!StringUtils.hasText(request.getBankName())) {
                throw new BusinessException(
                        "Bank name is required for " + method.getDisplayName() + " payments"
                );
            }
        }

        // ── Cheque number format validation ──
        if (method == PaymentMethod.CHEQUE && StringUtils.hasText(request.getInstrumentNumber())) {
            if (!request.getInstrumentNumber().matches("^\\d{6}$")) {
                throw new BusinessException("Cheque number must be exactly 6 digits");
            }
        }

        // ── UPI Transaction ID format (basic) ──
        if (method == PaymentMethod.UPI && StringUtils.hasText(request.getTransactionId())) {
            if (request.getTransactionId().length() < 8) {
                throw new BusinessException("UPI Transaction ID appears invalid (too short)");
            }
        }

        // ── Instrument date should not be too old ──
        if (request.getInstrumentDate() != null) {
            if (request.getInstrumentDate().isBefore(LocalDate.now().minusMonths(3))) {
                throw new BusinessException("Instrument date is older than 3 months — likely stale");
            }
            if (request.getInstrumentDate().isAfter(LocalDate.now().plusMonths(3))) {
                throw new BusinessException("Instrument date cannot be more than 3 months in the future");
            }
        }

        // ── Payment date validation ──
        if (request.getPaymentDate() != null) {
            if (request.getPaymentDate().isAfter(LocalDate.now())) {
                throw new BusinessException("Payment date cannot be in the future");
            }
            if (request.getPaymentDate().isBefore(LocalDate.now().minusYears(1))) {
                throw new BusinessException("Payment date cannot be more than 1 year in the past");
            }
        }
    }

    /**
     * Strips method-irrelevant fields to keep data clean.
     * E.g., if CASH is selected, clear out transactionId, instrumentNumber, etc.
     */
    public void sanitize(PaymentCreateRequest request) {

        PaymentMethod method = request.getPaymentMethod();

        if (!method.isTransactionIdRequired()) {
            request.setTransactionId(null);
        }

        if (!method.isInstrumentNumberRequired()) {
            request.setInstrumentNumber(null);
            request.setBankName(null);
            request.setBranchName(null);
        }

        if (!method.isInstrumentDateRequired()) {
            request.setInstrumentDate(null);
        }
    }
}