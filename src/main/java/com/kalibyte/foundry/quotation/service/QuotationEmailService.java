package com.kalibyte.foundry.quotation.service;

import com.kalibyte.foundry.quotation.entity.Quotation;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QuotationEmailService {

    private final JavaMailSender mailSender;
    private final QuotationPdfService quotationPdfService;

    public void sendQuotationEmail(Quotation quotation) {

        try {

            byte[] pdfBytes = quotationPdfService.generatePdf(quotation);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true);

            helper.setTo(quotation.getCustomer().getEmail());
            helper.setSubject("Quotation " + quotation.getQuotationNumber());

            helper.setText("""
                    Dear %s,

                    Please find attached quotation %s.

                    Regards,
                    Kali-Byte Precision Steel Foundry
                    """.formatted(
                    quotation.getCustomer().getName(),
                    quotation.getQuotationNumber()
            ));

            helper.addAttachment(
                    "Quotation_" + quotation.getQuotationNumber() + ".pdf",
                    new ByteArrayResource(pdfBytes)
            );

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Failed to send quotation email", e);
        }
    }
}