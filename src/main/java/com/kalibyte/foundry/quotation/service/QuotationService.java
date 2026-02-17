package com.kalibyte.foundry.quotation.service;

import com.kalibyte.foundry.common.response.PageResponse;
import com.kalibyte.foundry.quotation.dto.request.QuotationCreateRequest;
import com.kalibyte.foundry.quotation.dto.response.QuotationResponse;
import com.kalibyte.foundry.quotation.entity.Quotation;
import com.kalibyte.foundry.quotation.entity.enums.QuotationStatus;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface QuotationService {

    Quotation create(QuotationCreateRequest request);

    Quotation get(UUID id);

    PageResponse<QuotationResponse> list(Pageable pageable);

    Quotation update(UUID id, QuotationCreateRequest request);

    Quotation updateStatus(UUID id, QuotationStatus status);

}
