package com.kalibyte.foundry.quotation.service;

import com.kalibyte.foundry.common.email.EmailService;
import com.kalibyte.foundry.quotation.entity.Quotation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QuotationEmailService {

    private final EmailService emailService;
    private final QuotationPdfService quotationPdfService;

    public void sendQuotationEmail(Quotation quotation) {

        try {

            byte[] pdfBytes = quotationPdfService.generatePdf(quotation);

            Map<String, Object> variables = new HashMap<>();
            variables.put("customerName", quotation.getCustomer().getName());
            variables.put("quotationNumber", quotation.getQuotationNumber());
            variables.put("quotationDate", quotation.getQuotationDate().toString());
            variables.put("totalAmount", "₹ " + quotation.getTotalAmount()); // Should be formatted better if needed

            emailService.sendTemplatedEmailWithAttachment(
                    quotation.getCustomer().getEmail(),
                    "Quotation " + quotation.getQuotationNumber(),
                    "quotation",
                    variables,
                    pdfBytes,
                    "Quotation_" + quotation.getQuotationNumber() + ".pdf"
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to send quotation email", e);
        }
    }
}