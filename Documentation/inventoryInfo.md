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

### D. Furnace Integration (Automatic Issuance)
- When a **Furnace Heat** is recorded, the system automatically triggers a material issuance.
- **Flow**: Heat recorded → Validate stock availability → Create `MaterialIssue` for the `FURNACE` department.
- This ensures that raw material consumption (Pig Iron, Scrap, Alloys) is automatically deducted from inventory without manual entry by store personnel.
- For more details, refer to the [Furnace Module Documentation](furnaceDocument.md).

---

## 4. Technical Implementation Details

- **Concurrency**: Services use `@Transactional` to ensure atomicity. Stock updates and ledger entries happen in the same transaction as document confirmation.
- **Audit**: All entities extending `BaseInventoryEntity` automatically track `createdAt`, `updatedAt`, `createdBy`, and `updatedBy`.
- **Soft Deletes/Activation**: `Item` and `Vendor` have `isActive` flags instead of hard deletes to maintain referential integrity with historical documents.
- **Number Generation**: Custom generators (`PONumberGenerator`, etc.) ensure human-readable unique IDs for documents (e.g., `PO-2024-001`).
- **Data Integrity**: Database-level constraints (foreign keys, unique constraints on codes/numbers) complement Java-side business logic validation.

---

## 5. Reporting & Analytics

The system provides a suite of reports for operational and financial visibility via the `InventoryReportController` and `MaterialIssueController`.

### A. Operational Reports

#### 1. Material Inward Report
- **Endpoint**: `GET /api/inventory/reports/inwards`
- **Description**: Tracks material receipts from vendors.
- **Parameters**:
    - `startDate` (Optional, LocalDate): defaults to the first day of the current month.
    - `endDate` (Optional, LocalDate): defaults to today.
    - `vendorId` (Optional, Long): filter by a specific vendor.
    - `itemId` (Optional, Long): filter by a specific item.
    - `purchaseOrderId` (Optional, Long): filter by a specific PO.

#### 2. Material Issue Report
- **Endpoint**: `GET /api/inventory/reports/issues`
- **Description**: Monitors internal material distribution to departments.
- **Parameters**:
    - `startDate` (Optional, LocalDate): defaults to the first day of the current month.
    - `endDate` (Optional, LocalDate): defaults to today.
    - `departmentId` (Optional, Long): filter by a specific department.
    - `itemId` (Optional, Long): filter by a specific item.

#### 3. Department Consumption Report
- **Endpoint**: `GET /api/material-issues/consumption-report`
- **Description**: Aggregates total quantity and value of items consumed by a department.
- **Parameters**:
    - `departmentId` (Required, Long)
    - `from` (Required, LocalDate)
    - `to` (Required, LocalDate)

#### 4. Stock Summary Report
- **Endpoint**: `GET /api/inventory/reports/stock-summary`
- **Description**: Snapshot of current stock levels across the inventory.
- **Parameters**:
    - `category` (Optional, String): filter by item category (e.g., RAW_MATERIAL).
    - `belowReorderLevel` (Optional, Boolean): filter for items needing replenishment.
    - `departmentId` (Optional, Long): filter by primary department.

#### 5. Daily Stock Movement Report
- **Endpoint**: `GET /api/inventory/reports/daily-movement`
- **Description**: Reconciliation tool showing opening balance, daily inflows/outflows, and closing balance.
- **Parameters**:
    - `date` (Optional, LocalDate): defaults to today.
    - `category` (Optional, String): filter by item category.

### B. Financial & Analytical Reports

#### 1. Item Ledger Report
- **Endpoint**: `GET /api/inventory/reports/items/{itemId}/ledger`
- **Description**: Historical log for a specific item showing every IN/OUT transaction and running balance.
- **Parameters**:
    - `itemId` (Required, Path Variable)
    - `startDate` (Optional, LocalDate): defaults to one month ago.
    - `endDate` (Optional, LocalDate): defaults to today.

#### 2. Vendor Summary Report
- **Endpoint**: `GET /api/inventory/reports/vendor-summary`
- **Description**: Analyzes vendor performance, total PO vs. Inward value, and outstanding balances.
- **Parameters**:
    - `startDate` (Optional, LocalDate): defaults to first day of current month.
    - `endDate` (Optional, LocalDate): defaults to today.
    - `vendorId` (Optional, Long): filter for a specific vendor.

### C. Technical Implementation of Reports
- **Confirmed Document Basis**: All reports are generated from **Confirmed** documents. Drafts are excluded.
- **Opening Stock Calculation**: Dynamically calculated for Ledger and Movement reports by aggregating all transactions prior to the start date.
- **Real-time Aggregation**: Reports perform stream-based aggregation of line items (ReceivedItems/IssuedItems) for real-time accuracy.

---

## 6. Architectural Context: Unused Repositories

In the current implementation, you may notice that `ReceivedItemRepository` is defined but rarely (or never) used directly in service logic or report generation.

### Why ReceivedItemRepository is Bypasssed:
1. **Aggregate Root Pattern**: The `MaterialInward` entity acts as the **Aggregate Root**. Following Domain-Driven Design (DDD) principles, operations on `ReceivedItems` are managed through the parent `MaterialInward` document.
2. **JPA Cascading**: `MaterialInward` is configured with `cascade = CascadeType.ALL` and `orphanRemoval = true` for its `receivedItems` collection. This means when you save or delete a `MaterialInward`, all its line items are automatically handled by the JPA provider (Hibernate).
3. **Document-Centric Reporting**: Reports are generated by fetching `MaterialInward` documents with their items eagerly (using JOIN FETCH or similar patterns). This ensures that items are always viewed in the context of their parent document (vendor, date, challan number), which is essential for audit trails.
4. **Data Integrity**: By not exposing direct CRUD operations on `ReceivedItem` via its own repository, the system prevents "orphaned" items or modifications to line items that haven't been validated by the parent document's state (e.g., ensuring items can't be added to a 'CONFIRMED' inward).

The repository exists primarily for future extensibility or specific low-level queries that might bypass the aggregate root for performance optimization in very large datasets, but the current business logic intentionally routes all interactions through `MaterialInwardRepository`.

---

## 7. Database Schema

The following tables define the structure of the Inventory module and its financial tracking.

### `vendors`
Stores external supplier information.

| Column     | Type         | Constraints  | Description       |
|------------|--------------|--------------|-------------------|
| id         | BIGSERIAL    | PRIMARY KEY  | Unique identifier |
| name       | VARCHAR(255) | NOT NULL     |                   |
| phone      | VARCHAR(20)  |              |                   |
| gst_number | VARCHAR(20)  |              |                   |
| address    | TEXT         |              |                   |
| is_active  | BOOLEAN      | DEFAULT TRUE |                   |

### `items`
The central table for stockable materials.

| Column        | Type          | Constraints      | Description               |
|---------------|---------------|------------------|---------------------------|
| id            | BIGSERIAL     | PRIMARY KEY      |                           |
| name          | VARCHAR(255)  | NOT NULL         |                           |
| code          | VARCHAR(50)   | UNIQUE, NOT NULL | Unique item code          |
| category      | VARCHAR(30)   | NOT NULL         | RAW_MATERIAL, ALLOY, etc. |
| department_id | BIGINT        | FK (departments) | Primary department        |
| unit          | VARCHAR(20)   | NOT NULL         | KG, PCS, etc.             |
| current_stock | DECIMAL(15,3) | DEFAULT 0        | Current quantity on hand  |
| avg_rate      | DECIMAL(12,2) | DEFAULT 0        | Weighted Average Cost     |
| reorder_level | DECIMAL(15,3) | DEFAULT 0        |                           |

### `purchase_orders` & `purchase_order_items`
Tracks formal procurement requests.

### **purchase_orders**
| Column    | Type        | Constraints      | Description          |
|-----------|-------------|------------------|----------------------|
| id        | BIGSERIAL   | PRIMARY KEY      |                      |
| po_number | VARCHAR(50) | UNIQUE, NOT NULL | e.g., PO-2024-001    |
| vendor_id | BIGINT      | FK (vendors)     |                      |
| status    | VARCHAR(30) | NOT NULL         | OPEN, RECEIVED, etc. |
| po_date   | DATE        | NOT NULL         |                      |

### **purchase_order_items**
| Column            | Type          | Constraints          | Description |
|-------------------|---------------|----------------------|-------------|
| id                | BIGSERIAL     | PRIMARY KEY          |             |
| po_id             | BIGINT        | FK (purchase_orders) | Parent PO   |
| item_id           | BIGINT        | FK (items)           |             |
| ordered_quantity  | DECIMAL(15,3) | NOT NULL             |             |
| received_quantity | DECIMAL(15,3) | DEFAULT 0            |             |
| unit_rate         | DECIMAL(12,2) | NOT NULL             |             |

### `material_inwards` & `received_items`
Tracks reception of materials.

### **material_inwards**
| Column        | Type        | Constraints          | Description      |
|---------------|-------------|----------------------|------------------|
| id            | BIGSERIAL   | PRIMARY KEY          |                  |
| inward_number | VARCHAR(50) | UNIQUE, NOT NULL     |                  |
| po_id         | BIGINT      | FK (purchase_orders) | Optional         |
| vendor_id     | BIGINT      | FK (vendors)         |                  |
| inward_date   | DATE        | NOT NULL             |                  |
| status        | VARCHAR(20) | DEFAULT 'DRAFT'      | DRAFT, CONFIRMED |

### **received_items**
| Column             | Type          | Constraints           | Description          |
|--------------------|---------------|-----------------------|----------------------|
| id                 | BIGSERIAL     | PRIMARY KEY           |                      |
| material_inward_id | BIGINT        | FK (material_inwards) | Parent Inward        |
| item_id            | BIGINT        | FK (items)            |                      |
| received_quantity  | DECIMAL(15,3) | NOT NULL              |                      |
| unit_rate          | DECIMAL(12,2) | NOT NULL              |                      |
| amount             | DECIMAL(15,2) | GENERATED             | quantity * unit_rate |

### `material_issues` & `issued_items`
Tracks internal consumption by departments.

### **material_issues**
| Column        | Type        | Constraints      | Description          |
|---------------|-------------|------------------|----------------------|
| id            | BIGSERIAL   | PRIMARY KEY      |                      |
| issue_number  | VARCHAR(50) | UNIQUE, NOT NULL |                      |
| department_id | BIGINT      | FK (departments) | Consuming department |
| issue_date    | DATE        | NOT NULL         |                      |

### **issued_items**
| Column            | Type          | Constraints          | Description               |
|-------------------|---------------|----------------------|---------------------------|
| id                | BIGSERIAL     | PRIMARY KEY          |                           |
| material_issue_id | BIGINT        | FK (material_issues) | Parent Issue              |
| item_id           | BIGINT        | FK (items)           |                           |
| issued_quantity   | DECIMAL(15,3) | NOT NULL             |                           |
| unit_rate         | DECIMAL(12,2) | NOT NULL             | Captured at time of issue |

### `vendor_ledger`
Tracks financial obligations to vendors.

| Column             | Type          | Constraints           | Description |
|--------------------|---------------|-----------------------|-------------|
| id                 | BIGSERIAL     | PRIMARY KEY           |             |
| vendor_id          | BIGINT        | FK (vendors)          |             |
| material_inward_id | BIGINT        | FK (material_inwards) | Optional    |
| entry_type         | VARCHAR(10)   | CHECK (CREDIT, DEBIT) |             |
| amount             | DECIMAL(15,2) | NOT NULL              |             |
| entry_date         | DATE          | NOT NULL              |             |

