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
import com.kalibyte.foundry.inventory.item.repository.ItemRepository;
import com.kalibyte.foundry.inventory.ledger.entity.VendorLedger;
import com.kalibyte.foundry.inventory.ledger.entity.enums.LedgerEntryType;
import com.kalibyte.foundry.inventory.ledger.repository.VendorLedgerRepository;
import com.kalibyte.foundry.inventory.purchaseorder.entity.PurchaseOrder;
import com.kalibyte.foundry.inventory.purchaseorder.repository.PurchaseOrderRepository;
import com.kalibyte.foundry.inventory.report.dto.*;
import com.kalibyte.foundry.inventory.vendor.entity.Vendor;
import com.kalibyte.foundry.inventory.vendor.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
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

	public InventoryReportService(MaterialInwardRepository inwardRepository, MaterialIssueRepository issueRepository, ItemRepository itemRepository, VendorRepository vendorRepository, PurchaseOrderRepository poRepository, VendorLedgerRepository ledgerRepository) {
		this.inwardRepository = inwardRepository;
		this.issueRepository = issueRepository;
		this.itemRepository = itemRepository;
		this.vendorRepository = vendorRepository;
		this.poRepository = poRepository;
		this.ledgerRepository = ledgerRepository;
	}

	@Transactional(readOnly = true)
    public InwardReportResponse getInwardReport(LocalDate start, LocalDate end, Long vendorId, Long itemId, Long poId) {
        List<MaterialInward> inwards = inwardRepository.findAllFiltered(InwardStatus.CONFIRMED, vendorId, start, end, org.springframework.data.domain.Pageable.unpaged()).getContent();

        // Further filter by itemId or poId if provided (though poId is in the repository filter already, itemId isn't)
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
            BigDecimal docValue = BigDecimal.ZERO;
            List<InwardReportResponse.InwardItemDetail> itemDetails = new ArrayList<>();
            
            for (ReceivedItem ri : in.getReceivedItems()) {
                if (itemId != null && !ri.getItem().getId().equals(itemId)) continue;
                
                BigDecimal val = ri.getReceivedQuantity().multiply(ri.getUnitRate());
                docValue = docValue.add(val);
                totalQty = totalQty.add(ri.getReceivedQuantity());
                
                itemDetails.add(new InwardReportResponse.InwardItemDetail(
                        ri.getItem().getCode(),
                        ri.getItem().getName(),
                        ri.getReceivedQuantity(),
                        ri.getItem().getUnit().name(),
                        ri.getUnitRate(),
                        val,
                        null // ReceiptStatus might not be in the entity yet if it was SHORT/EXCESS logic
                ));
            }
            totalValue = totalValue.add(docValue);
            records.add(new InwardReportResponse.InwardDocumentDetail(
                    in.getInwardNumber(),
                    in.getInwardDate(),
                    in.getVendor().getName(),
                    in.getPurchaseOrder() != null ? in.getPurchaseOrder().getPoNumber() : "DIRECT",
                    docValue,
                    itemDetails
            ));
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
            BigDecimal docValue = BigDecimal.ZERO;
            List<IssueReportResponse.IssueItemDetail> itemDetails = new ArrayList<>();

            for (IssuedItem ii : is.getIssuedItems()) {
                if (itemId != null && !ii.getItem().getId().equals(itemId)) continue;

                BigDecimal val = ii.getIssuedQuantity().multiply(ii.getUnitRate());
                docValue = docValue.add(val);
                totalQty = totalQty.add(ii.getIssuedQuantity());

                itemDetails.add(new IssueReportResponse.IssueItemDetail(
                        ii.getItem().getCode(),
                        ii.getItem().getName(),
                        ii.getIssuedQuantity(),
                        ii.getItem().getUnit().name(),
                        ii.getUnitRate(),
                        val
                ));
            }
            totalValue = totalValue.add(docValue);
            records.add(new IssueReportResponse.IssueDocumentDetail(
                    is.getIssueNumber(),
                    is.getIssueDate(),
                    is.getDepartment().getName(),
                    docValue,
                    itemDetails
            ));
        }

        return new IssueReportResponse(totalQty, totalValue, records.size(), records);
    }

    @Transactional(readOnly = true)
    public ItemLedgerReport getItemLedgerReport(Long itemId, LocalDate start, LocalDate end) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        // Get all historical transactions to calculate opening stock
        List<MaterialInward> allInwards = inwardRepository.findAll(); // Optimization needed for large data
        List<MaterialIssue> allIssues = issueRepository.findAll();

        BigDecimal openingStock = BigDecimal.ZERO;
        for (MaterialInward in : allInwards) {
            if (in.getStatus() == InwardStatus.CONFIRMED && in.getInwardDate().isBefore(start)) {
                for (ReceivedItem ri : in.getReceivedItems()) {
                    if (ri.getItem().getId().equals(itemId)) {
                        openingStock = openingStock.add(ri.getReceivedQuantity());
                    }
                }
            }
        }
        for (MaterialIssue is : allIssues) {
            if (is.getIssueDate().isBefore(start)) {
                for (IssuedItem ii : is.getIssuedItems()) {
                    if (ii.getItem().getId().equals(itemId)) {
                        openingStock = openingStock.subtract(ii.getIssuedQuantity());
                    }
                }
            }
        }

        List<ItemLedgerReport.ItemLedgerTransaction> transactions = new ArrayList<>();
        BigDecimal currentBalance = openingStock;
        BigDecimal totalInQty = BigDecimal.ZERO;
        BigDecimal totalInVal = BigDecimal.ZERO;
        BigDecimal totalOutQty = BigDecimal.ZERO;
        BigDecimal totalOutVal = BigDecimal.ZERO;

        // Collect transactions in range
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

        transactions.sort(Comparator.comparing(ItemLedgerReport.ItemLedgerTransaction::date)
                .thenComparing(t -> t.type().equals("INWARD") ? 0 : 1));

        List<ItemLedgerReport.ItemLedgerTransaction> finalTransactions = new ArrayList<>();
        BigDecimal balance = openingStock;
        for (ItemLedgerReport.ItemLedgerTransaction t : transactions) {
            balance = balance.add(t.quantityIn()).subtract(t.quantityOut());
            finalTransactions.add(new ItemLedgerReport.ItemLedgerTransaction(
                    t.date(), t.type(), t.documentNumber(), t.reference(),
                    t.quantityIn(), t.quantityOut(), t.rate(), balance
            ));
        }

        return new ItemLedgerReport(
                item.getCode(), item.getName(), item.getCategory(), item.getUnit().name(),
                item.getCurrentStock(), item.getAvgRate(), openingStock, balance,
                totalInQty, totalInVal, totalOutQty, totalOutVal,
                totalInQty.subtract(totalOutQty), finalTransactions
        );
    }

    @Transactional(readOnly = true)
    public DailyMovementReport getDailyMovementReport(LocalDate date, String category) {

        List<DailyMovementReport.DailyMovementItem> records = new ArrayList<>();

        
        // Re-implementing more efficiently
        List<MaterialInward> inwards = inwardRepository.findAllFiltered(InwardStatus.CONFIRMED, null, date, date, org.springframework.data.domain.Pageable.unpaged()).getContent();
        List<MaterialIssue> issues = issueRepository.findAllFiltered(null, date, date, org.springframework.data.domain.Pageable.unpaged()).getContent();

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

        for (Long itemId : movedItemIds) {
            Item item = itemRepository.findById(itemId).get();
            if (category != null && !item.getCategory().name().equalsIgnoreCase(category)) continue;

            // Calculate opening stock for this item at 'date'
            // Opening = Current - (Inwards since date) + (Issues since date)
            // But 'date' is today or in past. 
            // Better: Sum all movements before 'date'
            BigDecimal opening = calculateOpeningStock(itemId, date);

            BigDecimal iQ = inQtys.getOrDefault(itemId, BigDecimal.ZERO);
            BigDecimal iV = inVals.getOrDefault(itemId, BigDecimal.ZERO);
            BigDecimal oQ = outQtys.getOrDefault(itemId, BigDecimal.ZERO);
            BigDecimal oV = outVals.getOrDefault(itemId, BigDecimal.ZERO);

            records.add(new DailyMovementReport.DailyMovementItem(
                    item.getCode(), item.getName(), item.getCategory(),
                    opening, iQ, iV, oQ, oV,
                    opening.add(iQ).subtract(oQ),
                    iQ.subtract(oQ)
            ));
        }

        return new DailyMovementReport(date, records);
    }

    private BigDecimal calculateOpeningStock(Long itemId, LocalDate date) {
        // This should be a single optimized query in production
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
        return totalIn.subtract(totalOut);
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

            // Last inward date and last issue date
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

            records.add(new StockSummaryReport.StockSummaryItem(
                    item.getId(), item.getCode(), item.getName(), item.getCategory(), item.getSubCategory(),
                    item.getUnit().name(), item.getCurrentStock(), item.getAvgRate(), item.getStockValue(),
                    item.getReorderLevel(), item.getMinStockLevel(), item.getStockStatus(),
                    item.getLastPurchaseRate(), lastIn, lastOut
            ));
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

        // Optimization: Fetch all needed data once and group by vendor
        List<PurchaseOrder> allPos = poRepository.findAll().stream()
                .filter(po -> (vendorId == null || po.getVendor().getId().equals(vendorId)) &&
                        !po.getPoDate().isBefore(start) && !po.getPoDate().isAfter(end))
                .toList();
        Map<Long, List<PurchaseOrder>> posByVendor = allPos.stream()
                .collect(Collectors.groupingBy(po -> po.getVendor().getId()));

        List<MaterialInward> allInwards = inwardRepository.findAll().stream()
                .filter(in -> (vendorId == null || in.getVendor().getId().equals(vendorId)) &&
                        in.getStatus() == InwardStatus.CONFIRMED &&
                        !in.getInwardDate().isBefore(start) && !in.getInwardDate().isAfter(end))
                .toList();
        Map<Long, List<MaterialInward>> inwardsByVendor = allInwards.stream()
                .collect(Collectors.groupingBy(in -> in.getVendor().getId()));

        List<VendorLedger> allLedger = ledgerRepository.findAll().stream()
                .filter(l -> (vendorId == null || l.getVendor().getId().equals(vendorId)))
                .toList();
        Map<Long, List<VendorLedger>> ledgerByVendor = allLedger.stream()
                .collect(Collectors.groupingBy(l -> l.getVendor().getId()));

        List<VendorSummaryReport.VendorSummaryDetail> details = new ArrayList<>();

        for (Vendor v : vendors) {
            List<PurchaseOrder> pos = posByVendor.getOrDefault(v.getId(), Collections.emptyList());
            List<MaterialInward> inwards = inwardsByVendor.getOrDefault(v.getId(), Collections.emptyList());
            List<VendorLedger> ledger = ledgerByVendor.getOrDefault(v.getId(), Collections.emptyList());

            BigDecimal totalPOVal = pos.stream()
                    .flatMap(po -> po.getOrderItems().stream())
                    .map(oi -> oi.getOrderedQuantity().multiply(oi.getUnitRate()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalInVal = inwards.stream()
                    .flatMap(in -> in.getReceivedItems().stream())
                    .map(ri -> ri.getReceivedQuantity().multiply(ri.getUnitRate()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal pendingVal = pos.stream()
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

            details.add(new VendorSummaryReport.VendorSummaryDetail(
                    v.getId(), v.getName(), v.getPhone(),
                    pos.size(), totalPOVal, inwards.size(), totalInVal, pendingVal, balance, items
            ));
        }

        return new VendorSummaryReport(details);
    }
}
