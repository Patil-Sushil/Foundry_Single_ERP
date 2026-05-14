package com.kalibyte.foundry.inventory.item.service.impl;

import com.kalibyte.foundry.common.exception.ResourceNotFoundException;
import com.kalibyte.foundry.common.security.UserPrincipal;
import com.kalibyte.foundry.inventory.item.dto.request.StockAdjustmentRequest;
import com.kalibyte.foundry.inventory.item.dto.response.ItemResponse;
import com.kalibyte.foundry.inventory.item.entity.AdjustmentItem;
import com.kalibyte.foundry.inventory.item.entity.Item;
import com.kalibyte.foundry.inventory.item.entity.StockAdjustment;
import com.kalibyte.foundry.inventory.item.repository.ItemRepository;
import com.kalibyte.foundry.inventory.item.repository.StockAdjustmentRepository;
import com.kalibyte.foundry.inventory.item.service.StockAdjustmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class StockAdjustmentServiceImpl implements StockAdjustmentService {

    private final ItemRepository itemRepository;
    private final StockAdjustmentRepository stockAdjustmentRepository;

    @Override
    @Transactional
    @CacheEvict(value = "items", allEntries = true)
    public ItemResponse adjustStock(Long itemId, StockAdjustmentRequest request, UserPrincipal userPrincipal) {
        // 1. Fetch Item
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + itemId));

        // 2. Create StockAdjustment record
        StockAdjustment adjustment = StockAdjustment.builder()
                .adjustmentNumber("ADJ-" + System.currentTimeMillis())
                .adjustmentDate(LocalDate.now())
                .reason(request.reason())
                .adjustedByUserId(userPrincipal.getUserId())
                .build();

        // 4. Create AdjustmentItem
        AdjustmentItem adjustmentItem = AdjustmentItem.builder()
                .item(item)
                .stockAdjustment(adjustment)
                .adjustedQuantity(request.quantity())
                .unitRate(request.unitRate())
                .build();
        
        // Linking adjustment item
        adjustment.addItem(adjustmentItem);

        // 5. Update Item stock using domain logic
        item.adjustStock(request.quantity(), request.unitRate());

        // 6. Save (Cascade handles AdjustmentItem, JPA Auditing handles audit fields)
        stockAdjustmentRepository.save(adjustment);
        Item updatedItem = itemRepository.save(item);

        // 7. Return mapped ItemResponse
        return toResponse(updatedItem);
    }

    private ItemResponse toResponse(Item item) {
        return new ItemResponse(
                item.getId(),
                item.getName(),
                item.getCode(),
                item.getDescription(),
                item.getCategory(),
                item.getSubCategory(),
                item.getDepartment() != null ? item.getDepartment().getName() : null,
                item.getUnit(),
                item.getCurrentStock(),
                item.getReorderLevel(),
                item.getMinStockLevel(),
                item.getLocation(),
                item.getLastPurchaseRate(),
                item.getAvgRate(),
                item.getStockValue(),
                item.getStockStatus(),
                item.getHsnCode(),
                item.getGstRate(),
                item.getIsActive(),
                item.getIsScrap(),
                item.getCreatedAt()
        );
    }
}
