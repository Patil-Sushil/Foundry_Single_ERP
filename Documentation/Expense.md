# Expenses Module – Foundry ERP

## Overview

The **Expenses Module** manages all operational and administrative expenses within the foundry ERP system.
It allows users to record expenses, categorize them under expense heads, and generate structured financial records for reporting and cost tracking.

This module supports both:

* **Predefined Expense Heads** (seeded by the system)
* **Manually created Expense Heads** (created dynamically by users)

Expense numbers are generated automatically in the format:

```
EXP-YYYY-0001
```

Example:

```
EXP-2026-0001
EXP-2026-0002
```

---

# Core Concepts

## Expense Head

An **Expense Head** represents a category of expense.

Examples:

| Expense Head   | Category         |
| -------------- | ---------------- |
| Power & Fuel   | FACTORY_OVERHEAD |
| Furnace Repair | MAINTENANCE      |
| Office Rent    | ADMIN            |
| Freight        | LOGISTICS        |

Each head is stored in the `expense_heads` table.

### Rule

If an expense head with the same **name and category** already exists, the system **reuses it instead of creating a new one**.

---

## Expense

An **Expense** is an individual financial transaction recorded against an expense head.

Examples:

| Expense Number | Expense Head        | Amount  |
| -------------- | ------------------- | ------- |
| EXP-2026-0001  | Power & Fuel        | ₹12,000 |
| EXP-2026-0002  | Machine Calibration | ₹3,000  |

Expenses are stored in the `expenses` table.

---

# Database Structure

## expense_heads

| Field       | Type      | Description          |
| ----------- | --------- | -------------------- |
| id          | UUID      | Primary Key          |
| name        | VARCHAR   | Expense head name    |
| category    | VARCHAR   | Expense category     |
| description | TEXT      | Optional description |
| created_at  | TIMESTAMP | Creation timestamp   |
| updated_at  | TIMESTAMP | Last update time     |

Unique constraint:

```
(name, category)
```

---

## expenses

| Field            | Type      | Description           |
| ---------------- | --------- | --------------------- |
| id               | UUID      | Primary Key           |
| expense_number   | VARCHAR   | Unique expense number |
| expense_head_id  | UUID      | Linked expense head   |
| amount           | DECIMAL   | Expense amount        |
| expense_date     | DATE      | Expense date          |
| payment_mode     | VARCHAR   | Payment method        |
| reference_number | VARCHAR   | Transaction reference |
| remarks          | TEXT      | Additional remarks    |
| created_at       | TIMESTAMP | Creation time         |

Relationship:

```
expenses.expense_head_id → expense_heads.id
```

---

# Expense Categories

Typical categories used in foundry operations:

| Category         | Description                 |
| ---------------- | --------------------------- |
| FACTORY_OVERHEAD | Power, fuel, utilities      |
| MAINTENANCE      | Machinery or furnace repair |
| QUALITY          | Testing and inspection      |
| PACKING          | Packaging materials         |
| FREIGHT          | Transport and logistics     |
| ADMIN            | Office expenses             |
| SALES            | Marketing and commissions   |
| FINANCE          | Bank charges or interest    |
| OTHER            | Miscellaneous expenses      |

---

# Payment Modes

Supported payment methods:

| Mode          |
| ------------- |
| CASH          |
| BANK_TRANSFER |
| UPI           |
| CARD          |

---

# Expense Creation Flow

### Step 1 — Select or Enter Expense Head

User can either:

* Choose an existing head from dropdown
* Enter a new expense head name

---

### Step 2 — System Validation

System checks:

```
(name + category)
```

If exists → reuse head
If not exists → create new head

---

### Step 3 — Generate Expense Number

Automatically generated:

```
EXP-YYYY-XXXX
```

Example:

```
EXP-2026-0001
```

---

### Step 4 — Save Expense

Expense record is stored in the database.

---

# API Endpoints

## Create Expense

```
POST /api/expenses
```

### Request Example

```json
{
  "expenseHeadName": "Machine Calibration",
  "category": "MAINTENANCE",
  "amount": 3000,
  "expenseDate": "2026-03-10",
  "paymentMode": "CASH",
  "referenceNumber": "TXN123",
  "remarks": "Calibration service"
}
```

### Response

```json
{
  "success": true,
  "data": {
    "expenseNumber": "EXP-2026-0001",
    "expenseHeadName": "Machine Calibration",
    "amount": 3000
  }
}
```

---

## Get Expense By ID

```
GET /api/expenses/{id}
```

Returns a single expense record.

---

## Get All Expenses

```
GET /api/expenses
```

Returns all recorded expenses.

---

## Get Expense Heads

```
GET /api/expense-heads
```

Used for frontend dropdown selection.

---

# Example Scenarios

### Scenario 1 — New Head

Request:

```
Machine Calibration
Category: MAINTENANCE
```

Result:

```
New expense head created
```

---

### Scenario 2 — Same Head

Request:

```
Machine Calibration
Category: MAINTENANCE
```

Result:

```
Existing head reused
```

---

### Scenario 3 — Same Name Different Category

Request:

```
Machine Calibration
Category: QUALITY
```

Result:

```
New expense head created
```

---

# Reporting Use Cases

The expenses module enables reporting such as:

### Monthly Expense Report

```
GET /api/expenses?from=2026-03-01&to=2026-03-31
```

### Category Summary

Example output:

```
Power & Fuel → ₹4,20,000
Maintenance → ₹1,10,000
Admin → ₹75,000
```

---

# Best Practices

1. Always reuse existing expense heads.
2. Normalize head names using `trim()`.
3. Use category-based reporting for financial insights.
4. Avoid storing duplicate heads.
5. Validate amount and expense date.

---

# Future Enhancements

Recommended features for enterprise ERP:

* Expense attachments (bill uploads)
* Monthly expense dashboards
* Budget control for expense categories
* Cost allocation to production or furnace
* Vendor-linked expenses

---

# Module Status

| Feature                  | Status      |
| ------------------------ | ----------- |
| Expense creation         | Implemented |
| Expense head reuse       | Implemented |
| Automatic numbering      | Implemented |
| Expense APIs             | Implemented |
| Reporting support        | Ready       |
| ERP production readiness | Complete    |

---
