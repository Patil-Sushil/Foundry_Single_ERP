package com.kalibyte.foundry.inventory.purchaseorder.service;

import com.kalibyte.foundry.common.exception.ResourceNotFoundException;
import com.kalibyte.foundry.inventory.common.PONumberGenerator;
import com.kalibyte.foundry.inventory.item.entity.Item;
import com.kalibyte.foundry.inventory.item.repository.ItemRepository;
import com.kalibyte.foundry.inventory.purchaseorder.dto.request.CreatePurchaseOrderRequest;
import com.kalibyte.foundry.inventory.purchaseorder.dto.request.PurchaseOrderItemRequest;
import com.kalibyte.foundry.inventory.purchaseorder.dto.response.LastPurchaseRate;
import com.kalibyte.foundry.inventory.purchaseorder.dto.response.PurchaseOrderResponse;
import com.kalibyte.foundry.inventory.purchaseorder.dto.response.PurchaseOrderSummary;
import com.kalibyte.foundry.inventory.purchaseorder.entity.PurchaseOrderItem;
import com.kalibyte.foundry.inventory.purchaseorder.entity.PurchaseOrder;
import com.kalibyte.foundry.inventory.purchaseorder.entity.enums.POStatus;
import com.kalibyte.foundry.inventory.purchaseorder.mapper.PurchaseOrderMapper;
import com.kalibyte.foundry.inventory.purchaseorder.repository.ItemVendorRateRepository;
import com.kalibyte.foundry.inventory.purchaseorder.repository.PurchaseOrderRepository;
import com.kalibyte.foundry.inventory.vendor.entity.Vendor;
import com.kalibyte.foundry.inventory.vendor.repository.VendorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final VendorRepository vendorRepository;
    private final ItemRepository itemRepository;
    private final ItemVendorRateRepository itemVendorRateRepository;
    private final PONumberGenerator poNumberGenerator;
    private final PurchaseOrderMapper purchaseOrderMapper;

	public PurchaseOrderService(PurchaseOrderRepository purchaseOrderRepository, 
                                VendorRepository vendorRepository, 
                                ItemRepository itemRepository, 
                                ItemVendorRateRepository itemVendorRateRepository, 
                                PONumberGenerator poNumberGenerator,
                                PurchaseOrderMapper purchaseOrderMapper) {
		this.purchaseOrderRepository = purchaseOrderRepository;
		this.vendorRepository = vendorRepository;
		this.itemRepository = itemRepository;
		this.itemVendorRateRepository = itemVendorRateRepository;
		this.poNumberGenerator = poNumberGenerator;
        this.purchaseOrderMapper = purchaseOrderMapper;
	}

	@Transactional
    public PurchaseOrderResponse create(CreatePurchaseOrderRequest request) {
        Vendor vendor = vendorRepository.findById(request.vendorId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with id: " + request.vendorId()));

        PurchaseOrder po = purchaseOrderMapper.toEntity(request);
        po.setPoNumber(poNumberGenerator.generate());
        po.setVendor(vendor);
        po.setStatus(POStatus.OPEN);
        po.setPoDate(LocalDate.now());
        po.setCreatedByUserId(com.kalibyte.foundry.common.util.SecurityUtils.getCurrentUserId());

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

        return purchaseOrderMapper.toResponse(purchaseOrderRepository.save(po));
    }

    @Transactional(readOnly = true)
    public PurchaseOrderResponse getById(Long id) {
        PurchaseOrder po = purchaseOrderRepository.findWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found with id: " + id));
        return purchaseOrderMapper.toResponse(po);
    }

    @Transactional(readOnly = true)
    public Page<PurchaseOrderSummary> getAll(POStatus status, Long vendorId, Pageable pageable) {
        return purchaseOrderRepository.findAllFiltered(status, vendorId, pageable)
                .map(purchaseOrderMapper::toSummary);
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrderSummary> getOpenOrders() {
        return purchaseOrderRepository.findByStatusInOrderByPoDateDesc(
                List.of(POStatus.OPEN, POStatus.PARTIALLY_RECEIVED))
                .stream()
                .map(purchaseOrderMapper::toSummary)
                .toList();
    }

    @Transactional
    public PurchaseOrderResponse cancel(Long id) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found with id: " + id));
        
        po.cancel(); // Validates status internally
        return purchaseOrderMapper.toResponse(purchaseOrderRepository.save(po));
    }

    @Transactional(readOnly = true)
    public LastPurchaseRate getLastPurchaseRate(Long itemId, Long vendorId) {
        if (!itemRepository.existsById(itemId)) {
            throw new ResourceNotFoundException("Item not found with id: " + itemId);
        }
        if (!vendorRepository.existsById(vendorId)) {
            throw new ResourceNotFoundException("Vendor not found with id: " + vendorId);
        }

        return itemVendorRateRepository.findByItemIdAndVendorId(itemId, vendorId)
                .map(purchaseOrderMapper::toLastPurchaseRate)
                .orElseThrow(() -> new ResourceNotFoundException("No purchase rate found for Item ID: " + itemId + " and Vendor ID: " + vendorId));
    }

    @Transactional
    public void updateStatusAfterInward(PurchaseOrder po) {
        po.updateStatusAfterInward();
        purchaseOrderRepository.save(po);
    }
}
