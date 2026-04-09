package com.kalibyte.foundry.billing.creditnote.service;

import com.kalibyte.foundry.billing.creditnote.dto.response.CreditNoteResponse;
import com.kalibyte.foundry.qa.customerreturn.entity.CustomerReturn;

import java.math.BigDecimal;
import java.util.UUID;

public interface CreditNoteService {
    CreditNoteResponse generateCreditNoteFromReturn(CustomerReturn customerReturn, BigDecimal creditAmount);
    CreditNoteResponse getById(UUID id);
}
