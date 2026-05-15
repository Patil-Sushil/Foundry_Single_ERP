package com.kalibyte.foundry.quotation.service;

import com.kalibyte.foundry.quotation.entity.Quotation;

public interface QuotationPdfService {
    byte[] generatePdf(Quotation quotation);
}
