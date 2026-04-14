package com.kalibyte.foundry.inventory.inward.service;

import com.kalibyte.foundry.common.exception.BusinessException;
import com.kalibyte.foundry.common.exception.ResourceNotFoundException;
import com.kalibyte.foundry.common.response.PageResponse;
import com.kalibyte.foundry.common.util.SecurityUtils;
import com.kalibyte.foundry.inventory.common.InwardNumberGenerator;
import com.kalibyte.foundry.inventory.inward.dto.request.ConfirmInwardRequest;
import com.kalibyte.foundry.inventory.inward.dto.request.InternalReturnRequest;
import com.kalibyte.foundry.inventory.inward.dto.request.StartInwardRequest;
import com.kalibyte.foundry.inventory.inward.dto.request.UpdateReceivedQuantityRequest;
import com.kalibyte.foundry.inventory.inward.dto.response.*;
import com.kalibyte.foundry.inventory.inward.entity.MaterialInward;
import com.kalibyte.foundry.inventory.inward.entity.ReceivedItem;
import com.kalibyte.foundry.inventory.inward.entity.enums.InwardStatus;
import com.kalibyte.foundry.inventory.inward.mapper.InwardMapper;
import com.kalibyte.foundry.inventory.inward.repository.MaterialInwardRepository;
import com.kalibyte.foundry.inventory.inward.repository.ReceivedItemRepository;
import com.kalibyte.foundry.inventory.item.entity.Item;
import com.kalibyte.foundry.inventory.item.repository.ItemRepository;
import com.kalibyte.foundry.inventory.ledger.service.VendorLedgerService;
import com.kalibyte.foundry.inventory.purchaseinvoice.entity.PurchaseInvoice;
import com.kalibyte.foundry.inventory.purchaseinvoice.repository.PurchaseInvoiceRepository;
import com.kalibyte.foundry.inventory.purchaseorder.entity.ItemVendorRate;
import com.kalibyte.foundry.inventory.purchaseorder.entity.PurchaseOrderItem;
import com.kalibyte.foundry.inventory.purchaseorder.entity.PurchaseOrder;
import com.kalibyte.foundry.inventory.purchaseorder.repository.ItemVendorRateRepository;
import com.kalibyte.foundry.inventory.purchaseorder.repository.PurchaseOrderRepository;
import com.kalibyte.foundry.inventory.purchaseorder.service.PurchaseOrderService;
import com.kalibyte.foundry.inventory.vendor.repository.VendorRepository;
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
public class MaterialInwardService {

    private final MaterialInwardRepository materialInwardRepository;
    private final ReceivedItemRepository receivedItemRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderService purchaseOrderService;
    private final ItemRepository itemRepository;
    private final ItemVendorRateRepository itemVendorRateRepository;
    private final VendorLedgerService vendorLedgerService;
    private final InwardNumberGenerator inwardNumberGenerator;
    private final InwardMapper inwardMapper;
    private final com.kalibyte.foundry.inventory.vendor.repository.VendorRepository vendorRepository;
    private final PurchaseInvoiceRepository purchaseInvoiceRepository;

    public MaterialInwardService(MaterialInwardRepository materialInwardRepository,
                                 ReceivedItemRepository receivedItemRepository,
                                 PurchaseOrderRepository purchaseOrderRepository,
                                 PurchaseOrderService purchaseOrderService,
                                 ItemRepository itemRepository,
                                 ItemVendorRateRepository itemVendorRateRepository,
                                 VendorLedgerService vendorLedgerService,
                                 InwardNumberGenerator inwardNumberGenerator,
                                 InwardMapper inwardMapper,
                                 VendorRepository vendorRepository,
                                 PurchaseInvoiceRepository purchaseInvoiceRepository) {
        this.materialInwardRepository = materialInwardRepository;
        this.receivedItemRepository = receivedItemRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.purchaseOrderService = purchaseOrderService;
        this.itemRepository = itemRepository;
        this.itemVendorRateRepository = itemVendorRateRepository;
        this.vendorLedgerService = vendorLedgerService;
        this.inwardNumberGenerator = inwardNumberGenerator;
        this.inwardMapper = inwardMapper;
        this.vendorRepository = vendorRepository;
        this.purchaseInvoiceRepository = purchaseInvoiceRepository;
    }

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
                .createdByUserId(SecurityUtils.getCurrentUserId())
                .build();

        for (PurchaseOrderItem orderItem : po.getOrderItems()) {
            BigDecimal taxableAmount = orderItem.getOrderedQuantity().multiply(orderItem.getUnitRate());
            BigDecimal gstRate = orderItem.getGstRate() != null ? orderItem.getGstRate() : BigDecimal.ZERO;
            BigDecimal taxAmount = taxableAmount.multiply(gstRate)
                    .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
            BigDecimal amount = taxableAmount.add(taxAmount).setScale(2, java.math.RoundingMode.HALF_UP);

            ReceivedItem receivedItem = ReceivedItem.builder()
                    .item(orderItem.getItem())
                    .orderItem(orderItem)
                    .poQuantity(orderItem.getOrderedQuantity())
                    .receivedQuantity(orderItem.getOrderedQuantity()) // Default to ordered
                    .unitRate(orderItem.getUnitRate())
                    .gstRate(gstRate)
                    .taxAmount(taxAmount)
                    .amount(amount)
                    .build();
            inward.addReceivedItem(receivedItem);
        }
        inward.calculateTotals();

        // Just save and return — invoice will be created at confirmation time
        return inwardMapper.toResponse(materialInwardRepository.save(inward));
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

                // Recalculate tax and amount
                BigDecimal taxableAmount = item.getReceivedQuantity().multiply(item.getUnitRate());
                BigDecimal gstRate = item.getGstRate() != null ? item.getGstRate() : BigDecimal.ZERO;
                BigDecimal taxAmount = taxableAmount.multiply(gstRate)
                        .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
                item.setTaxAmount(taxAmount);
                item.setAmount(taxableAmount.add(taxAmount).setScale(2, java.math.RoundingMode.HALF_UP));
            }
        }
        inward.calculateTotals();

        // Capture invoice info if provided in any update line
        String latestInvoiceNumber = null;
        LocalDate latestInvoiceDate = null;

        for (UpdateReceivedQuantityRequest req : updates) {
            if (req.vendorInvoiceNumber() != null && !req.vendorInvoiceNumber().isBlank()) {
                latestInvoiceNumber = req.vendorInvoiceNumber();
                latestInvoiceDate = req.vendorInvoiceDate();
            }
        }

        if (latestInvoiceNumber != null) {
            inward.setVendorInvoiceNumber(latestInvoiceNumber);
            inward.setVendorInvoiceDate(latestInvoiceDate != null ? latestInvoiceDate : LocalDate.now());
        }

        inward.calculateTotals();

        // Just save and return — invoice will be created at confirmation time
        return inwardMapper.toResponse(materialInwardRepository.save(inward));
    }

    @Transactional(readOnly = true)
    public InwardReviewResponse getReview(Long inwardId) {
        MaterialInward inward = materialInwardRepository.findWithFullDetails(inwardId)
                .orElseThrow(() -> new ResourceNotFoundException("Inward not found with id: " + inwardId));

        return inwardMapper.toReviewResponse(inward);
    }

    @Transactional
    public InwardResponse confirm(Long inwardId, ConfirmInwardRequest request) {
        MaterialInward inward = materialInwardRepository.findWithFullDetails(inwardId)
                .orElseThrow(() -> new ResourceNotFoundException("Inward not found with id: " + inwardId));

        // Check if already confirmed
        if (!inward.isDraft()) {
            throw new BusinessException("Inward is already confirmed.");
        }

        // Step 1: Process confirmation (stock, ledger, PO status)
        processConfirmation(inward);

        // Step 2: Resolve invoice details from all possible sources
        String invoiceNumber = resolveInvoiceNumber(inward, request);
        LocalDate invoiceDate = resolveInvoiceDate(inward, request);

        // Step 3: Create purchase invoice ONLY if details available
        if (invoiceNumber != null && !invoiceNumber.isBlank()) {
            BigDecimal invoiceAmount = request != null ? request.vendorInvoiceAmount() : null;
            String remarks = request != null ? request.remarks() : null;
            createPurchaseInvoiceFromInward(inward, invoiceNumber, invoiceDate, invoiceAmount, remarks);
        }

        return inwardMapper.toResponse(materialInwardRepository.save(inward));
    }

    private void processConfirmation(MaterialInward inward) {
        inward.calculateTotals();
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
            if (inward.getVendor() != null) {
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
        }

        // Create Ledger Entry
        if (inward.getVendor() != null) {
            vendorLedgerService.recordInwardEntry(inward);
        }

        // Update PO Status
        if (inward.getPurchaseOrder() != null) {
            purchaseOrderService.updateStatusAfterInward(inward.getPurchaseOrder());
        }
    }

    private String resolveInvoiceNumber(MaterialInward inward, ConfirmInwardRequest request) {
        if (request != null && request.hasInvoiceDetails()) {
            return request.vendorInvoiceNumber().trim();
        }
        if (inward.getVendorInvoiceNumber() != null && !inward.getVendorInvoiceNumber().isBlank()) {
            return inward.getVendorInvoiceNumber().trim();
        }
        return null;
    }

    private LocalDate resolveInvoiceDate(MaterialInward inward, ConfirmInwardRequest request) {
        if (request != null && request.vendorInvoiceDate() != null) {
            return request.vendorInvoiceDate();
        }
        if (inward.getVendorInvoiceDate() != null) {
            return inward.getVendorInvoiceDate();
        }
        return inward.getInwardDate();
    }

    /**
     * Creates a PurchaseInvoice record when inward is confirmed.
     * <p>
     * This is called ONLY from confirm() method, never from draft stages.
     * Each confirmed inward gets its own PurchaseInvoice — no updates.
     * <p>
     * For partial deliveries:
     * - PO: 100kg
     * - Inward 1 (60kg) → Invoice #1 (ABC/INV/001)
     * - Inward 2 (40kg) → Invoice #2 (ABC/INV/002)
     * <p>
     * Both invoices link to the same PO but different inwards.
     */
    private void createPurchaseInvoiceFromInward(
            MaterialInward inward,
            String invoiceNumber,
            LocalDate invoiceDate,
            BigDecimal invoiceAmount,
            String remarks) {

        // Duplicate check: same vendor cannot have same invoice number
        if (purchaseInvoiceRepository.existsByVendorIdAndVendorInvoiceNumber(
                inward.getVendor().getId(), invoiceNumber)) {
            throw new BusinessException(
                    "Invoice number '" + invoiceNumber + "' already exists for vendor '"
                            + inward.getVendor().getName()
                            + "'. Please use a different invoice number or add it manually via Purchase Invoice API.");
        }

        // Use provided amount or calculate from inward total
        BigDecimal finalAmount = invoiceAmount != null ? invoiceAmount : inward.getTotalAmount();

        PurchaseInvoice purchaseInvoice = PurchaseInvoice.builder()
                .vendorInvoiceNumber(invoiceNumber)
                .vendorInvoiceDate(invoiceDate)
                .invoiceAmount(finalAmount)
                .vendor(inward.getVendor())
                .purchaseOrder(inward.getPurchaseOrder())
                .materialInward(inward)
                .source("AUTO")
                .remarks(remarks)
                .createdByUserId(SecurityUtils.getCurrentUserId())
                .build();

        purchaseInvoiceRepository.save(purchaseInvoice);
    }

    @Transactional(readOnly = true)
    public InwardResponse getById(Long id) {
        MaterialInward inward = materialInwardRepository.findWithFullDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inward not found with id: " + id));
        return inwardMapper.toResponse(inward);
    }

    @Transactional(readOnly = true)
    public PageResponse<InwardSummary> getAll(InwardStatus status, Long vendorId, LocalDate from, LocalDate to, Pageable pageable) {
        Page<MaterialInward> page = materialInwardRepository.findAllFiltered(status, vendorId, from, to, pageable);
        return PageResponse.from(page, inwardMapper::toSummary);
    }

    @Transactional
    public InwardResponse createInternalReturnInward(InternalReturnRequest request) {
        com.kalibyte.foundry.inventory.vendor.entity.Vendor internalVendor = vendorRepository.findByName("INTERNAL")
                .orElseGet(() -> {
                    com.kalibyte.foundry.inventory.vendor.entity.Vendor v = new com.kalibyte.foundry.inventory.vendor.entity.Vendor();
                    v.setName("INTERNAL");
                    return vendorRepository.save(v);
                });

        MaterialInward inward = MaterialInward.builder()
                .inwardNumber(inwardNumberGenerator.generate())
                .inwardType("INTERNAL_RETURN")
                .vendor(internalVendor)
                .scrapEntryId(request.getScrapEntryId())
                .vehicleNumber(request.getVehicleNumber())
                .driverName(request.getDriverName())
                .inwardDate(request.getReturnDate() != null ? request.getReturnDate() : LocalDate.now())
                .status(InwardStatus.DRAFT)
                .notes(request.getRemarks())
                .createdByUserId(com.kalibyte.foundry.common.util.SecurityUtils.getCurrentUserId())
                .build();

        // I'll add the received items
        if (request.getItems() != null) {
            for (InternalReturnRequest.InternalReturnItemRequest itemReq : request.getItems()) {
                Item item = itemRepository.findById(itemReq.itemId())
                        .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + itemReq.itemId()));

                BigDecimal unitRate = itemReq.unitRate() != null ? itemReq.unitRate() : item.getAvgRate();
                BigDecimal taxableAmount = itemReq.quantity().multiply(unitRate);

                ReceivedItem receivedItem = ReceivedItem.builder()
                        .item(item)
                        .receivedQuantity(itemReq.quantity())
                        .unitRate(unitRate)
                        .gstRate(BigDecimal.ZERO)
                        .taxAmount(BigDecimal.ZERO)
                        .amount(taxableAmount)
                        .build();
                inward.addReceivedItem(receivedItem);
            }
        }
        inward.calculateTotals();

        processConfirmation(inward);

        return inwardMapper.toResponse(materialInwardRepository.save(inward));
    }
}
