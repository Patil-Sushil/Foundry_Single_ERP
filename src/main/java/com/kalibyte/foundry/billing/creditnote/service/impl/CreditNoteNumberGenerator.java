package com.kalibyte.foundry.billing.creditnote.service.impl;

import com.kalibyte.foundry.billing.creditnote.entity.CreditNote;
import com.kalibyte.foundry.billing.creditnote.repository.CreditNoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Year;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CreditNoteNumberGenerator {

    private final CreditNoteRepository creditNoteRepository;

    public String generateCreditNoteNumber() {
        int year = Year.now().getValue();
        String prefix = "CN-" + year + "-";

        Optional<CreditNote> lastNote =
                creditNoteRepository.findTopByCreditNoteNumberStartingWithOrderByCreditNoteNumberDesc(prefix);

        int nextNumber = 1;
        if (lastNote.isPresent()) {
            String lastNumber = lastNote.get().getCreditNoteNumber();
            String sequencePart = lastNumber.substring(prefix.length());
            nextNumber = Integer.parseInt(sequencePart) + 1;
        }

        return prefix + String.format("%05d", nextNumber);
    }
}
