# Scrap Management Module Documentation

## Overview

The **Scrap Management Module** provides a comprehensive framework for tracking, verifying, and recycling all types of scrap material within the foundry. By closing the loop between production and raw material inventory, it ensures accurate material reconciliation and reduces waste.

---

## 1. Scrap Sources

Scrap can originate from various stages of the manufacturing process:

| Source                   | Description                                                                                          |
|--------------------------|------------------------------------------------------------------------------------------------------|
| **PROCESS_SCRAP**        | Recyclable material from the gating system (runners, risers), furnace residue (skull), and spillage. |
| **PRODUCTION_REJECTION** | Castings that fail quality inspection due to defects (blowholes, cracks, etc.).                      |
| **CUSTOMER_RETURN**      | Products returned by customers that are deemed unusable and must be scrapped.                        |
| **UNKNOWN_YARD_SCRAP**   | Material found in the yard that needs to be graded and re-entered into the system.                   |

---

## 2. Scrap Workflow

The system enforces a strict verification and approval workflow to maintain metallurgical integrity.

### Workflow Stages
1. **PENDING_VERIFICATION**: The initial state when scrap is reported (e.g., automatically from a Furnace Heat).
2. **VERIFIED**: A metallurgist or supervisor has physically checked the scrap batch for cleanliness and grade accuracy.
3. **APPROVED_FOR_RETURN**: Authorized personnel have approved the scrap to be remelted. This triggers the **Automated Inventory Return**.
4. **REJECTED_FOR_RETURN**: Scrap that is contaminated or otherwise unsuitable for remelting. These batches are typically marked for external sale.
5. **RETURNED_TO_INVENTORY**: The final state after the system has successfully updated the raw material stock.
6. **SOLD / DISPOSED**: Final states for scrap that was not returned to the melting cycle.

> [!IMPORTANT]
> **Status Immutability & Idempotency**: Once a scrap entry reaches a terminal state (`APPROVED_FOR_RETURN`, `REJECTED_FOR_RETURN`, or `RETURNED_TO_INVENTORY`), it is locked. The system strictly prevents multiple approval calls for the same entry to ensure inventory levels are not artificially inflated by duplicate stock increases. Any attempt to re-approve a processed entry will result in a `BusinessException`.

---

## 3. Service Layer & Business Logic

The `ScrapService` acts as the central orchestrator for scrap lifecycle events, coordinating with the Inventory and Furnace modules.

### Key Operations
- **Verification**: Marks entries as `VERIFIED` and captures verification notes and supervisor identities.
- **Approval Decisioning**: 
    - Implements strict state validation: Terminal states (`APPROVED_FOR_RETURN`, `REJECTED_FOR_RETURN`, `RETURNED_TO_INVENTORY`) cannot be re-processed.
    - If approved for remelt, it triggers `createInventoryInward`.
- **Automated Inventory Inward**: 
    - Converts scrap items into an `InternalReturnRequest`.
    - Delegates to `MaterialInwardService` for immediate stock increment.
    - Sets the final status to `RETURNED_TO_INVENTORY` upon successful inward.
- **Just-in-Time (JIT) Item Management**:
    - If a scrap batch lacks a specific inventory `itemId` (common for manual yard entries), the service automatically searches for a matching scrap item by `grade`.
    - If no such item exists, it creates a new `Item` (e.g., "FG260 Process Scrap") assigned to the **FURNACE** department.

---

## 4. Automated Inventory Integration

### Just-in-Time Item Creation
To ensure that every grade of scrap is tracked accurately without manual configuration:
- When scrap is approved for return, the system searches for an inventory item with `isScrap = true` and a matching `grade` (e.g., `FG260`).
- If no matching item exists, the system automatically creates a new item (e.g., "FG260 Process Scrap", Code: `SCR-FG260`) in the **FURNACE** department.

### Automated Material Inward
1. Upon approval (`APPROVE_REMELT`), the system generates an **Internal Material Inward**.
2. This inward is **Automatically Confirmed**, instantly increasing the `currentStock` of the relevant scrap item.
3. The average rate for this scrap is typically set to ₹0, as it is a byproduct of internal processes.

---

## 5. Furnace Heat Integration

The Scrap module is tightly coupled with the **Furnace Module**:
- **Automatic Generation**: When a heat is saved, the system calculates total process scrap from runner, riser, skull, and spillage weights.
- **Traceability**: Every process scrap entry is linked back to the specific heat ID from which it originated.
- **Grade Inheritance**: Process scrap automatically inherits the material grade of the heat, providing high confidence in its metallurgical composition.

---

## 6. API Endpoints

### Scrap Entries
- `GET /api/scrap`: List all scrap entries.
- `GET /api/scrap?status=REJECTED_FOR_RETURN`: List scrap entries ready for external sale.
- `GET /api/scrap/{id}`: Get details of a specific scrap batch.
- `POST /api/scrap`: Manually record a scrap entry (e.g., for yard scrap).

### Workflow Operations
- `PUT /api/scrap/{id}/verify`: Verify a scrap batch.
- `PUT /api/scrap/{id}/approve`: Approve or reject scrap for return to inventory.

---

## 7. Business Logic & Calculations

### Total Weight
For process scrap, the total entry weight is the sum of:
`Runner Weight + Riser Weight + Skull Weight + Spillage Weight`

### Confidence Levels
The system assigns confidence levels based on the source:
- **HIGH**: Automatically generated from a validated furnace heat.
- **MEDIUM/LOW**: Manual entries or visual assessments that require more rigorous verification.

---

## Summary

The Scrap Management module ensures that the foundry operates as a circular system. By automating the re-integration of process scrap into inventory, it provides real-time visibility into available remelt material and ensures that production yields are accurately reflected in the company's financial and operational data.
