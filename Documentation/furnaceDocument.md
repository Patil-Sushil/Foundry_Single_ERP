# Furnace Module Documentation

## Overview

The **Furnace Module** is a critical part of the production tracking system in the Foundry ERP. It manages daily furnace operation reports, individual melting cycles (heats), chemical composition tracking, energy consumption analysis, and automatic material issuance from inventory.

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
- **Temperature & Timing**:
    - `pouringTemp`: The temperature of the metal when poured.
    - `pouringStartTime` & `pouringEndTime`: Duration of the pouring process.

### Reference to Orders
A heat can be optionally linked to a specific **Customer Order** (`order_id`). This allows tracking which production batches (heats) were used to fulfill which customer requirements.

---

## 3. Heat Material Items

Every heat consumes raw materials (Pig Iron, Scrap, Ferro-Alloys, etc.). These are tracked in the `heat_material_items` table.

### Fields
- **Item ID & Name**: Reference to the inventory item.
- **Material Type**: Classified as `RAW_MATERIAL`, `INOCULANT`, `NODULIZER`, etc.
- **Quantity Used**: The amount of material consumed.
- **Unit Rate & Total Cost**: Captured at the time of usage based on the inventory's **Weighted Average Cost (WAC)**.

---

## 4. Inventory Integration (Automatic Issuance)

The Furnace module is tightly integrated with the **Inventory Module**.

### The Issuance Workflow
1. When a Heat is saved, the system identifies all materials used.
2. It validates if enough stock is available in the inventory.
3. It automatically records a **Material Issue** in the inventory system for the `FURNACE` department.
4. The `Item.currentStock` is decreased, and the consumption value is recorded.

### Updates and Reversals
- **Updates**: If a heat is updated with different material quantities, the system calculates the "delta" and adjusts the inventory (either issuing more or returning stock).
- **Deletions**: If a heat or report is deleted, all material issuances are reversed, and stock is returned to the inventory.

---

## 5. API Endpoints

### Furnace Reports
- `POST /api/furnace-reports`: Create a new daily report with multiple heats.
- `GET /api/furnace-reports/{id}`: Get report details.
- `PUT /api/furnace-reports/{id}`: Update report and its heats.
- `GET /api/furnace-reports/ref/{refNo}`: Find by reference number.

### Furnace Heats
- `GET /api/furnace-heats/report/{reportId}`: List all heats for a specific report.
- `GET /api/furnace-heats/order/{orderId}`: List all heats associated with a specific customer order.

---

## 6. Business Logic & Calculations

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

## 7. Database Schema

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
