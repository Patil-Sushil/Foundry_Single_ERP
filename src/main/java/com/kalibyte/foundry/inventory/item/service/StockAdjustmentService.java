package com.kalibyte.foundry.inventory.item.service;

import com.kalibyte.foundry.common.security.UserPrincipal;
import com.kalibyte.foundry.inventory.item.dto.request.StockAdjustmentRequest;
import com.kalibyte.foundry.inventory.item.dto.response.ItemResponse;

/**
 * Service for manual stock adjustments.
 */
public interface StockAdjustmentService {
    
    /**
     * Manually adjust item stock (e.g., after physical verification).
     *
     * @param itemId         The ID of the item to adjust.
     * @param request        Adjustment details (quantity, rate, reason).
     * @param userPrincipal  The authenticated user principal performing the adjustment.
     * @return Updated item details.
     */
    ItemResponse adjustStock(Long itemId, StockAdjustmentRequest request, UserPrincipal userPrincipal);
}
