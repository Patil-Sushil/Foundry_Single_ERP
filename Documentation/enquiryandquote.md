# Enquiry and Quotation Module: Integrated Guide

This document provides a comprehensive overview of the **Enquiry** and **Quotation** modules, their technical relationship, workflows, and database structures in the Foundry ERP.

---

## 1. Module Relationship & Workflow

The Enquiry and Quotation modules form the "Pre-Sales" phase of the foundry's operational lifecycle.

### 1.1 Standard Workflow
1.  **Enquiry**: A customer submits a request for one or more casting parts. This is recorded in the `enquiry` table.
2.  **Items**: Specific part requirements (weight, grade, quantity, process) are recorded as `enquiry_item` records.
3.  **Quotation Link**: A salesperson creates a `quotation` for the customer. If an Enquiry exists, they link it via `enquiry_id`.
4.  **Auto-Population**: When linked, the system pulls all `enquiry_item` details into the `quotation_items` table, allowing the salesperson to focus on pricing (`unit_price`) and terms.
5.  **Status Sync**: Upon saving a quotation linked to an enquiry, the enquiry's status automatically moves from `PENDING` to `QUOTED`.
6.  **Review**: The quotation is sent to the customer (`SENT`). If changes are needed, it's revised (`REVISED`, `revision_no` incremented).
7.  **Approval**: Once the customer accepts, the quotation is `APPROVED`, which typically leads to the creation of a Sales Order.

### 1.2 Direct Quotation Workflow
The system also supports creating a quotation directly for a customer without a preceding enquiry. In this case, all part details must be entered manually into the quotation.

---

## 2. Database Table Structures

### 2.1 Enquiry Tables

#### Table: `enquiry`
Header table for customer enquiries.

| Column                   | Type          | Description                              |
|:-------------------------|:--------------|:-----------------------------------------|
| `id`                     | UUID          | Primary Key                              |
| `enquiry_no`             | VARCHAR(50)   | Unique reference (e.g., ENQ-2024-001)    |
| `enquiry_date`           | DATE          | Date the enquiry was received            |
| `customer_id`            | UUID          | Link to the `customer` table             |
| `total_weight_kg`        | NUMERIC(12,3) | Total weight of all items in the enquiry |
| `expected_delivery_date` | DATE          | Optional requested delivery date         |
| `status`                 | VARCHAR(20)   | `PENDING`, `REVISED`, `QUOTED`, `CLOSED` |
| `revision_no`            | INTEGER       | Version number (starts at 0)             |
| `revision_note`          | TEXT          | Optional comment for audit               |
| `created_at`             | TIMESTAMP     | Record creation timestamp                |
| `created_by`             | VARCHAR(255)  | User who created the record              |

#### Table: `enquiry_item`
Detailed part specifications for an enquiry.

| Column                   | Type          | Description                                   |
|:-------------------------|:--------------|:----------------------------------------------|
| `id`                     | UUID          | Primary Key                                   |
| `enquiry_id`             | UUID          | Foreign Key to `enquiry`                      |
| `part_name`              | VARCHAR(150)  | Name of the part to be cast                   |
| `metal_category`         | VARCHAR(50)   | `FERROUS` or `NON_FERROUS`                    |
| `material_grade`         | VARCHAR(100)  | Specific grade (e.g., FG260, SG500/7)         |
| `metal_type`             | VARCHAR(50)   | Specific metal type                           |
| `required_quantity`      | INT           | Number of pieces                              |
| `approx_piece_weight_kg` | NUMERIC(10,3) | Estimated weight per piece                    |
| `total_weight_kg`        | NUMERIC(12,3) | `required_quantity * approx_piece_weight_kg`  |
| `casting_process_id`    | UUID          | Link to `casting_processes` master table      |
| `pattern_provided_by`    | VARCHAR(20)   | `CUSTOMER` or `COMPANY`                       |
| `machine_required`       | BOOLEAN       | Indicates if post-casting machining is needed |

---

### 2.2 Quotation Tables

#### Table: `quotations`
Header table for formal price proposals.

| Column             | Type          | Description                                                    |
|:-------------------|:--------------|:---------------------------------------------------------------|
| `id`               | UUID          | Primary Key                                                    |
| `quotation_number` | VARCHAR(50)   | Unique reference (e.g., QUO-2024-001)                          |
| `quotation_date`   | DATE          | Date of the quote                                              |
| `valid_until`      | DATE          | Expiry date of the offer                                       |
| `revision_no`      | INTEGER       | Version number (starts at 0)                                   |
| `customer_id`      | UUID          | Link to the `customer` table                                   |
| `enquiry_id`       | UUID          | Optional: Link to the `enquiry` table                          |
| `status`           | VARCHAR(20)   | `DRAFT`, `SENT`, `REVISED`, `APPROVED`, `CANCELLED`, `EXPIRED` |
| `sub_total`        | DECIMAL(19,2) | Sum of all line items                                          |
| `discount`         | DECIMAL(19,2) | Total discount amount                                          |
| `tax`              | DECIMAL(19,2) | Total tax amount                                               |
| `total_amount`     | DECIMAL(19,2) | Final payable amount                                           |
| `payment_terms`    | VARCHAR(500)  | e.g., Net 30, Advance                                          |
| `delivery_terms`   | VARCHAR(500)  | e.g., Ex-Works, FOR                                            |
| `currency`         | VARCHAR(10)   | Defaults to `INR`                                              |
| `sent_at`          | TIMESTAMP     | Timestamp when sent to customer                                |
| `approved_at`      | TIMESTAMP     | Timestamp when approved                                        |

#### Table: `quotation_items`
Detailed pricing for each part in a quotation.

| Column               | Type          | Description                                              |
|:---------------------|:--------------|:---------------------------------------------------------|
| `id`                 | UUID          | Primary Key                                              |
| `quotation_id`       | UUID          | Foreign Key to `quotations`                              |
| `part_name`          | VARCHAR(255)  | Name of the part                                         |
| `drawing_number`     | VARCHAR(100)  | Customer drawing reference                               |
| `material_grade`     | VARCHAR(100)  | Material grade                                           |
| `metal_type`         | VARCHAR(50)   | Metal type                                               |
| `casting_process_id` | UUID          | Link to `casting_processes` master table                 |
| `net_weight_kg`      | DECIMAL(10,3) | Actual net weight of the casting                         |
| `quantity`           | INTEGER       | Number of pieces                                         |
| `unit_price`         | DECIMAL(19,2) | Price per piece (or KG based on business rule)           |
| `line_total`         | DECIMAL(19,2) | `quantity * unit_price`                                  |
| `pattern_status`     | VARCHAR(20)   | `AVAILABLE`, `TO_BE_MADE`, `CUSTOMER_SUPPLY`             |
| `pattern_id`         | UUID          | Link to `patterns` table (if company pattern used)       |
| `pattern_receipt_id` | UUID          | Link to `pattern_receipt` (if customer pattern received) |

---

## 3. Key Business Logic

### 3.1 Pattern Handling
The link between Quotations and Patterns is crucial:
- If `pattern_provided_by` is `COMPANY`, the system looks for a record in the `patterns` master table.
- If `pattern_provided_by` is `CUSTOMER`, the system tracks the physical equipment via the `pattern_receipt` table.

### 3.2 Total Calculation
In the `QuotationServiceImpl`, every time an item is added or updated, the system triggers `recalculateTotals()`:
- `Sub Total` = $\sum (line\_total)$
- `Total Amount` = `Sub Total` (Discounts and Taxes are applied based on customer configuration).

### 3.3 Status Transitions
- A quotation in `DRAFT` or `REVISED` can be sent via email.
- Sending an email automatically moves the status to `SENT`.
- Only a `SENT` quotation can be `APPROVED` or `CANCELLED`.
- Updating a `SENT` quotation resets it to `DRAFT` (or `REVISED`) and resets the `sent_at` timestamp.

---

## 4. Enquiry Revision Flow

When a customer revises their requirements (e.g., changes quantities or specifications) after an initial enquiry has been recorded, the system supports a **Revision Flow**.

### 4.1 Purpose
- **Audit Trail**: Maintain a history of changes to the original customer request.
- **Traceability**: Track why a quotation might have changed between revisions.

### 4.2 Revision Logic
1.  **Endpoint**: `PATCH /api/enquiries/{id}/revise`
2.  **Logic**:
    - The system fetches the latest revision of the enquiry.
    - It increments the `revision_no`.
    - It replaces the old items with the new specifications provided in the request.
    - It sets the status back to `REVISED`.
    - It stores an optional `revision_note` explaining the change.

### 4.3 Quotation Impact
**CRITICAL**: Quotations are **NOT** automatically created or revised when an enquiry is revised.
- The salesperson must **manually** create a new quotation (or a new revision of an existing quotation) from the latest revised enquiry.
- This gives the sales team full control over pricing, discounts, and terms which might change due to the revised enquiry (e.g., volume discounts for increased quantity).

### 4.4 Example Request (Revise Enquiry)
**PATCH** `/api/enquiries/{enquiry_id}/revise`

```json
{
  "revisionNote": "Customer increased quantity from 500 to 1000 and changed material grade to SG500/7.",
  "items": [
    {
      "partName": "Gear Housing",
      "metalType": "SG_IRON",
      "materialGrade": "SG500/7",
      "castingProcessId": "uuid",
      "requiredQuantity": 1000,
      "approxPieceWeightKg": 12.5,
      "machineRequired": true,
      "patternProvidedBy": "CUSTOMER"
    }
  ]
}
```

**Example Response**
```json
{
  "success": true,
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "enquiryNo": "ENQ-2024-00123",
    "revisionNo": 1,
    "revisionNote": "Customer increased quantity from 500 to 1000 and changed material grade to SG500/7.",
    "status": "REVISED",
    "totalWeightKg": 12500.0,
    "items": [...]
  }
}
```

