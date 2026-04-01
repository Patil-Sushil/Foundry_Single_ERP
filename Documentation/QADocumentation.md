# Quality Assurance (QA) Module Documentation

## Overview

The **Quality Assurance (QA) Module** is responsible for maintaining and verifying the quality standards of all casting products in the Foundry ERP system. It manages the complete lifecycle of quality control, from in-process and final inspections to customer returns and scrap disposition.

The module ensures full traceability by linking quality data directly to production entries, furnace heats, and customer orders.

---

## 1. QA Workflows

### 1.1 In-Process & Final Inspection
Inspections are performed on production items at various stages (e.g., after fettling).
1.  **Draft Creation**: An inspector creates a `QaInspection` for a specific `ProductionItem`.
2.  **Recording Findings**: Specific defects are recorded as `InspectionFindings` from the `DefectCatalog`.
3.  **Completion**: Upon completion, the system:
    *   Updates the `ProductionItem` with inspected, accepted, rejected, and rework quantities.
    *   Automatically creates a `QaRejection` if there are any rejected items.
    *   Sets the `dispatched_quantity` on the `ProductionItem` based on the accepted amount (QA-controlled dispatch).
4.  **Audit**: Every state change is logged in the `QaTrackingLog`.

### 1.2 Customer Returns Workflow
Handles products returned by customers due to defects or complaints.
1.  **Receipt**: Return is recorded as `CustomerReturn` with `RECEIVED` status.
2.  **Assessment**: QA assesses the return, confirming the defect and identifying the root cause. Status moves to `ASSESSED`.
3.  **Disposition**: Management decides the final outcome (Scrap, Credit Note, Replacement, etc.).
4.  **Closing**: Once disposition is implemented (e.g., scrap entry created), the return is marked as `CLOSED`.

---

## 2. Database Schema

### `qa_defect_catalog`
Master list of known defects in the foundry process.
*   `code`: Unique identifier (e.g., BH-001).
*   `category`: CASTING, SURFACE, DIMENSIONAL, CHEMICAL, etc.
*   `severity`: CRITICAL, MAJOR, MINOR.

### `qa_inspections`
Primary record for a quality check session.
*   **Links**: `production_entry_id`, `production_item_id`, `order_id`, `order_item_id`.
*   **Stages**: AFTER_POURING, AFTER_SHOT_BLASTING, AFTER_FETTLING, FINAL_INSPECTION.
*   **Results**: PASSED, FAILED, CONDITIONAL_PASS.

### `qa_inspection_findings`
Details of defects found during an inspection.
*   **Links**: `inspection_id`, `defect_id`.
*   **Disposition**: REJECT, REWORK, USE_AS_IS, HOLD.

### `qa_rejections`
Tracks items rejected during inspection that require disposition.
*   **Links**: `inspection_id`, `production_item_id`.
*   **Disposition**: SCRAP_FOR_REMELT, SCRAP_FOR_SALE, REWORK, DOWNGRADE, CUSTOMER_CONCESSION.

### `qa_customer_returns`
Manages customer complaints and physical returns.
*   **Links**: `customer_id`, `order_id`, `order_item_id`.
*   **Disposition**: SCRAP_FOR_REMELT, CREDIT_NOTE, REPLACE, RETURN_TO_CUSTOMER.

### `qa_tracking_log`
Centralized audit trail for all QA entities.
*   `reference_type`: REJECTION, CUSTOMER_RETURN, INSPECTION.
*   `reference_id`: ID of the entity.
*   `action`: CREATED, INSPECTED, DISPOSITIONED, CLOSED, CANCELLED.

---

## 3. Module Connections & Integration

### Production Integration
The QA module directly modifies `production_items` and `production_entries` during inspection completion:
*   `inspected_quantity`
*   `accepted_quantity`
*   `rejected_quantity`
*   `rework_quantity`
*   **Dispatch Control**: `ProductionItem.dispatched_quantity` is auto-set by QA to ensure only accepted pieces are billed.

### Scrap & Inventory Integration
When a `QaRejection` or `CustomerReturn` is dispositioned as `SCRAP_FOR_REMELT`:
*   A `ScrapEntry` is automatically created.
*   The `ScrapEntry` is linked via `qa_rejection_id` or `customer_return_id`.
*   The scrap inherits the `material_grade` from the original order item.

### Traceability Chain
The "Golden Chain" of traceability in the QA module:
`ScrapEntry` → `QaRejection` → `QaInspection` → `ProductionItem` → `FurnaceHeat` → `Order`

---

## 4. Key Enums & Process Details

### Inspection Stages
*   `AFTER_POURING`: Initial check for obvious pouring defects.
*   `AFTER_SHOT_BLASTING`: Check for surface defects after initial cleaning.
*   `AFTER_FETTLING`: Pre-dispatch check after grinding/finishing.
*   `FINAL_INSPECTION`: Final check before packing.

### Root Cause Categories (for Returns)
*   `MELTING`: Issues related to metal composition or temperature.
*   `MOULDING`: Issues related to mold strength or sand quality.
*   `CORE_MAKING`: Issues related to core dimensions or gases.
*   `PATTERN`: Issues related to pattern wear or design.
*   `MACHINING`: Issues occurring during post-casting machining.

---

## 5. Security & Roles

QA endpoints are protected by Role-Based Access Control (RBAC):
*   `QUALITY`: Primary role for inspectors and QA managers. Can create inspections, record findings, and assess returns.
*   `ADMIN`: Full access, including configuring the defect catalog and performing final dispositions.
*   `PRODUCTION`: Read-only access to quality results to monitor performance.

---

## Summary
The QA module ensures that quality is not just a final check, but an integrated part of the manufacturing process. By automating the creation of rejections and scrap entries, it reduces manual data entry and provides a real-time view of cost-of-quality.
