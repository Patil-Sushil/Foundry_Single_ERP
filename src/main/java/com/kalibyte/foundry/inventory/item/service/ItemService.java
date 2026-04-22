package com.kalibyte.foundry.inventory.item.service;

import com.kalibyte.foundry.common.response.PageResponse;
import com.kalibyte.foundry.inventory.item.dto.request.CreateItemRequest;
import com.kalibyte.foundry.inventory.item.dto.request.UpdateItemRequest;
import com.kalibyte.foundry.inventory.item.dto.response.ItemResponse;
import com.kalibyte.foundry.inventory.item.dto.response.ItemSummary;
import com.kalibyte.foundry.inventory.item.entity.enums.ItemCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ItemService {
     ItemResponse create(CreateItemRequest request);
     ItemResponse update(Long id, UpdateItemRequest request);
     ItemResponse getById(Long id);
     Page<ItemResponse> getAll(ItemCategory category, Boolean isActive, Boolean isScrap, Pageable pageable);
     List<ItemSummary> search(String query, Boolean isScrap);
     List<ItemResponse> getLowStockItems();
     ItemResponse toggleStatus(Long id);
}
