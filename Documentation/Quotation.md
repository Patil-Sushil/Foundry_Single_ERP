# Quotation Module

## Foundry ERP System

**Module:** Quotation Management
**Technology:** Spring Boot, PostgreSQL, JPA, Flyway

---

# 1. Overview

The **Quotation Module** manages the creation, modification, tracking, and delivery of quotations to customers within the Foundry ERP system.

It allows the organization to:

* Generate quotations from customer enquiries
* Create direct quotations without enquiries
* Add multiple casting items
* Calculate casting cost automatically
* Generate professional quotation PDFs
* Send quotations via email
* Track quotation lifecycle status
* Maintain quotation revision history

This module integrates with:

* Customer Module
* Enquiry Module
* Email Service
* PDF Generation Service
* Authentication System

---

# 2. Business Workflow

### Standard Foundry Sales Flow

```
Customer
   ↓
Enquiry
   ↓
Quotation
   ↓
Send to Customer
   ↓
Customer Response
   ↓
Approved / Rejected
```

### Direct Quotation Flow

```
Customer
   ↓
Direct Quotation
   ↓
Send to Customer
```

---

# 3. Key Features

## 3.1 Create Quotation

A quotation can be created in two ways:

1. **From Enquiry**
2. **Directly for a Customer**

### API

```
POST /api/quotations
```

### Rules

* Customer must exist
* Quotation must contain at least one item
* Only one quotation can be created per enquiry

---

## 3.2 Quotation Items

Each quotation can contain multiple casting parts.

### Item Fields

| Field         | Description          |
| ------------- | -------------------- |
| partName      | Casting part name    |
| drawingNumber | Drawing reference    |
| materialGrade | Material grade       |
| netWeightKg   | Net casting weight   |
| grossWeightKg | Gross casting weight |
| patternStatus | Pattern availability |
| quantity      | Required quantity    |
| unitPrice     | Price per kilogram   |

---

# 4. Casting Cost Calculation

Foundry quotations calculate cost based on casting weight.

### Formula

```
Line Total = Net Weight × Rate per KG × Quantity
```

### Example

```
Net Weight = 12.5 kg
Rate = ₹250 per kg
Quantity = 100

Total = 12.5 × 250 × 100
Total = ₹312,500
```

---

# 5. Quotation Totals

When quotation items are added:

```
Sub Total = Sum of all item totals
Total Amount = Sub Total - Discount + Tax
```

Currently:

```
Total Amount = Sub Total
```

---

# 6. Quotation Status Lifecycle

The system tracks quotation lifecycle stages.

| Status   | Description                    |
| -------- | ------------------------------ |
| DRAFT    | Quotation created but not sent |
| SENT     | Quotation sent to customer     |
| APPROVED | Customer approved quotation    |
| REJECTED | Customer rejected quotation    |

### Allowed Status Transitions

```
DRAFT → SENT
SENT → APPROVED
SENT → REJECTED
```

Invalid transitions are blocked.

Example:

```
DRAFT → APPROVED ❌
```

---

# 7. Revision Handling

When a quotation is updated after being sent:

```
Status → DRAFT
Revision Number +1
sent_at → reset
```

This ensures the customer always receives the **latest version of the quotation**.

---

# 8. Email Integration

Quotations can be sent to customers via email.

### API

```
POST /api/quotations/{id}/send-email
```

### Behavior

When email is sent:

```
Status → SENT
sent_at → current timestamp
```

The email contains:

* Quotation reference
* Customer information
* Attached PDF quotation document

---

# 9. PDF Generation

The system generates a professional quotation document.

The generated PDF includes:

* Company Logo
* Company Details
* Customer Information
* Quotation Information
* Item Table
* Casting Cost Calculation
* Total Amount
* Terms and Conditions
* Digital Signature

---

# 10. Lifecycle Tracking

The system tracks important timestamps.

| Field            | Purpose                     |
| ---------------- | --------------------------- |
| sent_at          | When quotation was sent     |
| approved_at      | When quotation was approved |
| rejected_at      | When quotation was rejected |
| rejection_reason | Reason for rejection        |
| viewed_at        | When quotation was viewed   |

---

# 11. Database Structure

## quotations

Stores main quotation information.

Important fields:

```
quotation_number
customer_id
enquiry_id
status
sub_total
total_amount
valid_until
payment_terms
delivery_terms
```

---

## quotation_items

Stores individual casting items.

Fields include:

```
part_name
drawing_number
material_grade
net_weight_kg
quantity
unit_price
line_total
```

---

# 12. Performance Optimizations

Indexes are created for faster lookup.

```
idx_quotations_customer
idx_quotations_enquiry
idx_quotations_status
idx_quotations_date
idx_quotations_number
idx_quotation_items_quotation
```

Lazy loading and entity graph optimizations are used to prevent **N+1 query issues**.

---

# 13. Security and Auditing

User actions are tracked using audit fields.

```
created_by
updated_by
created_at
updated_at
```

User identity is obtained from:

```
SecurityUtils.getCurrentUsername()
```

---

# 14. API Endpoints

| Method | Endpoint                        | Description             |
| ------ | ------------------------------- | ----------------------- |
| POST   | /api/quotations                 | Create quotation        |
| GET    | /api/quotations/{id}            | Get quotation           |
| GET    | /api/quotations                 | List quotations         |
| PUT    | /api/quotations/{id}            | Update quotation        |
| PATCH  | /api/quotations/{id}/status     | Update quotation status |
| POST   | /api/quotations/{id}/send-email | Send quotation email    |

---

# 15. Validation Rules

The system enforces the following rules:

* Customer must exist
* Quotation must contain items
* Only one quotation per enquiry
* Approved quotations cannot be modified
* Only DRAFT quotations can be sent
* Customer email must exist to send quotation

---

# 16. Module Integration

The quotation module interacts with:

* Customer Module
* Enquiry Module
* Authentication System
* Email Service
* PDF Generation Service

---

# 17. Future Enhancements

Possible future improvements:

* Automatic quotation expiry
* Customer quotation portal
* WhatsApp quotation sending
* Approval workflow
* Multi-currency support
* Quotation analytics dashboard

---

# 18. Next ERP Module

The next module in the Foundry ERP workflow is:

```
Sales Order Module
```

ERP lifecycle:

```
Customer
   ↓
Enquiry
   ↓
Quotation
   ↓
Sales Order
   ↓
Production
   ↓
Dispatch
   ↓
Invoice
```
