package com.kalibyte.foundry.inventory.report.mapper;

import com.kalibyte.foundry.inventory.inward.entity.MaterialInward;
import com.kalibyte.foundry.inventory.inward.entity.ReceivedItem;
import com.kalibyte.foundry.inventory.issue.entity.IssuedItem;
import com.kalibyte.foundry.inventory.issue.entity.MaterialIssue;
import com.kalibyte.foundry.inventory.item.entity.Item;
import com.kalibyte.foundry.inventory.item.entity.AdjustmentItem;
import com.kalibyte.foundry.inventory.item.entity.StockAdjustment;
import com.kalibyte.foundry.inventory.vendor.entity.Vendor;
import com.kalibyte.foundry.inventory.report.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * MapStruct mapper for Inventory Report DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface InventoryReportMapper {

    // --- Inward Report Mappings ---

    @Mapping(source = "in.inwardNumber", target = "inwardNumber")
    @Mapping(source = "in.inwardDate", target = "inwardDate")
    @Mapping(source = "in.vendor.name", target = "vendorName")
    @Mapping(target = "poNumber", expression = "java(in.getPurchaseOrder() != null ? in.getPurchaseOrder().getPoNumber() : \"DIRECT\")")
    @Mapping(source = "docValue", target = "totalValue")
    @Mapping(source = "itemDetails", target = "items")
    InwardReportResponse.InwardDocumentDetail toInwardDocumentDetail(MaterialInward in, BigDecimal docValue, List<InwardReportResponse.InwardItemDetail> itemDetails);

    @Mapping(source = "ri.item.code", target = "itemCode")
    @Mapping(source = "ri.item.name", target = "itemName")
    @Mapping(source = "ri.receivedQuantity", target = "quantity")
    @Mapping(target = "unit", expression = "java(ri.getItem().getUnit().name())")
    @Mapping(source = "ri.unitRate", target = "unitRate")
    @Mapping(target = "totalValue", expression = "java(ri.getAmount())")
    @Mapping(target = "status", expression = "java(ri.getReceiptStatus())")
    InwardReportResponse.InwardItemDetail toInwardItemDetail(ReceivedItem ri);

    @Mapping(source = "is.issueNumber", target = "issueNumber")
    @Mapping(source = "is.issueDate", target = "issueDate")
    @Mapping(source = "is.department.name", target = "departmentName")
    @Mapping(source = "docValue", target = "totalValue")
    @Mapping(source = "itemDetails", target = "items")
    IssueReportResponse.IssueDocumentDetail toIssueDocumentDetail(MaterialIssue is, BigDecimal docValue, List<IssueReportResponse.IssueItemDetail> itemDetails);

    @Mapping(source = "v.id", target = "vendorId")
    @Mapping(source = "v.name", target = "vendorName")
    @Mapping(source = "v.phone", target = "contactNumber")
    @Mapping(source = "poCount", target = "totalPOsRaised")
    @Mapping(source = "totalPoVal", target = "totalPOValue")
    @Mapping(source = "inwardCount", target = "totalInwardsReceived")
    @Mapping(source = "totalInVal", target = "totalInwardValue")
    @Mapping(source = "pendingVal", target = "pendingPOValue")
    @Mapping(source = "balance", target = "ledgerBalance")
    @Mapping(source = "items", target = "suppliedItems")
    VendorSummaryReport.VendorSummaryDetail toVendorSummaryDetail(Vendor v, long poCount, BigDecimal totalPoVal, long inwardCount, BigDecimal totalInVal, BigDecimal pendingVal, BigDecimal balance, List<VendorSummaryReport.SuppliedItemDetail> items);

    @Mapping(source = "ii.item.code", target = "itemCode")
    @Mapping(source = "ii.item.name", target = "itemName")
    @Mapping(source = "ii.issuedQuantity", target = "quantity")
    @Mapping(target = "unit", expression = "java(ii.getItem().getUnit().name())")
    @Mapping(target = "totalValue", expression = "java(ii.getAmount())")
    IssueReportResponse.IssueItemDetail toIssueItemDetail(IssuedItem ii);

    // --- Stock Summary Mappings ---

    @Mapping(source = "item.id", target = "itemId")
    @Mapping(source = "item.code", target = "itemCode")
    @Mapping(source = "item.name", target = "itemName")
    @Mapping(target = "unit", expression = "java(item.getUnit().name())")
    @Mapping(target = "stockValue", expression = "java(item.getStockValue())")
    @Mapping(target = "status", expression = "java(item.getStockStatus())")
    @Mapping(source = "lastIn", target = "lastInwardDate")
    @Mapping(source = "lastOut", target = "lastIssueDate")
    StockSummaryReport.StockSummaryItem toStockSummaryItem(Item item, LocalDate lastIn, LocalDate lastOut);

    // --- Daily Movement Mappings ---

    @Mapping(source = "item.code", target = "itemCode")
    @Mapping(source = "item.name", target = "itemName")
    @Mapping(source = "item.category", target = "category")
    @Mapping(source = "openingStock", target = "openingStock")
    @Mapping(source = "inQty", target = "totalInwardQty")
    @Mapping(source = "inVal", target = "totalInwardValue")
    @Mapping(source = "outQty", target = "totalIssueQty")
    @Mapping(source = "outVal", target = "totalIssueValue")
    @Mapping(target = "closingStock", expression = "java(openingStock.add(inQty).subtract(outQty))")
    @Mapping(target = "netMovement", expression = "java(inQty.subtract(outQty))")
    DailyMovementReport.DailyMovementItem toDailyMovementItem(Item item, BigDecimal openingStock, BigDecimal inQty, BigDecimal inVal, BigDecimal outQty, BigDecimal outVal);

    // --- Item Ledger Mappings ---

    @Mapping(source = "item.code", target = "itemCode")
    @Mapping(source = "item.name", target = "itemName")
    @Mapping(source = "item.category", target = "category")
    @Mapping(target = "unit", expression = "java(item.getUnit().name())")
    @Mapping(source = "item.currentStock", target = "currentStock")
    @Mapping(source = "item.avgRate", target = "avgRate")
    @Mapping(source = "openingStock", target = "openingStock")
    @Mapping(source = "closingStock", target = "closingStock")
    @Mapping(source = "totalInwardQty", target = "totalInwardQty")
    @Mapping(source = "totalInwardValue", target = "totalInwardValue")
    @Mapping(source = "totalIssueQty", target = "totalIssueQty")
    @Mapping(source = "totalIssueValue", target = "totalIssueValue")
    @Mapping(target = "netMovement", expression = "java(totalInwardQty.subtract(totalIssueQty))")
    @Mapping(source = "transactions", target = "transactions")
    ItemLedgerReport toItemLedgerReport(Item item, BigDecimal openingStock, BigDecimal closingStock, BigDecimal totalInwardQty, BigDecimal totalInwardValue, BigDecimal totalIssueQty, BigDecimal totalIssueValue, List<ItemLedgerReport.ItemLedgerTransaction> transactions);

    // --- Report Final Mappings ---

    @Mapping(source = "totalQty", target = "totalQuantity")
    @Mapping(source = "totalValue", target = "totalValue")
    @Mapping(source = "documentCount", target = "totalInwardCount")
    @Mapping(source = "documents", target = "records")
    InwardReportResponse toInwardReportResponse(BigDecimal totalQty, BigDecimal totalValue, int documentCount, List<InwardReportResponse.InwardDocumentDetail> documents);

    @Mapping(source = "totalQty", target = "totalQuantity")
    @Mapping(source = "totalValue", target = "totalValue")
    @Mapping(source = "documentCount", target = "totalIssueCount")
    @Mapping(source = "documents", target = "records")
    IssueReportResponse toIssueReportResponse(BigDecimal totalQty, BigDecimal totalValue, int documentCount, List<IssueReportResponse.IssueDocumentDetail> documents);

    @Mapping(source = "date", target = "date")
    @Mapping(source = "items", target = "records")
    DailyMovementReport toDailyMovementReport(LocalDate date, List<DailyMovementReport.DailyMovementItem> items);

    @Mapping(source = "totalValue", target = "totalStockValue")
    @Mapping(source = "lowCount", target = "lowStockCount")
    @Mapping(source = "criticalCount", target = "criticalStockCount")
    @Mapping(source = "records", target = "items")
    StockSummaryReport toStockSummaryReport(BigDecimal totalValue, long lowCount, long criticalCount, List<StockSummaryReport.StockSummaryItem> records);
}
