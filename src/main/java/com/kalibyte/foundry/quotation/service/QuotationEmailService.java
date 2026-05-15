package com.kalibyte.foundry.quotation.service;

import com.kalibyte.foundry.quotation.entity.Quotation;

public interface QuotationEmailService {
    void sendQuotationEmail(Quotation quotation);
}
