package com.kalibyte.foundry.production.service;

import com.kalibyte.foundry.production.dto.request.ProductionEntryRequest;
import com.kalibyte.foundry.production.dto.request.UpdateStatusRequest;
import com.kalibyte.foundry.production.dto.response.entry.ProductionEntryResponse;

import java.util.UUID;

public interface ProductionService {

    ProductionEntryResponse createEntry(ProductionEntryRequest request);

    ProductionEntryResponse getById(UUID id);

    ProductionEntryResponse updateStatus(UUID id, UpdateStatusRequest request);

    void delete(UUID id);
}
