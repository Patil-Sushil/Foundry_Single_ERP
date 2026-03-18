package com.kalibyte.foundry.inventory.report.service;

import com.kalibyte.foundry.common.exception.ResourceNotFoundException;
import com.kalibyte.foundry.inventory.inward.entity.MaterialInward;
import com.kalibyte.foundry.inventory.inward.entity.ReceivedItem;
import com.kalibyte.foundry.inventory.inward.entity.enums.InwardStatus;
import com.kalibyte.foundry.inventory.inward.repository.MaterialInwardRepository;
import com.kalibyte.foundry.inventory.issue.entity.IssuedItem;
import com.kalibyte.foundry.inventory.issue.entity.MaterialIssue;
import com.kalibyte.foundry.inventory.issue.repository.MaterialIssueRepository;
import com.kalibyte.foundry.inventory.item.entity.Item;
import com.kalibyte.foundry.inventory.item.entity.AdjustmentItem;
import com.kalibyte.foundry.inventory.item.entity.StockAdjustment;
import com.kalibyte.foundry.inventory.item.repository.ItemRepository;
import com.kalibyte.foundry.inventory.item.repository.StockAdjustmentRepository;
import com.kalibyte.foundry.inventory.ledger.entity.VendorLedger;
import com.kalibyte.foundry.inventory.ledger.entity.enums.LedgerEntryType;
import com.kalibyte.foundry.inventory.ledger.repository.VendorLedgerRepository;
import com.kalibyte.foundry.inventory.purchaseorder.entity.PurchaseOrder;
import com.kalibyte.foundry.inventory.purchaseorder.entity.enums.POStatus;
import com.kalibyte.foundry.inventory.purchaseorder.repository.PurchaseOrderRepository;
import com.kalibyte.foundry.inventory.report.dto.*;
import com.kalibyte.foundry.inventory.report.mapper.InventoryReportMapper;
import com.kalibyte.foundry.inventory.vendor.entity.Vendor;
import com.kalibyte.foundry.inventory.vendor.repository.VendorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InventoryReportService {

    private final MaterialInwardRepository inwardRepository;
    private final MaterialIssueRepository issueRepository;
    private final ItemRepository itemRepository;
    private final VendorRepository vendorRepository;
    private final PurchaseOrderRepository poRepository;
    private final VendorLedgerRepository ledgerRepository;
    private final StockAdjustmentRepository stockAdjustmentRepository;
    private final InventoryReportMapper inventoryReportMapper;

	public InventoryReportService(MaterialInwardRepository inwardRepository, 
                                 MaterialIssueRepository issueRepository, 
                                 ItemRepository itemRepository, 
                                 VendorRepository vendorRepository, 
                                 PurchaseOrderRepository poRepository, 
                                 VendorLedgerRepository ledgerRepository, 
                                 StockAdjustmentRepository stockAdjustmentRepository,
                                 InventoryReportMapper inventoryReportMapper) {
		this.inwardRepository = inwardRepository;
		this.issueRepository = issueRepository;
		this.itemRepository = itemRepository;
		this.vendorRepository = vendorRepository;
		this.poRepository = poRepository;
		this.ledgerRepository = ledgerRepository;
        this.stockAdjustmentRepository = stockAdjustmentRepository;
        this.inventoryReportMapper = inventoryReportMapper;
	}

	@Transactional(readOnly = true)
    public InwardReportResponse getInwardReport(LocalDate start, LocalDate end, Long vendorId, Long itemId, Long poId) {
        List<MaterialInward> inwards = inwardRepository.findAllFiltered(InwardStatus.CONFIRMED, vendorId, start, end, org.springframework.data.domain.Pageable.unpaged()).getContent();

        if (itemId != null) {
            inwards = inwards.stream()
                    .filter(in -> in.getReceivedItems().stream().anyMatch(ri -> ri.getItem().getId().equals(itemId)))
                    .collect(Collectors.toList());
        }
        if (poId != null) {
            inwards = inwards.stream()
                    .filter(in -> in.getPurchaseOrder() != null && in.getPurchaseOrder().getId().equals(poId))
                    .collect(Collectors.toList());
        }

        BigDecimal totalQty = BigDecimal.ZERO;
        BigDecimal totalValue = BigDecimal.ZERO;
        List<InwardReportResponse.InwardDocumentDetail> records = new ArrayList<>();

        for (MaterialInward in : inwards) {
            List<ReceivedItem> filteredItems = in.getReceivedItems().stream()
                    .filter(ri -> itemId == null || ri.getItem().getId().equals(itemId))
                    .toList();

            List<InwardReportResponse.InwardItemDetail> itemDetails = filteredItems.stream()
                    .map(inventoryReportMapper::toInwardItemDetail)
                    .toList();

            BigDecimal docValue = itemDetails.stream()
                    .map(InwardReportResponse.InwardItemDetail::totalValue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal docQty = itemDetails.stream()
                    .map(InwardReportResponse.InwardItemDetail::quantity)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            totalQty = totalQty.add(docQty);
            totalValue = totalValue.add(docValue);

            records.add(inventoryReportMapper.toInwardDocumentDetail(in, docValue, itemDetails));
        }

        return new InwardReportResponse(totalQty, totalValue, records.size(), records);
    }

    @Transactional(readOnly = true)
    public IssueReportResponse getIssueReport(LocalDate start, LocalDate end, Long departmentId, Long itemId) {
        List<MaterialIssue> issues = issueRepository.findAllFiltered(departmentId, start, end, org.springframework.data.domain.Pageable.unpaged()).getContent();

        if (itemId != null) {
            issues = issues.stream()
                    .filter(is -> is.getIssuedItems().stream().anyMatch(ii -> ii.getItem().getId().equals(itemId)))
                    .toList();
        }

        BigDecimal totalQty = BigDecimal.ZERO;
        BigDecimal totalValue = BigDecimal.ZERO;
        List<IssueReportResponse.IssueDocumentDetail> records = new ArrayList<>();

        for (MaterialIssue is : issues) {
            List<IssuedItem> filteredItems = is.getIssuedItems().stream()
                    .filter(ii -> itemId == null || ii.getItem().getId().equals(itemId))
                    .toList();

            List<IssueReportResponse.IssueItemDetail> itemDetails = filteredItems.stream()
                    .map(inventoryReportMapper::toIssueItemDetail)
                    .toList();

            BigDecimal docValue = itemDetails.stream()
                    .map(IssueReportResponse.IssueItemDetail::totalValue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal docQty = itemDetails.stream()
                    .map(IssueReportResponse.IssueItemDetail::quantity)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            totalQty = totalQty.add(docQty);
            totalValue = totalValue.add(docValue);

            records.add(inventoryReportMapper.toIssueDocumentDetail(is, docValue, itemDetails));
        }

        return new IssueReportResponse(totalQty, totalValue, records.size(), records);
    }

    @Transactional(readOnly = true)
    public ItemLedgerReport getItemLedgerReport(Long itemId, LocalDate start, LocalDate end) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        BigDecimal openingStock = calculateOpeningStock(itemId, start);

        List<MaterialInward> allInwards = inwardRepository.findAll();
        List<MaterialIssue> allIssues = issueRepository.findAll();
        List<StockAdjustment> allAdjustments = stockAdjustmentRepository.findAll();

        List<ItemLedgerReport.ItemLedgerTransaction> transactions = new ArrayList<>();
        BigDecimal totalInQty = BigDecimal.ZERO;
        BigDecimal totalInVal = BigDecimal.ZERO;
        BigDecimal totalOutQty = BigDecimal.ZERO;
        BigDecimal totalOutVal = BigDecimal.ZERO;

        for (MaterialInward in : allInwards) {
            if (in.getStatus() == InwardStatus.CONFIRMED && !in.getInwardDate().isBefore(start) && !in.getInwardDate().isAfter(end)) {
                for (ReceivedItem ri : in.getReceivedItems()) {
                    if (ri.getItem().getId().equals(itemId)) {
                        totalInQty = totalInQty.add(ri.getReceivedQuantity());
                        totalInVal = totalInVal.add(ri.getReceivedQuantity().multiply(ri.getUnitRate()));
                        transactions.add(new ItemLedgerReport.ItemLedgerTransaction(
                                in.getInwardDate(), "INWARD", in.getInwardNumber(), in.getVendor().getName(),
                                ri.getReceivedQuantity(), BigDecimal.ZERO, ri.getUnitRate(), BigDecimal.ZERO
                        ));
                    }
                }
            }
        }
        for (MaterialIssue is : allIssues) {
            if (!is.getIssueDate().isBefore(start) && !is.getIssueDate().isAfter(end)) {
                for (IssuedItem ii : is.getIssuedItems()) {
                    if (ii.getItem().getId().equals(itemId)) {
                        totalOutQty = totalOutQty.add(ii.getIssuedQuantity());
                        totalOutVal = totalOutVal.add(ii.getIssuedQuantity().multiply(ii.getUnitRate()));
                        transactions.add(new ItemLedgerReport.ItemLedgerTransaction(
                                is.getIssueDate(), "ISSUE", is.getIssueNumber(), is.getDepartment().getName(),
                                BigDecimal.ZERO, ii.getIssuedQuantity(), ii.getUnitRate(), BigDecimal.ZERO
                        ));
                    }
                }
            }
        }
        for (StockAdjustment adj : allAdjustments) {
            if (!adj.getAdjustmentDate().isBefore(start) && !adj.getAdjustmentDate().isAfter(end)) {
                for (AdjustmentItem ai : adj.getItems()) {
                    if (ai.getItem().getId().equals(itemId)) {
                        BigDecimal qty = ai.getAdjustedQuantity();
                        BigDecimal qtyIn = qty.compareTo(BigDecimal.ZERO) > 0 ? qty : BigDecimal.ZERO;
                        BigDecimal qtyOut = qty.compareTo(BigDecimal.ZERO) < 0 ? qty.abs() : BigDecimal.ZERO;
                        
                        totalInQty = totalInQty.add(qtyIn);
                        totalOutQty = totalOutQty.add(qtyOut);
                        if (ai.getUnitRate() != null) {
                            totalInVal = totalInVal.add(qtyIn.multiply(ai.getUnitRate()));
                            totalOutVal = totalOutVal.add(qtyOut.multiply(ai.getUnitRate()));
                        }

                        transactions.add(new ItemLedgerReport.ItemLedgerTransaction(
                                adj.getAdjustmentDate(), "ADJUSTMENT", adj.getAdjustmentNumber(), adj.getReason(),
                                qtyIn, qtyOut, ai.getUnitRate() != null ? ai.getUnitRate() : BigDecimal.ZERO, BigDecimal.ZERO
                        ));
                    }
                }
            }
        }

        transactions.sort(Comparator.comparing(ItemLedgerReport.ItemLedgerTransaction::date)
                .thenComparing(t -> {
                    if (t.type().equals("INWARD")) return 0;
                    if (t.type().equals("ADJUSTMENT")) return 1;
                    return 2;
                }));

        List<ItemLedgerReport.ItemLedgerTransaction> finalTransactions = new ArrayList<>();
        BigDecimal balance = openingStock;
        for (ItemLedgerReport.ItemLedgerTransaction t : transactions) {
            balance = balance.add(t.quantityIn()).subtract(t.quantityOut());
            finalTransactions.add(new ItemLedgerReport.ItemLedgerTransaction(
                    t.date(), t.type(), t.documentNumber(), t.reference(),
                    t.quantityIn(), t.quantityOut(), t.rate(), balance
            ));
        }

        return inventoryReportMapper.toItemLedgerReport(item, openingStock, balance, totalInQty, totalInVal, totalOutQty, totalOutVal, finalTransactions);
    }

    @Transactional(readOnly = true)
    public DailyMovementReport getDailyMovementReport(LocalDate date, String category) {

        List<DailyMovementReport.DailyMovementItem> records = new ArrayList<>();
        
        List<MaterialInward> inwards = inwardRepository.findAllFiltered(InwardStatus.CONFIRMED, null, date, date, org.springframework.data.domain.Pageable.unpaged()).getContent();
        List<MaterialIssue> issues = issueRepository.findAllFiltered(null, date, date, org.springframework.data.domain.Pageable.unpaged()).getContent();
        List<StockAdjustment> adjustments = stockAdjustmentRepository.findAll().stream()
                .filter(adj -> adj.getAdjustmentDate().isEqual(date))
                .toList();

        Map<Long, BigDecimal> inQtys = new HashMap<>();
        Map<Long, BigDecimal> inVals = new HashMap<>();
        Map<Long, BigDecimal> outQtys = new HashMap<>();
        Map<Long, BigDecimal> outVals = new HashMap<>();
        Set<Long> movedItemIds = new HashSet<>();

        for (MaterialInward in : inwards) {
            for (ReceivedItem ri : in.getReceivedItems()) {
                Long id = ri.getItem().getId();
                movedItemIds.add(id);
                inQtys.put(id, inQtys.getOrDefault(id, BigDecimal.ZERO).add(ri.getReceivedQuantity()));
                inVals.put(id, inVals.getOrDefault(id, BigDecimal.ZERO).add(ri.getReceivedQuantity().multiply(ri.getUnitRate())));
            }
        }
        for (MaterialIssue is : issues) {
            for (IssuedItem ii : is.getIssuedItems()) {
                Long id = ii.getItem().getId();
                movedItemIds.add(id);
                outQtys.put(id, outQtys.getOrDefault(id, BigDecimal.ZERO).add(ii.getIssuedQuantity()));
                outVals.put(id, outVals.getOrDefault(id, BigDecimal.ZERO).add(ii.getIssuedQuantity().multiply(ii.getUnitRate())));
            }
        }
        for (StockAdjustment adj : adjustments) {
            for (AdjustmentItem ai : adj.getItems()) {
                Long id = ai.getItem().getId();
                movedItemIds.add(id);
                BigDecimal qty = ai.getAdjustedQuantity();
                BigDecimal rate = ai.getUnitRate() != null ? ai.getUnitRate() : BigDecimal.ZERO;
                
                if (qty.compareTo(BigDecimal.ZERO) > 0) {
                    inQtys.put(id, inQtys.getOrDefault(id, BigDecimal.ZERO).add(qty));
                    inVals.put(id, inVals.getOrDefault(id, BigDecimal.ZERO).add(qty.multiply(rate)));
                } else if (qty.compareTo(BigDecimal.ZERO) < 0) {
                    BigDecimal absQty = qty.abs();
                    outQtys.put(id, outQtys.getOrDefault(id, BigDecimal.ZERO).add(absQty));
                    outVals.put(id, outVals.getOrDefault(id, BigDecimal.ZERO).add(absQty.multiply(rate)));
                }
            }
        }

        for (Long itemId : movedItemIds) {
            Item item = itemRepository.findById(itemId).get();
            if (category != null && !item.getCategory().name().equalsIgnoreCase(category)) continue;

            BigDecimal opening = calculateOpeningStock(itemId, date);
            BigDecimal iQ = inQtys.getOrDefault(itemId, BigDecimal.ZERO);
            BigDecimal iV = inVals.getOrDefault(itemId, BigDecimal.ZERO);
            BigDecimal oQ = outQtys.getOrDefault(itemId, BigDecimal.ZERO);
            BigDecimal oV = outVals.getOrDefault(itemId, BigDecimal.ZERO);

            records.add(inventoryReportMapper.toDailyMovementItem(item, opening, iQ, iV, oQ, oV));
        }

        return new DailyMovementReport(date, records);
    }

    private BigDecimal calculateOpeningStock(Long itemId, LocalDate date) {
        BigDecimal totalIn = inwardRepository.findAll().stream()
                .filter(in -> in.getStatus() == InwardStatus.CONFIRMED && in.getInwardDate().isBefore(date))
                .flatMap(in -> in.getReceivedItems().stream())
                .filter(ri -> ri.getItem().getId().equals(itemId))
                .map(ReceivedItem::getReceivedQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalOut = issueRepository.findAll().stream()
                .filter(is -> is.getIssueDate().isBefore(date))
                .flatMap(is -> is.getIssuedItems().stream())
                .filter(ii -> ii.getItem().getId().equals(itemId))
                .map(IssuedItem::getIssuedQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalAdjustment = stockAdjustmentRepository.findAll().stream()
                .filter(adj -> adj.getAdjustmentDate().isBefore(date))
                .flatMap(adj -> adj.getItems().stream())
                .filter(ai -> ai.getItem().getId().equals(itemId))
                .map(AdjustmentItem::getAdjustedQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalIn.subtract(totalOut).add(totalAdjustment);
    }

    @Transactional(readOnly = true)
    public StockSummaryReport getStockSummaryReport(String category, Boolean belowReorder, Long departmentId) {
        List<Item> items = itemRepository.findAll();

        if (category != null) {
            items = items.stream().filter(i -> i.getCategory().name().equalsIgnoreCase(category)).collect(Collectors.toList());
        }
        if (belowReorder != null && belowReorder) {
            items = items.stream().filter(i -> i.getCurrentStock().compareTo(i.getReorderLevel()) <= 0).collect(Collectors.toList());
        }
        if (departmentId != null) {
            items = items.stream().filter(i -> i.getDepartment() != null && i.getDepartment().getId().equals(departmentId)).collect(Collectors.toList());
        }

        BigDecimal totalStockValue = BigDecimal.ZERO;
        long lowCount = 0;
        long criticalCount = 0;
        List<StockSummaryReport.StockSummaryItem> records = new ArrayList<>();

        for (Item item : items) {
            totalStockValue = totalStockValue.add(item.getStockValue());
            if (item.getCurrentStock().compareTo(item.getMinStockLevel()) <= 0) criticalCount++;
            else if (item.getCurrentStock().compareTo(item.getReorderLevel()) <= 0) lowCount++;

            LocalDate lastIn = inwardRepository.findAll().stream()
                    .filter(in -> in.getStatus() == InwardStatus.CONFIRMED && in.getReceivedItems().stream().anyMatch(ri -> ri.getItem().getId().equals(item.getId())))
                    .map(MaterialInward::getInwardDate)
                    .max(LocalDate::compareTo)
                    .orElse(null);

            LocalDate lastOut = issueRepository.findAll().stream()
                    .filter(is -> is.getIssuedItems().stream().anyMatch(ii -> ii.getItem().getId().equals(item.getId())))
                    .map(MaterialIssue::getIssueDate)
                    .max(LocalDate::compareTo)
                    .orElse(null);

            records.add(inventoryReportMapper.toStockSummaryItem(item, lastIn, lastOut));
        }

        return new StockSummaryReport(totalStockValue, lowCount, criticalCount, records);
    }

    @Transactional(readOnly = true)
    public VendorSummaryReport getVendorSummaryReport(LocalDate start, LocalDate end, Long vendorId) {
        List<Vendor> vendors;
        if (vendorId != null) {
            vendors = List.of(vendorRepository.findById(vendorId).orElseThrow(() -> new ResourceNotFoundException("Vendor not found")));
        } else {
            vendors = vendorRepository.findAll();
        }

        // Fetch ALL POs for the relevant vendors once to avoid N+1 and fix pending value logic
        List<PurchaseOrder> allVendorPos = poRepository.findAll().stream()
                .filter(po -> vendorId == null || po.getVendor().getId().equals(vendorId))
                .toList();
        Map<Long, List<PurchaseOrder>> posByVendor = allVendorPos.stream()
                .collect(Collectors.groupingBy(po -> po.getVendor().getId()));

        // Fetch Inwards in period
        List<MaterialInward> inwardsInPeriod = inwardRepository.findAll().stream()
                .filter(in -> (vendorId == null || in.getVendor().getId().equals(vendorId)) &&
                        in.getStatus() == InwardStatus.CONFIRMED &&
                        !in.getInwardDate().isBefore(start) && !in.getInwardDate().isAfter(end))
                .toList();
        Map<Long, List<MaterialInward>> inwardsByVendor = inwardsInPeriod.stream()
                .collect(Collectors.groupingBy(in -> in.getVendor().getId()));

        // Fetch Ledger entries for balance
        List<VendorLedger> allLedger = ledgerRepository.findAll().stream()
                .filter(l -> (vendorId == null || l.getVendor().getId().equals(vendorId)))
                .toList();
        Map<Long, List<VendorLedger>> ledgerByVendor = allLedger.stream()
                .collect(Collectors.groupingBy(l -> l.getVendor().getId()));

        List<VendorSummaryReport.VendorSummaryDetail> details = new ArrayList<>();

        for (Vendor v : vendors) {
            List<PurchaseOrder> allPosForVendor = posByVendor.getOrDefault(v.getId(), Collections.emptyList());
            
            // POs raised WITHIN the period
            List<PurchaseOrder> posInPeriod = allPosForVendor.stream()
                    .filter(po -> !po.getPoDate().isBefore(start) && !po.getPoDate().isAfter(end))
                    .toList();

            List<MaterialInward> inwards = inwardsByVendor.getOrDefault(v.getId(), Collections.emptyList());
            List<VendorLedger> ledger = ledgerByVendor.getOrDefault(v.getId(), Collections.emptyList());

            BigDecimal totalPOVal = posInPeriod.stream()
                    .flatMap(po -> po.getOrderItems().stream())
                    .map(oi -> oi.getOrderedQuantity().multiply(oi.getUnitRate()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalInVal = inwards.stream()
                    .flatMap(in -> in.getReceivedItems().stream())
                    .map(ri -> ri.getReceivedQuantity().multiply(ri.getUnitRate()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Pending value should consider ALL open/partially received POs, even if raised before the period
            BigDecimal pendingVal = allPosForVendor.stream()
                    .filter(po -> po.getStatus() == POStatus.OPEN || po.getStatus() == POStatus.PARTIALLY_RECEIVED)
                    .flatMap(po -> po.getOrderItems().stream())
                    .map(oi -> oi.getOrderedQuantity().subtract(oi.getReceivedQuantity()).multiply(oi.getUnitRate()))
                    .filter(val -> val.compareTo(BigDecimal.ZERO) > 0)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal balance = ledger.stream()
                    .map(l -> l.getEntryType() == LedgerEntryType.CREDIT ? l.getAmount() : l.getAmount().negate())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            List<VendorSummaryReport.SuppliedItemDetail> items = new ArrayList<>();
            Map<Long, List<ReceivedItem>> grouped = inwards.stream()
                    .flatMap(in -> in.getReceivedItems().stream())
                    .collect(Collectors.groupingBy(ri -> ri.getItem().getId()));

            for (var entry : grouped.entrySet()) {
                List<ReceivedItem> itemReceipts = entry.getValue();
                ReceivedItem firstRi = itemReceipts.get(0);
                Item item = firstRi.getItem();
                
                BigDecimal totalQty = itemReceipts.stream()
                        .map(ReceivedItem::getReceivedQuantity)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                BigDecimal totalItemValue = itemReceipts.stream()
                        .map(ri -> ri.getReceivedQuantity().multiply(ri.getUnitRate()))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                BigDecimal avgR = totalQty.compareTo(BigDecimal.ZERO) > 0
                        ? totalItemValue.divide(totalQty, 2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;

                items.add(new VendorSummaryReport.SuppliedItemDetail(item.getCode(), item.getName(), totalQty, avgR));
            }

            details.add(inventoryReportMapper.toVendorSummaryDetail(v, posInPeriod.size(), totalPOVal, inwards.size(), totalInVal, pendingVal, balance, items));
        }

        return new VendorSummaryReport(details);
    }
}
