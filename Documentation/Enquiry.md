# Enquiry Module Documentation

## Overview

The **Enquiry Module** is the first stage of the foundry sales workflow.
It captures casting requirements received from customers before quotations are generated.

An enquiry represents a **customer request for casting parts**, including:

* Part details
* Metal specifications
* Quantity and weight
* Casting process
* Pattern availability
* Machining requirements

This module serves as the **foundation for quotation generation** and later processes like order management, production planning, and billing.

---

# Enquiry Lifecycle

The enquiry follows a controlled lifecycle using the `EnquiryStatus` enum.

| Status      | Description                                 |
| ----------- | ------------------------------------------- |
| **PENDING** | Enquiry created but quotation not generated |
| **QUOTED**  | Quotation created for the enquiry           |
| **CLOSED**  | Enquiry completed or cancelled              |

Default Status:

```
PENDING
```

Automatic transitions:

```
PENDING → QUOTED (when quotation created)
QUOTED → CLOSED (manually or after order completion)
```

---

# Database Schema

## Enquiry Table

Stores the main enquiry information.

| Column                 | Type      | Description                   |
| ---------------------- | --------- | ----------------------------- |
| id                     | UUID      | Primary key                   |
| enquiry_no             | VARCHAR   | Unique enquiry number         |
| enquiry_date           | DATE      | Date of enquiry               |
| customer_id            | UUID      | Customer reference            |
| total_weight_kg        | NUMERIC   | Total casting weight          |
| expected_delivery_date | DATE      | Optional delivery expectation |
| status                 | VARCHAR   | Enquiry lifecycle status      |
| created_at             | TIMESTAMP | Record creation time          |
| updated_at             | TIMESTAMP | Record update time            |
| created_by             | VARCHAR   | User who created the enquiry  |
| updated_by             | VARCHAR   | Last updated by               |

Indexes:

```
idx_enquiry_customer
idx_enquiry_status
idx_enquiry_date
```

---

## Enquiry Item Table

Stores individual parts inside an enquiry.

| Column                       | Type    | Description                 |
| ---------------------------- | ------- | --------------------------- |
| id                           | UUID    | Primary key                 |
| enquiry_id                   | UUID    | Reference to enquiry        |
| part_name                    | VARCHAR | Name of casting part        |
| metal_category               | VARCHAR | Metal category enum         |
| metal_type                   | VARCHAR | Specific metal type enum    |
| required_quantity            | INT     | Number of pieces            |
| approx_piece_weight_kg       | NUMERIC | Approx weight per piece     |
| total_weight_kg              | NUMERIC | Calculated total weight     |
| casting_process              | VARCHAR | Casting method              |
| pattern_provided_by_customer | BOOLEAN | Pattern ownership           |
| pattern_id                   | UUID    | Reference to pattern master |
| pattern_receipt_id           | UUID    | Customer provided pattern   |
| machine_required             | BOOLEAN | Machining requirement       |

---

# Pattern Handling

The system supports two types of patterns.

## 1. Foundry Pattern (System Pattern)

Patterns stored inside the system.

Table: `patterns`

Used when:

```
pattern_provided_by_customer = FALSE
```

Fields:

* name
* type
* material

---

## 2. Customer Provided Pattern

Captured in `pattern_receipt`.

Used when:

```
pattern_provided_by_customer = TRUE
```

Fields:

* inward_date
* outward_date
* pattern name
* pattern type
* pattern material

Constraint ensures only one is used:

```
CHECK (
  pattern_provided_by_customer = TRUE AND pattern_receipt_id IS NOT NULL
  OR
  pattern_provided_by_customer = FALSE AND pattern_id IS NOT NULL
)
```

---

# Enquiry Number Generation

Format:

```
ENQ-{YEAR}-{SEQUENCE}
```

Example:

```
ENQ-2026-00001
ENQ-2026-00002
```

Sequence resets every year.

---

# Weight Calculation

Total item weight:

```
total_weight_kg = approx_piece_weight_kg * required_quantity
```

Total enquiry weight:

```
sum(all enquiry_item.total_weight_kg)
```

---

# Enquiry Creation Workflow

1. Customer selected
2. Parts added
3. Metal specifications defined
4. Pattern information recorded
5. Weight calculated automatically
6. Enquiry saved
7. Status set to `PENDING`

---

# Validation Rules

### Pattern Logic

```
If patternProvidedByCustomer = true
    patternReceipt must exist

If patternProvidedByCustomer = false
    patternId must exist
```

---

### Required Fields

* Customer
* Enquiry date
* Part name
* Metal category
* Metal type
* Quantity
* Piece weight
* Casting process

---

# API Endpoints

## Create Enquiry

```
POST /api/enquiries
```

Creates a new enquiry.

---

## Get All Enquiries

```
GET /api/enquiries?page=0&size=10
```

Returns paginated enquiry list.

---

## Get Enquiry By ID

```
GET /api/enquiries/{id}
```

Returns detailed enquiry including items.

---

## Update Enquiry Status

```
PATCH /api/enquiries/{id}/status
```

Example request:

```
{
  "status": "QUOTED"
}
```

Used to manually change enquiry status if required.

---

# Example Request Payload

```
{
  "customerId": "uuid",
  "enquiryDate": "2026-02-20",
  "enquiryItems": [
    {
      "partName": "Valve Body",
      "metalCategory": "FERROUS",
      "metalType": "CAST_IRON",
      "requiredQuantity": 100,
      "approxPieceWeightKg": 5.5,
      "castingProcess": "SAND_CASTING",
      "patternProvidedByCustomer": true,
      "patternReceipt": {
        "inwardDate": "2026-02-20",
        "outwardDate": "2026-03-01",
        "name": "Customer Valve Pattern",
        "type": "SPLIT_PATTERN",
        "material": "CAST_IRON"
      },
      "machineRequired": true
    }
  ]
}
```

---

# Example Response

```
{
 "id": "uuid",
 "enquiryNo": "ENQ-2026-00001",
 "status": "PENDING",
 "customerName": "JW Industries",
 "totalWeightKg": 550.0,
 "items": [
   {
     "partName": "Valve Body",
     "metalCategory": "FERROUS",
     "metalType": "CAST_IRON",
     "requiredQuantity": 100,
     "approxPieceWeightKg": 5.5,
     "totalWeightKg": 550.0,
     "castingProcess": "SAND_CASTING",
     "patternName": "Customer Valve Pattern",
     "machineRequired": true
   }
 ]
}
```

---

# Security

All enquiry APIs require authentication using JWT.

Roles typically allowed:

```
ADMIN
SALES
```

---

# Future Enhancements

Planned improvements for the enquiry module:

* Enquiry approval workflow
* File attachments (drawings / CAD)
* Pattern inventory tracking
* Enquiry revision history
* Dashboard analytics
* Status change audit logs
* Email notification on enquiry creation

---

# Summary

The Enquiry module provides:

* Structured capture of casting requirements
* Pattern tracking
* Weight calculation
* Status lifecycle management
* Integration point for quotation generation

This module acts as the **starting point of the complete foundry ERP workflow**.

---
