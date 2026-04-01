# Furnace Module Documentation

## Overview

The **Furnace Module** is a critical part of the production tracking system in the Foundry ERP. 
It manages daily furnace operation reports, individual melting cycles (heats), chemical composition tracking, energy consumption analysis, and automatic material issuance from inventory.

This module ensures that every kilogram of metal melted is accounted for, both in terms of energy and raw material consumption.

---

## 1. Furnace Report

A **Furnace Report** represents a collection of heats performed during a specific shift by an operator.

### Core Fields
- **Furnace Reference No**: A unique identifier (e.g., `FUR-2026-0001`) generated automatically.
- **Operator Name**: The person responsible for the furnace during the shift.
- **Shift**: `DAY` or `NIGHT`.
- **Incharge Name**: The supervisor on duty.
- **Date**: The date of operation.

### Lifecycle
A report acts as an aggregate for multiple **Heats**. When a report is created or updated, its associated heats are managed via the `FurnaceHeatService`.

---

## 2. Furnace Heats

A **Furnace Heat** represents a single melting cycle. It captures technical data required for quality control and efficiency analysis.

### Technical Parameters
- **Chemical Composition**: Tracks percentages of Silicon (`sipercentage`), Carbon (`cpcpercentage`), and Magnesium (`mgpercentage`).
- **Energy Readings**:
    - `startReading`: Electricity meter reading at the start of the heat.
    - `stopReading`: Electricity meter reading at the end of the heat.
    - `differenceReading`: Calculated as `stopReading - startReading`.
- **Production Data**:
    - `totalWeight`: Total weight of the metal melted in the heat.
    - `powerToWeight`: Efficiency metric calculated as `differenceReading / totalWeight`.
    - `liquidMetalWeight`: Actual tapped liquid metal weight.
    - `castingsPouredWeight`: Total weight of castings poured (excluding gating).
- **Process Scrap Breakdown**:
    - `runnerWeight`: Weight of runners (gating system).
    - `riserWeight`: Weight of risers/feeders.
    - `skullWeight`: Weight of furnace residue.
    - `spillageWeight`: Weight of metal spillage during pouring.
    - `totalProcessScrap`: Auto-calculated sum of the above scrap types.
- **Yield Calculations**:
    - **Furnace Yield %**: `(Liquid Metal Weight / Total Charge Weight) * 100`. Measures melting efficiency.
    - **Pouring Yield %**: `(Castings Poured Weight / Liquid Metal Weight) * 100`. Measures pouring efficiency.
- **Temperature & Timing**:
    - `pouringTemp`: The temperature of the metal when poured.
    - `pouringStartTime` & `pouringEndTime`: Duration of the pouring process.

### Reference to Orders & Items
A heat is linked to a **Customer Order** and specific **Order Items** via the `heat_order_items` junction table. 
- **Grade Validation**: The system ensures the heat's material grade (e.g., FG260) matches the grade of all linked order items.
- **Production Tracking**: Tracks exactly how many pieces and what weight was produced for each order item within a specific heat.

---

## 3. Service Layer & Business Logic

The Furnace module employs a dual-service architecture to manage the complexity of reports and individual melting cycles.

### Furnace Report Service (`FurnaceService`)
- **Orchestration**: Manages the lifecycle of daily reports and ensures that all associated heats are correctly initialized through the `FurnaceHeatService`.
- **Reference Numbering**: Automatically generates unique, sequence-based reference numbers (e.g., `FUR-2026-0001`).
- **Reporting & Analytics**: Aggregates material consumption and cost data across all heats in a shift to provide operator-level efficiency summaries.

### Furnace Heat Service (`FurnaceHeatService`)
- **Melting Efficiency Calculations**: Automatically computes energy consumption (`differenceReading`) and energy efficiency (`powerToWeight`) during heat creation and updates.
- **Automated Material Issuance**:
    - Validates available inventory stock before allowing a heat to be saved.
    - Triggers the `MaterialIssueService` to deduct raw materials (Pig Iron, Scrap, Alloys) from the **FURNACE** department.
- **Grade & Order Validation**: Enforces metallurgical integrity by ensuring the heat's material grade matches the `material_grade` specified in the customer's `OrderItem`.
- **Heat Update Logic (Delta Handling)**: When a heat is modified, the service calculates the difference (delta) in material usage, either issuing more stock or returning unused material to the inventory.
- **Scrap Synchronization**: Automatically synchronizes the associated `ScrapEntry` weights whenever a heat's process scrap breakdown is updated.

---

## 4. Heat Material Items

Every heat consumes raw materials (Pig Iron, Scrap, Ferro-Alloys, etc.). These are tracked in the `heat_material_items` table.

### Fields
- **Item ID & Name**: Reference to the inventory item.
- **Material Type**: Classified as `PIG_IRON`, `SCRAP`, `ADDITIVE`, etc.
- **Quantity Used**: The amount of material consumed.
- **Unit Rate & Total Cost**: Captured at the time of usage based on the inventory's **Weighted Average Cost (WAC)**.

---

## 5. Inventory & Scrap Integration

### Automatic Material Issuance
1. When a Heat is saved, the system identifies all materials used.
2. It validates if enough stock is available in the inventory.
3. It automatically records a **Material Issue** in the inventory system for the `FURNACE` department.

### Automatic Scrap Generation
1. If `autoReturnScrap` is enabled, saving a heat automatically creates a **Scrap Entry**.
2. This entry captures the runner, riser, skull, and spillage weights.
3. The scrap is categorized by the heat's grade to ensure metallurgical integrity when remelted.
4. **Just-in-Time Linking**: The system automatically links the scrap to a "Scrap Item" in the inventory. If an item for that specific grade doesn't exist, it is created automatically (e.g., "FG260 Process Scrap").

---

## 6. API Endpoints

### Furnace Reports
- `POST /api/furnace/reports`: Create a new daily report with multiple heats.
- `GET /api/furnace/reports/{id}`: Get report details.
- `PUT /api/furnace/reports/{id}`: Update report and its heats.

### Furnace Heats
- `GET /api/furnace/reports/{reportId}/heats`: List all heats for a specific report.
- `GET /api/furnace/heats/by-order/{orderId}`: List all heats associated with a specific customer order.
- `POST /api/furnace/reports/{reportId}/heats`: Create a heat manually within a report.

---

## 7. Business Logic & Calculations

### Power Consumption
```
Difference Reading = Stop Reading - Start Reading
Power to Weight = Difference Reading / Total Weight
```

### Material Costing
```
Total Cost = Quantity Used × Avg Unit Rate (from Inventory)
```

---

## 8. Database Schema

The following tables define the structure of the Furnace module and its integration with orders and inventory.

### `furnace_reports`
Stores shift-level operation data.

| Column         | Type        | Constraints        | Description         |
|----------------|-------------|--------------------|---------------------|
| id             | BIGSERIAL   | PRIMARY KEY        | Unique identifier   |
| furnace_ref_no | VARCHAR(50) | UNIQUE, NOT NULL   | e.g., FUR-2026-0001 |
| operator_name  | VARCHAR(50) | NOT NULL           |                     |
| shift          | VARCHAR(8)  | CHECK (DAY, NIGHT) |                     |
| incharge_name  | VARCHAR(50) |                    | Supervisor name     |
| date           | DATE        | NOT NULL           | Date of operation   |
| created_at     | TIMESTAMP   | DEFAULT NOW()      |                     |

### `furnace_heats`
Stores individual melting cycle (heat) data.

| Column             | Type      | Constraints          | Description                     |
|--------------------|-----------|----------------------|---------------------------------|
| id                 | BIGSERIAL | PRIMARY KEY          | Unique identifier               |
| furnace_id         | BIGINT    | FK (furnace_reports) | Linked report                   |
| sipercentage       | DOUBLE    |                      | Silicon percentage              |
| cpcpercentage      | DOUBLE    |                      | Carbon percentage               |
| mgpercentage       | DOUBLE    |                      | Magnesium percentage            |
| total_weight       | DOUBLE    | NOT NULL             | Total metal weight melted       |
| start_reading      | DOUBLE    | NOT NULL             | Meter start reading             |
| stop_reading       | DOUBLE    | NOT NULL             | Meter stop reading              |
| difference_reading | DOUBLE    |                      | Energy consumed                 |
| power_to_weight    | DOUBLE    |                      | Efficiency metric               |
| pouring_temp       | DOUBLE    |                      | Metal temperature               |
| pouring_start_time | TIME      |                      |                                 |
| pouring_end_time   | TIME      |                      |                                 |
| order_id           | UUID      | FK (orders)          | Optional link to customer order |

### `heat_material_items`
Tracks raw materials consumed for each heat.

| Column        | Type         | Constraints            | Description                 |
|---------------|--------------|------------------------|-----------------------------|
| id            | BIGSERIAL    | PRIMARY KEY            | Unique identifier           |
| heat_id       | BIGINT       | FK (furnace_heats)     | Parent heat                 |
| item_id       | BIGINT       |                        | Reference to Inventory Item |
| item_name     | VARCHAR(255) | NOT NULL               | Name at time of use         |
| material_type | VARCHAR(20)  | DEFAULT 'RAW_MATERIAL' |                             |
| quantity_used | DOUBLE       | NOT NULL               | Amount consumed             |
| unit_rate     | DOUBLE       |                        | Cost at time of issue       |
| total_cost    | DOUBLE       |                        | quantity * unit_rate        |

### `orders`
Linked table for customer requirements.

| Column       | Type        | Constraints      | Description              |
|--------------|-------------|------------------|--------------------------|
| id           | UUID        | PRIMARY KEY      |                          |
| order_number | VARCHAR(50) | UNIQUE, NOT NULL |                          |
| customer_id  | UUID        | FK (customer)    |                          |
| order_date   | DATE        |                  |                          |
| status       | VARCHAR(30) |                  | e.g., PENDING, COMPLETED |
| order_type   | VARCHAR(20) | NOT NULL         | QUOTATION or DIRECT      |

---

## Summary

The Furnace module bridges the gap between raw material inventory and finished goods production. By automating material issuance and tracking energy efficiency per heat, it provides the foundry with accurate costing and operational insights.
