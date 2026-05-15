package com.kalibyte.foundry.scrap.service;

import com.kalibyte.foundry.scrap.dto.request.ScrapEntryRequest;
import com.kalibyte.foundry.scrap.dto.response.ScrapEntryResponse;
import com.kalibyte.foundry.scrap.enums.ApprovalDecision;
import com.kalibyte.foundry.scrap.enums.ScrapStatus;

import java.util.List;

public interface ScrapService {
    List<ScrapEntryResponse> getAll();
    List<ScrapEntryResponse> getByStatus(ScrapStatus status);
    ScrapEntryResponse getById(Long id);
    ScrapEntryResponse createScrapEntry(ScrapEntryRequest request);
    ScrapEntryResponse verifyScrap(Long id, String verifiedBy, String notes);
    ScrapEntryResponse approveScrap(Long id, String approvedBy, ApprovalDecision decision, String notes, String finalGrade);
}
