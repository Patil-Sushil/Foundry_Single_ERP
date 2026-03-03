package com.kalibyte.foundry.inventory.inward.service;

import com.kalibyte.foundry.common.exception.BusinessException;
import com.kalibyte.foundry.common.exception.ResourceNotFoundException;
import com.kalibyte.foundry.inventory.common.InwardNumberGenerator;
import com.kalibyte.foundry.inventory.inward.dto.request.StartInwardRequest;
import com.kalibyte.foundry.inventory.inward.dto.request.UpdateReceivedQuantityRequest;
import com.kalibyte.foundry.inventory.inward.dto.response.*;
import com.kalibyte.foundry.inventory.inward.entity.MaterialInward;
import com.kalibyte.foundry.inventory.inward.entity.ReceivedItem;
import com.kalibyte.foundry.inventory.inward.entity.enums.InwardStatus;
import com.kalibyte.foundry.inventory.inward.entity.enums.ReceiptStatus;
import com.kalibyte.foundry.inventory.inward.repository.MaterialInwardRepository;
import com.kalibyte.foundry.inventory.inward.repository.ReceivedItemRepository;
import com.kalibyte.foundry.inventory.item.entity.Item;
import com.kalibyte.foundry.inventory.item.repository.ItemRepository;
import com.kalibyte.foundry.inventory.ledger.service.VendorLedgerService;
import com.kalibyte.foundry.inventory.purchaseorder.entity.ItemVendorRate;
import com.kalibyte.foundry.inventory.purchaseorder.entity.PurchaseOrderItem;
import com.kalibyte.foundry.inventory.purchaseorder.entity.PurchaseOrder;
import com.kalibyte.foundry.inventory.purchaseorder.repository.ItemVendorRateRepository;
import com.kalibyte.foundry.inventory.purchaseorder.repository.PurchaseOrderRepository;
import com.kalibyte.foundry.inventory.purchaseorder.service.PurchaseOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MaterialInwardService {

    private final MaterialInwardRepository materialInwardRepository;
    private final ReceivedItemRepository receivedItemRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderService purchaseOrderService;
    private final ItemRepository itemRepository;
    private final ItemVendorRateRepository itemVendorRateRepository;
    private final VendorLedgerService vendorLedgerService;
    private final InwardNumberGenerator inwardNumberGenerator;

    @Transactional
    public InwardResponse startFromPO(Long poId, StartInwardRequest request) {
        PurchaseOrder po = purchaseOrderRepository.findWithDetails(poId)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found with id: " + poId));

        if (!po.isOpen()) {
            throw new BusinessException("Purchase Order is not open for inward.");
        }

        MaterialInward inward = MaterialInward.builder()
                .inwardNumber(inwardNumberGenerator.generate())
                .purchaseOrder(po)
                .vendor(po.getVendor())
                .vehicleNumber(request.vehicleNumber())
                .driverName(request.driverName())
                .driverPhone(request.driverPhone())
                .vendorChallanNumber(request.vendorChallanNumber())
                .inwardDate(LocalDate.now())
                .status(InwardStatus.DRAFT)
                .createdByUserId(com.kalibyte.foundry.common.util.SecurityUtils.getCurrentUserId())
                .build();

        for (PurchaseOrderItem orderItem : po.getOrderItems()) {
            ReceivedItem receivedItem = ReceivedItem.builder()
                    .item(orderItem.getItem())
                    .orderItem(orderItem)
                    .poQuantity(orderItem.getOrderedQuantity())
                    .receivedQuantity(orderItem.getOrderedQuantity()) // Default to ordered
                    .unitRate(orderItem.getUnitRate())
                    .build();
            inward.addReceivedItem(receivedItem);
        }

        return toResponse(materialInwardRepository.save(inward));
    }

    @Transactional
    public InwardResponse updateReceivedQuantities(Long inwardId, List<UpdateReceivedQuantityRequest> updates) {
        MaterialInward inward = materialInwardRepository.findById(inwardId)
                .orElseThrow(() -> new ResourceNotFoundException("Inward not found with id: " + inwardId));

        if (!inward.isDraft()) {
            throw new BusinessException("Cannot update quantities for a confirmed inward.");
        }

        Map<Long, UpdateReceivedQuantityRequest> updateMap = updates.stream()
                .collect(Collectors.toMap(UpdateReceivedQuantityRequest::receivedItemId, Function.identity()));

        for (ReceivedItem item : inward.getReceivedItems()) {
            if (updateMap.containsKey(item.getId())) {
                UpdateReceivedQuantityRequest req = updateMap.get(item.getId());
                item.setReceivedQuantity(req.receivedQuantity());
                if (req.unitRate() != null) {
                    item.setUnitRate(req.unitRate());
                }
            }
        }

        return toResponse(materialInwardRepository.save(inward));
    }

    @Transactional(readOnly = true)
    public InwardReviewResponse getReview(Long inwardId) {
        MaterialInward inward = materialInwardRepository.findWithFullDetails(inwardId)
                .orElseThrow(() -> new ResourceNotFoundException("Inward not found with id: " + inwardId));

        List<ReceivedItemComparison> comparisons = inward.getReceivedItems().stream()
                .map(item -> new ReceivedItemComparison(
                        item.getId(),
                        item.getItem().getName(),
                        item.getItem().getCode(),
                        item.getItem().getUnit().name(),
                        item.getPoQuantity() != null ? item.getPoQuantity() : BigDecimal.ZERO,
                        item.getReceivedQuantity(),
                        item.getQuantityDifference(),
                        item.getReceiptStatus().name(),
                        item.getUnitRate(),
                        item.getAmount()
                ))
                .toList();

        boolean hasShortage = comparisons.stream().anyMatch(c -> ReceiptStatus.SHORT.name().equals(c.receiptStatus()));
        boolean hasExcess = comparisons.stream().anyMatch(c -> ReceiptStatus.EXCESS.name().equals(c.receiptStatus()));

        return new InwardReviewResponse(
                inward.getId(),
                inward.getInwardNumber(),
                inward.getPurchaseOrder() != null ? inward.getPurchaseOrder().getPoNumber() : null,
                inward.getVendor().getName(),
                inward.getVehicleNumber(),
                inward.getVendorChallanNumber(),
                inward.getInwardDate(),
                comparisons,
                inward.getTotalAmount(),
                hasShortage,
                hasExcess
        );
    }

    @Transactional
    public InwardResponse confirm(Long inwardId) {
        MaterialInward inward = materialInwardRepository.findWithFullDetails(inwardId)
                .orElseThrow(() -> new ResourceNotFoundException("Inward not found with id: " + inwardId));

        inward.confirm(com.kalibyte.foundry.common.util.SecurityUtils.getCurrentUserId()); // Updates status and confirmedBy

        for (ReceivedItem receivedItem : inward.getReceivedItems()) {
            Item item = receivedItem.getItem();
            
            // Update Item Stock and Avg Rate
            item.receiveStock(receivedItem.getReceivedQuantity(), receivedItem.getUnitRate());
            itemRepository.save(item);

            // Update Order Item Received Quantity if linked
            if (receivedItem.getOrderItem() != null) {
                PurchaseOrderItem orderItem = receivedItem.getOrderItem();
                orderItem.addReceivedQuantity(receivedItem.getReceivedQuantity());
            }

            // Update Item Vendor Rate
            Optional<ItemVendorRate> existingRate = itemVendorRateRepository
                    .findByItemIdAndVendorId(item.getId(), inward.getVendor().getId());

            ItemVendorRate rate = existingRate.orElseGet(() -> ItemVendorRate.builder()
                    .item(item)
                    .vendor(inward.getVendor())
                    .build());
            
            rate.setLastRate(receivedItem.getUnitRate());
            rate.setLastPurchasedOn(LocalDate.now());
            itemVendorRateRepository.save(rate);
        }

        // Create Ledger Entry
        vendorLedgerService.recordInwardEntry(inward);

        // Update PO Status
        if (inward.getPurchaseOrder() != null) {
            purchaseOrderService.updateStatusAfterInward(inward.getPurchaseOrder());
        }

        return toResponse(materialInwardRepository.save(inward));
    }

    @Transactional(readOnly = true)
    public InwardResponse getById(Long id) {
        MaterialInward inward = materialInwardRepository.findWithFullDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inward not found with id: " + id));
        return toResponse(inward);
    }

    @Transactional(readOnly = true)
    public Page<InwardSummary> getAll(InwardStatus status, Long vendorId, LocalDate from, LocalDate to, Pageable pageable) {
        return materialInwardRepository.findAllFiltered(status, vendorId, from, to, pageable)
                .map(this::toSummary);
    }

    private InwardResponse toResponse(MaterialInward inward) {
        List<ReceivedItemDetail> items = inward.getReceivedItems().stream()
                .map(item -> new ReceivedItemDetail(
                        item.getId(),
                        item.getItem().getId(),
                        item.getItem().getName(),
                        item.getItem().getCode(),
                        item.getItem().getUnit().name(),
                        item.getPoQuantity(),
                        item.getReceivedQuantity(),
                        item.getUnitRate(),
                        item.getAmount(),
                        item.getNotes()
                ))
                .toList();

        return new InwardResponse(
                inward.getId(),
                inward.getInwardNumber(),
                inward.getStatus(),
                inward.getPurchaseOrder() != null ? inward.getPurchaseOrder().getPoNumber() : null,
                inward.getVendor().getName(),
                inward.getVehicleNumber(),
                inward.getDriverName(),
                inward.getDriverPhone(),
                inward.getVendorChallanNumber(),
                inward.getInwardDate(),
                items,
                inward.getTotalAmount(),
                inward.getConfirmedAt(),
                inward.getCreatedAt()
        );
    }

    private InwardSummary toSummary(MaterialInward inward) {
        return new InwardSummary(
                inward.getId(),
                inward.getInwardNumber(),
                inward.getVendor().getName(),
                inward.getStatus(),
                inward.getInwardDate(),
                inward.getReceivedItems().size(),
                inward.getTotalAmount()
        );
    }
}
