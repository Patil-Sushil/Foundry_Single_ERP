package com.kalibyte.foundry.billing.util;

import com.kalibyte.foundry.billing.invoice.entity.Invoice;
import com.kalibyte.foundry.billing.invoice.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Year;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class InvoiceNumberGenerator {

    private final InvoiceRepository invoiceRepository;

    public String generateInvoiceNumber() {

        int year = Year.now().getValue();
        String prefix = "INV-" + year + "-";

        Optional<Invoice> lastInvoice =
                invoiceRepository.findTopByInvoiceNumberStartingWithOrderByInvoiceNumberDesc(prefix);

        int nextNumber = 1;

        if (lastInvoice.isPresent()) {

            String lastInvoiceNumber = lastInvoice.get().getInvoiceNumber();

            String sequencePart = lastInvoiceNumber.substring(prefix.length());

            nextNumber = Integer.parseInt(sequencePart) + 1;
        }

        return prefix + String.format("%05d", nextNumber);
    }
}
