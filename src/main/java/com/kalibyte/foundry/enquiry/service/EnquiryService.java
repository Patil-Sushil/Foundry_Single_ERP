package com.kalibyte.foundry.enquiry.service;

import com.kalibyte.foundry.common.response.PageResponse;
import com.kalibyte.foundry.enquiry.dto.EnquiryCreateRequest;
import com.kalibyte.foundry.enquiry.dto.EnquiryResponse;

import java.util.UUID;


public interface EnquiryService {
    EnquiryResponse create(EnquiryCreateRequest request);

    PageResponse<EnquiryResponse> getAll(int page, int size);

    EnquiryResponse getById(UUID enquiryId);
}
