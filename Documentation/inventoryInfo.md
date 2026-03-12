# Inventory Module Documentation

This document provides a comprehensive overview of the Inventory Management System within the Foundry Spring Backend. It covers the directory structure, entity relationships, core business logic, and data flow.

## 1. Directory Structure

The inventory module is located under `com.kalibyte.foundry.inventory` and is organized into sub-packages based on functional domains:

- `common/`: Shared utilities and base classes.
    - `BaseInventoryEntity`: Mapped superclass with common fields (id, audit timestamps, created/updated by).
    - `NumberGenerators`: Services to generate unique identifiers for POs, Inwards, and Issues.
- `department/`: Internal departments that consume materials.
- `item/`: The core of the inventory system. Defines items, categories, and handles stock calculations.
- `vendor/`: External suppliers.
- `ledger/`: Financial tracking for vendor transactions.
- `purchaseorder/`: Procurement process, tracking what is ordered from vendors.
- `inward/`: Material reception, updating stock and financial ledgers.
- `issue/`: Internal material distribution to departments.
- `report/`: Analytics and business intelligence, generating stock and financial reports.

---

## 2. Core Entities & Relationships

### Item (`item.entity.Item`)
The central entity representing a stockable material.
- **Attributes**: Code, Name, Category, Sub-Category, Unit, Current Stock, Reorder Level, Min Stock Level, Avg Rate, Last Purchase Rate.
- **Mappings**: 
    - `ManyToOne` with `Department` (primary consuming department).
- **Key Logic**: 
    - `receiveStock(qty, rate)`: Increases `currentStock` and recalculates `avgRate` using a Weighted Average Cost (WAC) method. Updates `lastPurchaseRate`.
    - `issueStock(qty)`: Decreases `currentStock`. Throws `BusinessException` if stock is insufficient.

### Purchase Order (`purchaseorder.entity.PurchaseOrder`)
Represents a formal request to a vendor for materials.
- **Status**: `OPEN`, `PARTIALLY_RECEIVED`, `RECEIVED`, `CANCELLED`.
- **Mappings**: 
    - `ManyToOne` with `Vendor`.
    - `OneToMany` with `OrderItem` (Composition).
- **Key Logic**: Tracks the lifecycle of procurement. Status is updated automatically during the inward process.

### Order Item (`purchaseorder.entity.OrderItem`)
Individual lines within a Purchase Order.
- **Attributes**: Ordered Quantity, Received Quantity, Unit Rate.
- **Mappings**: 
    - `ManyToOne` with `Item`.

### Material Inward (`inward.entity.MaterialInward`)
Records the reception of materials.
- **Status**: `DRAFT`, `CONFIRMED`.
- **Mappings**:
    - `ManyToOne` with `PurchaseOrder` (Optional).
    - `ManyToOne` with `Vendor`.
    - `OneToMany` with `ReceivedItem` (Composition).
- **Key Logic**: Confirmation of an inward triggers stock updates and ledger entries.

### Received Item (`inward.entity.ReceivedItem`)
Individual lines within an Inward document.
- **Mappings**:
    - `ManyToOne` with `Item`.
    - `ManyToOne` with `OrderItem` (Links back to PO if applicable).
- **Key Logic**: Calculates `ReceiptStatus` (OK, SHORT, EXCESS) by comparing `receivedQuantity` with `poQuantity`.

### Material Issue (`issue.entity.MaterialIssue`)
Records the consumption of material by an internal department.
- **Mappings**:
    - `ManyToOne` with `Department`.
    - `OneToMany` with `IssuedItem` (Composition).

### Issued Item (`issue.entity.IssuedItem`)
Individual lines within an Issue document.
- **Mappings**:
    - `ManyToOne` with `Item`.
- **Key Logic**: Captures the `avgRate` of the item at the exact moment of issuance to track consumption value accurately.

### Vendor Ledger (`ledger.entity.VendorLedger`)
Tracks financial obligations to vendors.
- **Mappings**:
    - `ManyToOne` with `Vendor`.
    - `ManyToOne` with `MaterialInward`.
- **Key Logic**: Records the total value of confirmed material inwards as a credit/payable to the vendor.

---

## 3. Core Business Workflows

### A. The Procurement & Reception Flow
1. **Create PO**: A `PurchaseOrder` is created with multiple `OrderItems`. Status is `OPEN`.
2. **Start Inward**: A `MaterialInward` is initialized, usually referencing a `PO`. It copies items from the PO into `ReceivedItems` with a `DRAFT` status.
3. **Confirm Inward**: When the physical material is verified:
    - `MaterialInward` status becomes `CONFIRMED`.
    - For each `ReceivedItem`:
        - `Item.receiveStock()` is called: `currentStock` increases, `avgRate` is recalculated.
        - `OrderItem.receivedQuantity` is updated.
        - `ItemVendorRate` (price history) is updated/created.
    - `VendorLedger` records a new entry for the total inward amount.
    - `PurchaseOrder` status is updated to `PARTIALLY_RECEIVED` or `RECEIVED`.

### B. The Internal Consumption Flow
1. **Record Issue**: A `MaterialIssue` is created for a specific `Department`.
2. **Process Items**: For each item requested:
    - `Item.issueStock()` is called: Validates availability and decreases `currentStock`.
    - The current `Item.avgRate` is captured in `IssuedItem.unitRate`.
3. **Reporting**: Consumption reports aggregate `IssuedItem` data to show total value consumed by departments over a date range.

### C. Financial Tracking
- The system doesn't just track quantities; it tracks **Value**.
- **Stock Valuation**: Done using the `avgRate` (Weighted Average Cost).
- **Vendor Balance**: Derived from `VendorLedger` entries. Every confirmed Inward creates a payable.

---

## 4. Technical Implementation Details

- **Concurrency**: Services use `@Transactional` to ensure atomicity. Stock updates and ledger entries happen in the same transaction as document confirmation.
- **Audit**: All entities extending `BaseInventoryEntity` automatically track `createdAt`, `updatedAt`, `createdBy`, and `updatedBy`.
- **Soft Deletes/Activation**: `Item` and `Vendor` have `isActive` flags instead of hard deletes to maintain referential integrity with historical documents.
- **Number Generation**: Custom generators (`PONumberGenerator`, etc.) ensure human-readable unique IDs for documents (e.g., `PO-2024-001`).
- **Data Integrity**: Database-level constraints (foreign keys, unique constraints on codes/numbers) complement Java-side business logic validation.

---

## 5. Reporting & Analytics

The system provides a suite of reports for operational and financial visibility.

### A. Operational Reports
- **Material Inward Report**: Tracks receipts from vendors over a date range. Filterable by Vendor, Item, and PO.
- **Material Issue Report**: Monitors internal consumption by departments. Filterable by Department and Item.
- **Stock Summary Report**: Provides a snapshot of current stock levels across the inventory. Identifies **Low Stock** (below reorder level) and **Critical Stock** (below min level) items.
- **Daily Movement Report**: A reconciliation tool showing opening balance, daily inflows/outflows, and closing balance for a specific date.

### B. Financial & Analytical Reports
- **Item Ledger Report**: A comprehensive historical log for a specific item. It calculates the **Opening Stock** at the start of a period and tracks every transaction (IN/OUT) to arrive at the **Closing Balance**, providing a clear audit trail.
- **Vendor Summary Report**: Analyzes vendor performance and financial status. Tracks total PO value vs. actual inward value and provides the current **Outstanding Balance** from the vendor ledger.

### C. Technical Implementation of Reports
- **Confirmed Document Basis**: All reports (Inward, Issue, Ledger) are generated by querying **Confirmed** documents. Drafts are excluded to ensure data integrity and accurate stock/financial representation.
- **Date Range Filtering**: Reports use `LocalDate` for filtering. The `InventoryReportService` processes historical data within these ranges to provide snapshots and movement summaries.
- **Opening Stock Calculation**: The Item Ledger and Daily Movement reports dynamically calculate opening stock by aggregating all confirmed inflows (Inwards) and outflows (Issues) prior to the requested start date.
- **Real-time Aggregation**: Instead of pre-calculating report data, the system performs real-time stream-based aggregation of document items (ReceivedItems/IssuedItems) to ensure reports always reflect the most current state of the database.

---

## 6. Architectural Context: Unused Repositories

In the current implementation, you may notice that `ReceivedItemRepository` is defined but rarely (or never) used directly in service logic or report generation.

### Why ReceivedItemRepository is Bypasssed:
1. **Aggregate Root Pattern**: The `MaterialInward` entity acts as the **Aggregate Root**. Following Domain-Driven Design (DDD) principles, operations on `ReceivedItems` are managed through the parent `MaterialInward` document.
2. **JPA Cascading**: `MaterialInward` is configured with `cascade = CascadeType.ALL` and `orphanRemoval = true` for its `receivedItems` collection. This means when you save or delete a `MaterialInward`, all its line items are automatically handled by the JPA provider (Hibernate).
3. **Document-Centric Reporting**: Reports are generated by fetching `MaterialInward` documents with their items eagerly (using JOIN FETCH or similar patterns). This ensures that items are always viewed in the context of their parent document (vendor, date, challan number), which is essential for audit trails.
4. **Data Integrity**: By not exposing direct CRUD operations on `ReceivedItem` via its own repository, the system prevents "orphaned" items or modifications to line items that haven't been validated by the parent document's state (e.g., ensuring items can't be added to a 'CONFIRMED' inward).

The repository exists primarily for future extensibility or specific low-level queries that might bypass the aggregate root for performance optimization in very large datasets, but the current business logic intentionally routes all interactions through `MaterialInwardRepository`.

