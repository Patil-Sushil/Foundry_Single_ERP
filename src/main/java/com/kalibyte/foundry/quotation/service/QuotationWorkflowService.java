package com.kalibyte.foundry.quotation.service;

import java.util.UUID;

public interface QuotationWorkflowService {

    void submit(UUID id);

    void approve(UUID id);

    void reject(UUID id, String reason);

    void accept(UUID id);
}
