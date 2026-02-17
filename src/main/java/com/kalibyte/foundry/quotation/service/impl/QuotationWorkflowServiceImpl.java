package com.kalibyte.foundry.quotation.service.impl;

import com.kalibyte.foundry.common.exception.ResourceNotFoundException;
import com.kalibyte.foundry.quotation.entity.Quotation;
import com.kalibyte.foundry.quotation.entity.enums.QuotationStatus;
import com.kalibyte.foundry.quotation.repository.QuotationRepository;
import com.kalibyte.foundry.quotation.service.QuotationWorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class QuotationWorkflowServiceImpl
        implements QuotationWorkflowService {

    private final QuotationRepository quotationRepository;

    @Override
    public void submit(UUID id) {
        Quotation q = get(id);
        q.setStatus(QuotationStatus.PENDING_APPROVAL);
        quotationRepository.save(q);
    }

    @Override
    public void approve(UUID id) {
        Quotation q = get(id);
        q.setStatus(QuotationStatus.APPROVED);
        quotationRepository.save(q);
    }

    @Override
    public void reject(UUID id, String reason) {
        Quotation q = get(id);
        q.setStatus(QuotationStatus.REJECTED);
        quotationRepository.save(q);
    }

    @Override
    public void accept(UUID id) {
        Quotation q = get(id);
        q.setStatus(QuotationStatus.ACCEPTED);
        quotationRepository.save(q);
    }

    private Quotation get(UUID id) {
        return quotationRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Quotation not found"));
    }
}
