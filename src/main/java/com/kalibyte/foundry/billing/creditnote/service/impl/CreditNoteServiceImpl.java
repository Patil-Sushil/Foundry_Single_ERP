package com.kalibyte.foundry.billing.creditnote.service.impl;

import com.kalibyte.foundry.billing.invoice.entity.Invoice;
import com.kalibyte.foundry.billing.invoice.repository.InvoiceRepository;
import com.kalibyte.foundry.billing.creditnote.dto.response.CreditNoteResponse;
import com.kalibyte.foundry.billing.creditnote.entity.CreditNote;
import com.kalibyte.foundry.billing.creditnote.entity.enums.CreditNoteStatus;
import com.kalibyte.foundry.billing.creditnote.mapper.CreditNoteMapper;
import com.kalibyte.foundry.billing.creditnote.repository.CreditNoteRepository;
import com.kalibyte.foundry.billing.creditnote.service.CreditNoteService;
import com.kalibyte.foundry.billing.util.GstCalculationResult;
import com.kalibyte.foundry.common.exception.ResourceNotFoundException;
import com.kalibyte.foundry.qa.customerreturn.entity.CustomerReturn;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreditNoteServiceImpl implements CreditNoteService {

    private final CreditNoteRepository creditNoteRepository;
    private final InvoiceRepository invoiceRepository;
    private final CreditNoteMapper creditNoteMapper;
    private final CreditNoteNumberGenerator numberGenerator;

    @Override
    @Transactional
    public CreditNoteResponse generateCreditNoteFromReturn(CustomerReturn customerReturn, BigDecimal creditAmount) {
        BigDecimal subtotal = creditAmount != null ? creditAmount : BigDecimal.ZERO;
        
        BigDecimal gstPercentage = customerReturn.getOrder().getGstPercentage();
        if (gstPercentage == null) {
            gstPercentage = BigDecimal.valueOf(18);
        }

        GstCalculationResult gstResult = GstCalculationResult.calculate(
                subtotal, gstPercentage, customerReturn.getCustomer().getState());

        Invoice originalInvoice = invoiceRepository.findByOrder(customerReturn.getOrder())
                .orElse(null);

        CreditNote creditNote = CreditNote.builder()
                .creditNoteNumber(numberGenerator.generateCreditNoteNumber())
                .customer(customerReturn.getCustomer())
                .order(customerReturn.getOrder())
                .invoiceId(originalInvoice != null ? originalInvoice.getId() : null)
                .originalInvoiceNumber(originalInvoice != null ? originalInvoice.getInvoiceNumber() : null)
                .customerReturn(customerReturn)
                .issueDate(LocalDate.now())
                .reason("Customer Return: " + customerReturn.getReturnNumber())
                .subtotal(subtotal)
                .gstType(gstResult.getGstType())
                .gstPercentage(gstResult.getGstPercentage())
                .cgst(gstResult.getCgst())
                .sgst(gstResult.getSgst())
                .igst(gstResult.getIgst())
                .totalGst(gstResult.getTotalGst())
                .totalAmount(gstResult.getGrandTotal())
                .status(CreditNoteStatus.ISSUED)
                .build();

        CreditNote saved = creditNoteRepository.save(creditNote);
        return creditNoteMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CreditNoteResponse getById(UUID id) {
        return creditNoteRepository.findById(id)
                .map(creditNoteMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Credit Note not found: " + id));
    }
}
