# Inventory Module Documentation

This document provides a comprehensive overview of the Inventory Management System within the Foundry Spring Backend. It covers the directory structure, entity relationships, core business logic, and data flow.

## 1. Directory Structure

The inventory module is located under `com.kalibyte.foundry.inventory` and is organized into sub-packages based on functional domains:

- `common/`: Shared utilities and base classes.
    - `BaseInventoryEntity`: Mapped superclass with common fields (id, audit timestamps, created/updated by).
    - `NumberGenerators`: Services to generate unique identifiers for POs, Inwards, and Issues.
- `department/`: Internal departments that consume materials.
- `item/`: The core of the inventory system. Defines items, categories, and handles stock calculations and adjustments.
- `vendor/`: External suppliers.
- `ledger/`: Financial tracking for vendor transactions (accounts payable).
- `purchaseorder/`: Procurement process, tracking what is ordered from vendors and historical rates.
- `purchaseinvoice/`: Vendor invoice recording, verification, and GST reporting.
- `inward/`: Material reception, updating stock and financial ledgers.
- `issue/`: Internal material distribution to departments.
- `report/`: Analytics and business intelligence, generating stock and financial reports.

---

## 2. Core Entities & Relationships

### Item (`item.entity.Item`)
The central entity representing a stockable material.
- **Attributes**: Code, Name, Category, Sub-Category, Unit, Current Stock, Reorder Level, Min Stock Level, Avg Rate, Last Purchase Rate.
- **Scrap Attributes**:
    - `isScrap` (Boolean): Marks if the item represents recyclable scrap.
    - `grade` (String): The material grade (e.g., FG260, SG400) for metallurgical tracking.
- **Mappings**: 
    - `ManyToOne` with `Department` (primary consuming department).
- **Key Logic**: 
    - `receiveStock(qty, rate)`: Increases `currentStock` and recalculates `avgRate` using a Weighted Average Cost (WAC) method. Updates `lastPurchaseRate`.
    - `issueStock(qty)`: Decreases `currentStock`. Throws `BusinessException` if stock is insufficient.
    - `adjustStock(qty, rate)`: Handles manual stock corrections (positive or negative).

### Stock Adjustment (`item.entity.StockAdjustment`)
Records manual corrections to stock levels.
- **Attributes**: Adjustment Number, Date, Reason.
- **Mappings**:
    - `OneToMany` with `AdjustmentItem`.

### Purchase Order (`purchaseorder.entity.PurchaseOrder`)
Represents a formal request to a vendor for materials.
- **Status**: `OPEN`, `PARTIALLY_RECEIVED`, `RECEIVED`, `CANCELLED`.
- **Mappings**: 
    - `ManyToOne` with `Vendor`.
    - `OneToMany` with `OrderItem` (Composition).

### Item Vendor Rate (`purchaseorder.entity.ItemVendorRate`)
Tracks the historical/last purchase rate for a specific item from a specific vendor.
- **Attributes**: Last Rate, Last Purchase Date.

### Material Inward (`inward.entity.MaterialInward`)
Records the reception of materials.
- **Status**: `DRAFT`, `CONFIRMED`.
- **Mappings**:
    - `ManyToOne` with `PurchaseOrder` (Optional).
    - `ManyToOne` with `Vendor`.
    - `OneToMany` with `ReceivedItem` (Composition).

### Material Issue (`issue.entity.MaterialIssue`)
Records the consumption of material by an internal department.
- **Mappings**:
    - `ManyToOne` with `Department`.
    - `OneToMany` with `IssuedItem` (Composition).

### Purchase Invoice (`purchaseinvoice.entity.PurchaseInvoice`)
Records the vendor's official invoice for received materials.
- **Attributes**: Vendor Invoice Number, Date, Invoice Amount, Verification Status, Source (AUTO/MANUAL).
- **Mappings**:
    - `ManyToOne` with `Vendor`.
    - `ManyToOne` with `PurchaseOrder` (Optional).
    - `ManyToOne` with `MaterialInward` (Optional).
- **Key Logic**:
    - `verify(userId)`: Marks the invoice as verified for payment processing.
    - `getAmountMismatch()`: Calculates variance between invoice amount and inward value.

### Vendor Ledger (`ledger.entity.VendorLedger`)
Tracks financial obligations to vendors.
- **Mappings**:
    - `ManyToOne` with `Vendor`.
    - `ManyToOne` with `MaterialInward`.

---

## 3. Core Business Workflows

### A. The Procurement & Reception Flow
1. **Create PO**: A `PurchaseOrder` is created. Status is `OPEN`.
2. **Start Inward**: A `MaterialInward` is initialized, referencing a `PO`.
3. **Confirm Inward**: When physical material is verified:
    - `MaterialInward` status becomes `CONFIRMED`.
    - `Item.receiveStock()` is called: `currentStock` increases, `avgRate` is recalculated.
    - `ItemVendorRate` is updated with the latest price.
    - `VendorLedger` records a credit/payable.
    - `PurchaseInvoice` is automatically generated (if configured).

### B. The Internal Consumption Flow
1. **Record Issue**: A `MaterialIssue` is created for a `Department`.
2. **Process Items**: For each item, `Item.issueStock()` validates availability and decreases stock. The current `avgRate` is captured for costing.

### C. Internal Returns (Scrap Recycling)
- Generated from Furnace Heats or Quality Inspections.
- Uses automated `INTERNAL_RETURN` inwards to return material to stock.

### D. Stock Adjustment Flow
1. **Identify Discrepancy**: Physical stock doesn't match system stock.
2. **Record Adjustment**: Create a `StockAdjustment` with the delta quantity.
3. **Update Item**: `Item.adjustStock()` updates the balance and WAC if adding stock.

### E. Purchase Invoice Verification
1. **Record Invoice**: Invoices are captured (either auto-generated from Inwards or manually entered).
2. **Reconciliation**: System flags mismatches between the vendor's invoice amount and the system's recorded inward value.
3. **Verification**: Authorized users verify invoices once discrepancies are resolved.

### F. Furnace Integration (Automatic Issuance)
- **Flow**: Heat recorded → Validate stock → Create `MaterialIssue` for `FURNACE` department.
- Automated raw material deduction ensures real-time inventory accuracy without manual store entry.

---

## 4. Technical Implementation Details

- **Concurrency**: Services use `@Transactional` for atomic operations.
- **Audit**: `BaseInventoryEntity` tracks audit timestamps and users.
- **Weighted Average Cost (WAC)**: Stock valuation is recalculated on every receipt using the formula:
  `New Avg Rate = (Existing Value + Incoming Value) / (Existing Qty + Incoming Qty)`
- **Number Generation**: Sequence-based identifiers (e.g., `PO-2026-0001`, `INW-2026-0001`).

---

## 6. Database Schema

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

| Column             | Type          | Constraints      | Description               |
|--------------------|---------------|------------------|---------------------------|
| id                 | BIGSERIAL     | PRIMARY KEY      |                           |
| name               | VARCHAR(255)  | NOT NULL         |                           |
| code               | VARCHAR(50)   | UNIQUE, NOT NULL | Unique item code          |
| category           | VARCHAR(30)   | NOT NULL         | RAW_MATERIAL, ALLOY, etc. |
| department_id      | BIGINT        | FK (departments) | Primary department        |
| unit               | VARCHAR(20)   | NOT NULL         | KG, PCS, etc.             |
| current_stock      | DECIMAL(15,3) | DEFAULT 0        | Current quantity on hand  |
| avg_rate           | DECIMAL(12,2) | DEFAULT 0        | Weighted Average Cost     |
| last_purchase_rate | DECIMAL(12,2) | DEFAULT 0        |                           |
| reorder_level      | DECIMAL(15,3) | DEFAULT 0        |                           |

### `purchase_orders` & `purchase_order_items`
Tracks formal procurement requests.

| Table | Description |
|-------|-------------|
| `purchase_orders` | Header with `po_number`, `vendor_id`, `status`, `po_date`. |
| `purchase_order_items` | Lines with `item_id`, `ordered_quantity`, `received_quantity`, `unit_rate`. |

### `material_inwards` & `received_items`
Tracks reception of materials.

| Table | Description |
|-------|-------------|
| `material_inwards` | Header with `inward_number`, `vendor_id`, `status`, `inward_date`. |
| `received_items` | Lines with `item_id`, `received_quantity`, `unit_rate`. |

### `material_issues` & `issued_items`
Tracks internal consumption by departments.

| Table | Description |
|-------|-------------|
| `material_issues` | Header with `issue_number`, `department_id`, `issue_date`. |
| `issued_items` | Lines with `item_id`, `issued_quantity`, `unit_rate`. |

### `purchase_invoices`
Records vendor invoices.

| Column | Type | Description |
|--------|------|-------------|
| id | BIGSERIAL | |
| vendor_invoice_number | VARCHAR(50) | |
| vendor_invoice_date | DATE | |
| invoice_amount | DECIMAL(12,2) | |
| vendor_id | BIGINT | |
| material_inward_id | BIGINT | |
| is_verified | BOOLEAN | |

### `stock_adjustments` & `adjustment_items`
Records manual stock corrections.

| Table | Description |
|-------|-------------|
| `stock_adjustments` | Header with `adjustment_number`, `adjustment_date`, `reason`. |
| `adjustment_items` | Lines with `item_id`, `adjusted_quantity`, `unit_rate`. |

### `vendor_ledger`
Tracks financial obligations (payables) to vendors.

