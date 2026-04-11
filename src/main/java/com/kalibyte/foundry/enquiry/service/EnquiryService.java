package com.kalibyte.foundry.enquiry.service;

import com.kalibyte.foundry.common.response.PageResponse;
import com.kalibyte.foundry.enquiry.dto.request.EnquiryCreateRequest;
import com.kalibyte.foundry.enquiry.dto.request.EnquiryReviseRequest;
import com.kalibyte.foundry.enquiry.dto.response.EnquiryResponse;
import com.kalibyte.foundry.enquiry.entity.enums.EnquiryStatus;

import java.util.UUID;


public interface EnquiryService {
    EnquiryResponse create(EnquiryCreateRequest request);

    PageResponse<EnquiryResponse> getAll(int page, int size);

    EnquiryResponse getById(UUID enquiryId);

    PageResponse<EnquiryResponse> getByCustomerId(UUID customerId, int page, int size);

    EnquiryResponse updateStatus(UUID enquiryId, EnquiryStatus newStatus);

    EnquiryResponse reviseEnquiry(UUID enquiryId, EnquiryReviseRequest request);
}
