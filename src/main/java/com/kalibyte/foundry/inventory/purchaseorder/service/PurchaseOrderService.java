package com.kalibyte.foundry.inventory.purchaseorder.service;

import com.kalibyte.foundry.common.exception.ResourceNotFoundException;
import com.kalibyte.foundry.inventory.common.PONumberGenerator;
import com.kalibyte.foundry.inventory.item.entity.Item;
import com.kalibyte.foundry.inventory.item.repository.ItemRepository;
import com.kalibyte.foundry.inventory.purchaseorder.dto.request.CreatePurchaseOrderRequest;
import com.kalibyte.foundry.inventory.purchaseorder.dto.request.PurchaseOrderItemRequest;
import com.kalibyte.foundry.inventory.purchaseorder.dto.response.LastPurchaseRate;
import com.kalibyte.foundry.inventory.purchaseorder.dto.response.OrderItemDetail;
import com.kalibyte.foundry.inventory.purchaseorder.dto.response.PurchaseOrderResponse;
import com.kalibyte.foundry.inventory.purchaseorder.dto.response.PurchaseOrderSummary;
import com.kalibyte.foundry.inventory.purchaseorder.entity.PurchaseOrderItem;
import com.kalibyte.foundry.inventory.purchaseorder.entity.PurchaseOrder;
import com.kalibyte.foundry.inventory.purchaseorder.entity.enums.POStatus;
import com.kalibyte.foundry.inventory.purchaseorder.repository.ItemVendorRateRepository;
import com.kalibyte.foundry.inventory.purchaseorder.repository.PurchaseOrderRepository;
import com.kalibyte.foundry.inventory.vendor.entity.Vendor;
import com.kalibyte.foundry.inventory.vendor.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final VendorRepository vendorRepository;
    private final ItemRepository itemRepository;
    private final ItemVendorRateRepository itemVendorRateRepository;
    private final PONumberGenerator poNumberGenerator;

    @Transactional
    public PurchaseOrderResponse create(CreatePurchaseOrderRequest request) {
        Vendor vendor = vendorRepository.findById(request.vendorId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with id: " + request.vendorId()));

        PurchaseOrder po = PurchaseOrder.builder()
                .poNumber(poNumberGenerator.generate())
                .vendor(vendor)
                .status(POStatus.OPEN)
                .notes(request.notes())
                .expectedDeliveryDate(request.expectedDeliveryDate())
                .createdByUserId(com.kalibyte.foundry.common.util.SecurityUtils.getCurrentUserId())
                .build();

        for (PurchaseOrderItemRequest itemRequest : request.items()) {
            Item item = itemRepository.findById(itemRequest.itemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + itemRequest.itemId()));

            PurchaseOrderItem orderItem = PurchaseOrderItem.builder()
                    .item(item)
                    .orderedQuantity(itemRequest.quantity())
                    .unitRate(itemRequest.unitRate())
                    .notes(itemRequest.notes())
                    .build();
            
            po.addOrderItem(orderItem);
        }

        return toResponse(purchaseOrderRepository.save(po));
    }

    @Transactional(readOnly = true)
    public PurchaseOrderResponse getById(Long id) {
        PurchaseOrder po = purchaseOrderRepository.findWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found with id: " + id));
        return toResponse(po);
    }

    @Transactional(readOnly = true)
    public Page<PurchaseOrderSummary> getAll(POStatus status, Long vendorId, Pageable pageable) {
        return purchaseOrderRepository.findAllFiltered(status, vendorId, pageable)
                .map(this::toSummary);
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrderSummary> getOpenOrders() {
        return purchaseOrderRepository.findByStatusInOrderByPoDateDesc(
                List.of(POStatus.OPEN, POStatus.PARTIALLY_RECEIVED))
                .stream()
                .map(this::toSummary)
                .toList();
    }

    @Transactional
    public PurchaseOrderResponse cancel(Long id) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found with id: " + id));
        
        po.cancel(); // Validates status internally
        return toResponse(purchaseOrderRepository.save(po));
    }

    @Transactional(readOnly = true)
    public Optional<LastPurchaseRate> getLastPurchaseRate(Long itemId, Long vendorId) {
        return itemVendorRateRepository.findByItemIdAndVendorId(itemId, vendorId)
                .map(rate -> new LastPurchaseRate(
                        rate.getItem().getId(),
                        rate.getVendor().getId(),
                        rate.getLastRate(),
                        rate.getLastPurchasedOn()
                ));
    }

    @Transactional
    public void updateStatusAfterInward(PurchaseOrder po) {
        po.updateStatusAfterInward();
        purchaseOrderRepository.save(po);
    }

    private PurchaseOrderResponse toResponse(PurchaseOrder po) {
        List<OrderItemDetail> itemDetails = po.getOrderItems().stream()
                .map(item -> new OrderItemDetail(
                        item.getId(),
                        item.getItem().getId(),
                        item.getItem().getName(),
                        item.getItem().getCode(),
                        item.getItem().getUnit(),
                        item.getOrderedQuantity(),
                        item.getReceivedQuantity(),
                        item.getPendingQuantity(),
                        item.getUnitRate(),
                        item.getTotalValue(),
                        item.getNotes()
                ))
                .toList();

        return new PurchaseOrderResponse(
                po.getId(),
                po.getPoNumber(),
                po.getStatus(),
                po.getVendor().getName(),
                po.getVendor().getId(),
                itemDetails,
                po.getTotalOrderValue(),
                po.getPoDate(),
                po.getExpectedDeliveryDate(),
                po.getNotes(),
                po.getCreatedAt()
        );
    }

    private PurchaseOrderSummary toSummary(PurchaseOrder po) {
        return new PurchaseOrderSummary(
                po.getId(),
                po.getPoNumber(),
                po.getVendor().getName(),
                po.getStatus(),
                po.getOrderItems().size(),
                po.getTotalOrderValue(),
                po.getPoDate(),
                po.getExpectedDeliveryDate()
        );
    }
}
