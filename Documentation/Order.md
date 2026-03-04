# Order Module Documentation

## Overview

The **Order Module** is responsible for managing customer orders in the Foundry ERP system. It supports both **quotation-based orders** and **direct customer orders**, enabling flexible sales workflows suitable for manufacturing environments.

This module integrates with the **Customer**, **Quotation**, and **Production (future Job Card)** modules and provides complete lifecycle management of orders including creation, status updates, filtering, and detailed retrieval.

---

# Key Capabilities

### 1. Dual Order Creation Support

The system supports two types of order creation:

#### Quotation-Based Orders

Orders can be created directly from an **approved quotation**.
In this flow:

* The quotation must have status **APPROVED**
* Order items are automatically derived from quotation items
* The quotation can only generate **one order**

#### Direct Orders

Orders can also be created **without a quotation**.
In this scenario:

* The order is created directly for a customer
* Order items must be provided manually
* Total amount is calculated automatically from item totals

---

# Order Type Classification

An `OrderType` enum was introduced to explicitly classify orders.

```java
public enum OrderType {
    QUOTATION,
    DIRECT
}
```

This improves clarity, reporting, and future workflow control.

---

# Order Status Workflow

Orders follow a controlled lifecycle using the `OrderStatus` enum:

```java
public enum OrderStatus {
    CREATED,
    CONFIRMED,
    IN_PRODUCTION,
    READY_FOR_DISPATCH,
    DISPATCHED,
    COMPLETED,
    CANCELLED
}
```

A **status transition validator** ensures that invalid transitions cannot occur.

Example:

* Completed orders cannot change status
* Cancelled orders cannot move back to active states

---

# Entity Design

## Order Entity

The `Order` entity represents a customer order and includes:

* Unique Order Number
* Customer reference
* Optional quotation reference
* Order type
* Order and delivery dates
* Current status
* Total order amount
* Associated order items
* Audit fields (created/updated information)

Key relationships:

* **Many-to-One → Customer**
* **Many-to-One → Quotation (optional)**
* **One-to-Many → OrderItems**

---

## OrderItem Entity

Each order contains multiple order items representing the individual casting parts.

Fields include:

* Product Name
* Metal Type
* Quantity
* Unit Price
* Total Price

Items are automatically populated when an order is created from a quotation.

---

# DTO Layer

The module uses a clean **DTO-based architecture** to separate API responses from entity models.

### Request DTOs

* `OrderCreateRequest`
* `OrderItemRequest`

### Response DTOs

* `OrderResponse`
* `OrderItemResponse`
* `CustomerSummary`
* `QuotationSummary`

This structure ensures:

* Secure API exposure
* Controlled data responses
* Flexible response shaping

---

# Order Creation Logic

## From Quotation

Steps performed:

1. Validate quotation exists
2. Ensure quotation status is **APPROVED**
3. Ensure no existing order for the quotation
4. Convert quotation items to order items
5. Generate order number
6. Persist order and items

---

## Direct Order

Steps performed:

1. Validate customer exists
2. Validate order items are provided
3. Calculate total price per item
4. Calculate order total
5. Persist order and items

---

# Order Number Generation

Orders are assigned unique numbers using the format:

```
ORD-YYYY-XXXXX
```

Example:

```
ORD-2026-00001
```

The number is generated dynamically based on the current year and sequence.

---

# Data Retrieval

## Fetch Order by ID

Orders can be retrieved with full details including:

* Customer information
* Quotation information (if available)
* Order items
* Order totals
* Audit metadata

To prevent lazy loading issues, the repository uses **fetch joins** to load related data.

---

# Pagination and Filtering

Orders can be retrieved with pagination and dynamic filtering.

Supported filters include:

* Order Status
* Customer ID
* Date Range (From / To)

This functionality is implemented using **Spring JPA Specifications**.

---

# Validation and Error Handling

The module integrates with the global exception framework and provides validation for:

* Missing quotation
* Missing customer
* Invalid quotation status
* Duplicate order creation from quotation
* Invalid status transitions
* Missing order items for direct orders

---

# Database Design

Key tables created via Flyway migrations:

### orders

Stores order-level information.

Important columns:

* id (UUID)
* order_number
* customer_id
* quotation_id (nullable)
* order_type
* order_date
* delivery_date
* status
* total_amount

---

### order_item

Stores individual line items for each order.

Important columns:

* id
* order_id
* product_name
* metal_type
* quantity
* unit_price
* total_price

---

# Auditing Support

The module uses the shared `BaseEntity` which provides:

* UUID primary key
* Created timestamp
* Updated timestamp
* Created by user
* Updated by user

This ensures all order records maintain proper audit history.

---

# Logging

All major operations include structured logging using `SLF4J`:

* Order creation
* Status updates
* Direct order processing
* Quotation-based conversion

This improves debugging and operational monitoring.

---

# Architectural Pattern

The module follows a clean layered architecture:

```
Controller
   ↓
Service
   ↓
Repository
   ↓
Entity
```

Supporting components include:

* DTO layer
* Mapper layer
* Specification filtering
* Validation utilities

---

# Future Enhancements

The Order module is designed to integrate with upcoming modules including:

* Job Card (Production)
* Dispatch Management
* Invoice & GST
* Payment Tracking
* Inventory Reservation
* Order History Audit

---

# Summary

The Order module provides a robust foundation for managing customer orders within the Foundry ERP system. It supports both quotation-driven and direct order workflows while maintaining strong validation, clean architecture, and extensibility for future manufacturing processes.
