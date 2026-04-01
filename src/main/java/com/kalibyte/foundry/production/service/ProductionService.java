package com.kalibyte.foundry.production.service;

import com.kalibyte.foundry.common.response.PageResponse;
import com.kalibyte.foundry.production.dto.request.ProductionEntryRequest;
import com.kalibyte.foundry.production.dto.request.UpdateStatusRequest;
import com.kalibyte.foundry.production.dto.response.entry.ProductionEntryListItem;
import com.kalibyte.foundry.production.dto.response.entry.ProductionEntryResponse;
import com.kalibyte.foundry.production.entity.enums.ProductionShift;
import com.kalibyte.foundry.production.entity.enums.ProductionStatus;

import java.time.LocalDate;
import java.util.UUID;

public interface ProductionService {

    ProductionEntryResponse createEntry(ProductionEntryRequest request);

    ProductionEntryResponse getById(UUID id);

    PageResponse<ProductionEntryListItem> list(
            UUID orderId,
            LocalDate fromDate,
            LocalDate toDate,
            ProductionStatus status,
            ProductionShift shift,
            int page,
            int size
    );

    ProductionEntryResponse updateStatus(UUID id, UpdateStatusRequest request);

    void delete(UUID id);

    // ProductionService.java — add
    ProductionEntryResponse updateEntry(UUID id, ProductionEntryRequest request);
}