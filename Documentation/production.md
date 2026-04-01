# Production Module Documentation

## Overview

The **Production Module** in the Foundry ERP system manages the daily manufacturing operations on the shop floor. It tracks the progress of casting parts through various stages of production, from core making and moulding to cleaning (fettling) and final dispatch.

This module provides real-time visibility into order status, helps identify bottlenecks in the manufacturing process, and ensures traceability back to specific furnace heats.

---

## 1. Core Workflow

Production tracking is centered around **Daily Production Entries**. The workflow typically follows these steps:

1.  **Entry Creation**: A supervisor creates a `ProductionEntry` for a specific `Order`, `Date`, and `Shift`.
2.  **Stage Tracking**: For each `OrderItem` in the order, quantities are recorded at different manufacturing stages:
    -   **Ready Cores**: Internal sand shapes prepared for the casting.
    -   **Poured Moulds**: The actual casting process where metal is poured into the mold.
    -   **Shot Blasting**: Initial cleaning process using abrasive materials.
    -   **Fettling**: Final cleaning, grinding, and removal of risers and runners.
    -   **Dispatched**: Items ready for shipping or already moved out of the production area.
3.  **Traceability**: Production items can be linked to specific `HeatOrderItems` from the **Furnace Module**, allowing the factory to know exactly which melt a specific part came from.
4.  **Completion**: Once all items for an order are processed through all stages, the production entry or the overall order status can be updated.

---

## 2. Key Entities

### ProductionEntry
Represents a shift-wise report for a specific order.

| Column | Type | Description |
| --- | --- | --- |
| id | UUID | Primary Key |
| entry_number | VARCHAR | Unique identifier (e.g., PROD-2026-0001) |
| order_id | UUID | Reference to the Customer Order |
| report_date | DATE | The date of the production shift |
| shift | ENUM | DAY or NIGHT |
| status | ENUM | IN_PROGRESS, COMPLETED, CANCELLED |
| operator_name | VARCHAR | Name of the shift supervisor/operator |
| total_counts | INTEGER | Aggregate counts for all stages (Cores, Moulds, etc.) |

### ProductionItem
Granular tracking for each individual part within a production entry.

| Column | Type | Description |
| --- | --- | --- |
| id | UUID | Primary Key |
| production_entry_id | UUID | Link to the parent entry |
| order_item_id | UUID | Link to the specific part being produced |
| heat_order_item_id | UUID | (Optional) Link to the furnace heat record |
| ready_cores | INTEGER | Quantity of cores completed |
| poured_moulds | INTEGER | Quantity of moulds poured |
| shot_blasting_quantity | INTEGER | Quantity cleaned via shot blasting |
| fettling_quantity | INTEGER | Quantity finished in fettling |
| dispatched_quantity | INTEGER | Quantity moved to dispatch |

---

## 3. Service Layer Architecture

The module follows a standard layered architecture:

-   **Controller Layer**: Handles REST requests, security (RBAC), and validation.
-   **Service Layer (`ProductionService`)**: Contains business logic, including status transition validation, auto-calculation of totals, and integration with the furnace module.
-   **Repository Layer**: Uses Spring Data JPA with `Specifications` for advanced filtering (e.g., filtering entries by date range, order, or shift).
-   **Mapper Layer**: Uses MapStruct to convert between Entities and DTOs efficiently.

---

## 4. API Endpoints

The Production API is protected by role-based access control (RBAC). Only users with `ADMIN` or `PRODUCTION` roles can access these endpoints.

| Method | Endpoint | Description |
| --- | --- | --- |
| POST | `/api/production` | Create a new production entry |
| GET | `/api/production/{id}` | Get detailed information about an entry |
| GET | `/api/production` | List entries with filters (date, order, shift, status) |
| PUT | `/api/production/{id}` | Update an existing production entry |
| PATCH | `/api/production/{id}/status` | Update the status (e.g., mark as COMPLETED) |
| DELETE | `/api/production/{id}` | Soft delete an entry (Admin only) |

---

## 5. Integration with Other Modules

-   **Order Module**: Production entries are always linked to an existing `Order`. The module validates that quantities don't exceed the original ordered amount.
-   **Furnace Module**: Through the `heat_order_item_id`, production is linked to the melting process. This enables "Metal-to-Part" traceability.
-   **Pattern Module**: Tracks which pattern was used for the production of specific items.
-   **Quality Module (Future)**: Production rejections (found during fettling) are fed into the Quality and Scrap modules for recycling.

---

## 6. Business Rules

-   **Unique Entries**: Only one production entry per Order/Date/Shift combination is typically allowed to prevent duplicate reporting.
-   **Sequence Validation**: While not strictly enforced at the database level, the application logic encourages items to flow logically through the stages (e.g., you shouldn't fettle more than you poured).
-   **Audit Trail**: Every change to a production entry tracks the user and timestamp via the `BaseEntity`.

---

## Summary

The Production Module bridges the gap between the Sales Order and the Final Invoice. By tracking the physical manufacturing stages in detail, it provides the foundry management with the data needed to optimize shift performance and ensure timely delivery to customers.
