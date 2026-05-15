package com.kalibyte.foundry.inventory.inward.service;

import com.kalibyte.foundry.common.response.PageResponse;
import com.kalibyte.foundry.inventory.inward.dto.request.ConfirmInwardRequest;
import com.kalibyte.foundry.inventory.inward.dto.request.InternalReturnRequest;
import com.kalibyte.foundry.inventory.inward.dto.request.StartInwardRequest;
import com.kalibyte.foundry.inventory.inward.dto.request.UpdateReceivedQuantityRequest;
import com.kalibyte.foundry.inventory.inward.dto.response.InwardResponse;
import com.kalibyte.foundry.inventory.inward.dto.response.InwardReviewResponse;
import com.kalibyte.foundry.inventory.inward.dto.response.InwardSummary;
import com.kalibyte.foundry.inventory.inward.entity.enums.InwardStatus;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface MaterialInwardService {
    InwardResponse startFromPO(Long poId, StartInwardRequest request);
    InwardResponse updateReceivedQuantities(Long inwardId, List<UpdateReceivedQuantityRequest> updates);
    InwardReviewResponse getReview(Long inwardId);
    InwardResponse confirm(Long inwardId, ConfirmInwardRequest request);
    InwardResponse getById(Long id);
    PageResponse<InwardSummary> getAll(InwardStatus status, Long vendorId, LocalDate from, LocalDate to, Pageable pageable);
    InwardResponse createInternalReturnInward(InternalReturnRequest request);
}
