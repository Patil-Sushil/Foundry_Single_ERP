package com.kalibyte.foundry.quotation.service.impl;

import com.kalibyte.foundry.common.email.EmailService;
import com.kalibyte.foundry.quotation.entity.Quotation;
import com.kalibyte.foundry.quotation.service.QuotationEmailService;
import com.kalibyte.foundry.quotation.service.QuotationPdfService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class QuotationEmailServiceImpl implements QuotationEmailService {

    private final EmailService emailService;
    private final QuotationPdfService quotationPdfService;

    @Override
    public void sendQuotationEmail(Quotation quotation) {
        try {
            byte[] pdfBytes = quotationPdfService.generatePdf(quotation);

            Map<String, Object> variables = new HashMap<>();
            variables.put("customerName", quotation.getCustomer().getName());
            variables.put("quotationNumber", quotation.getQuotationNumber());
            variables.put("quotationDate", quotation.getQuotationDate().toString());
            variables.put("totalAmount", "₹ " + quotation.getTotalAmount());

            emailService.sendTemplatedEmailWithAttachment(
                    quotation.getCustomer().getEmail(),
                    "Quotation " + quotation.getQuotationNumber(),
                    "quotation",
                    variables,
                    pdfBytes,
                    "Quotation_" + quotation.getQuotationNumber() + ".pdf"
            );

        } catch (Exception e) {
            log.error("Failed to send quotation email for {}: {}", 
                quotation.getQuotationNumber(), e.getMessage());
        }
    }
}
