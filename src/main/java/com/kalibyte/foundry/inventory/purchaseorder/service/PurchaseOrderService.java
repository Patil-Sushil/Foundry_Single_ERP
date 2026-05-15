package com.kalibyte.foundry.inventory.purchaseorder.service;

import com.kalibyte.foundry.common.response.PageResponse;
import com.kalibyte.foundry.inventory.purchaseorder.dto.request.CreatePurchaseOrderRequest;
import com.kalibyte.foundry.inventory.purchaseorder.dto.response.LastPurchaseRate;
import com.kalibyte.foundry.inventory.purchaseorder.dto.response.PurchaseOrderResponse;
import com.kalibyte.foundry.inventory.purchaseorder.dto.response.PurchaseOrderSummary;
import com.kalibyte.foundry.inventory.purchaseorder.entity.PurchaseOrder;
import com.kalibyte.foundry.inventory.purchaseorder.entity.enums.POStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PurchaseOrderService {
    PurchaseOrderResponse create(CreatePurchaseOrderRequest request);
    PurchaseOrderResponse getById(Long id);
    PageResponse<PurchaseOrderSummary> getAll(POStatus status, Long vendorId, Pageable pageable);
    List<PurchaseOrderSummary> getOpenOrders();
    PurchaseOrderResponse cancel(Long id);
    LastPurchaseRate getLastPurchaseRate(Long itemId, Long vendorId);
    void updateStatusAfterInward(PurchaseOrder po);
}
