# Billing Module Documentation

## Overview

The **Billing Module** in the Foundry ERP system manages the complete dispatch and billing workflow after an order is produced.
It handles the creation of **Delivery Challans**, generation of **Invoices**, GST calculations, and sending documents to customers via email.

The billing process ensures accurate tracking of dispatched quantities, invoice generation based on casting weight, and proper GST handling based on the customer's state.

---

# Billing Workflow

```
Create Order
      ↓
Send Order Confirmation Email
      ↓
Create Delivery Challan (DC)
      ↓
Generate Delivery Challan PDF
      ↓
Send Delivery Challan Email
      ↓
Generate Invoice
      ↓
Generate Invoice PDF
      ↓
Send Invoice Email
```

---

# Delivery Challan

## Purpose

A **Delivery Challan (DC)** is generated when finished castings are ready to be dispatched to the customer.

One order can have **multiple delivery challans**.

Example:

```
Order → ORD-2026-00001

DC-2026-00001
DC-2026-00002
DC-2026-00003
```

This supports **partial dispatch** of orders.

---

# Delivery Challan Fields

| Field         | Description                     |
| ------------- | ------------------------------- |
| dcNumber      | Unique delivery challan number  |
| order         | Associated order                |
| customer      | Customer receiving dispatch     |
| dispatchDate  | Date of dispatch                |
| vehicleNumber | Transport vehicle number        |
| transportName | Transport company name          |
| lrNumber      | Logistics receipt number        |
| totalQuantity | Total dispatched quantity       |
| totalWeight   | Total casting weight            |
| totalAmount   | Total value of dispatched items |
| status        | CREATED / DISPATCHED            |

---

# Delivery Challan Item

Each DC contains one or more items.

| Field     | Description             |
| --------- | ----------------------- |
| orderItem | Reference to order item |
| quantity  | Quantity dispatched     |
| weight    | Total casting weight    |
| rate      | Rate per kg             |
| amount    | Calculated amount       |

### Amount Calculation

```
Amount = Weight × Rate
```

Example:

```
Weight = 40 kg
Rate = ₹20 per kg

Amount = 40 × 20 = ₹800
```

---

# Dispatch Validation

The system ensures that dispatched quantity does not exceed the ordered quantity.

Example:

```
Order Quantity = 100

DC1 = 40
DC2 = 30
DC3 = 30

Total = 100
```

If a dispatch exceeds remaining quantity, the system throws an error.

---

# Order Completion Logic

When the total dispatched quantity equals the ordered quantity:

1. Order status becomes **COMPLETED**
2. The **pattern status becomes AVAILABLE**
3. Pattern can be used for another order

---

# Delivery Challan PDF

The system automatically generates a **PDF delivery challan** containing:

* DC number
* Customer details
* Dispatch date
* Transport information
* Casting items
* Quantity and weight details
* Total dispatch summary

This document is used for:

* Dispatch packaging
* Transport verification
* Customer record

---

# Invoice

## Purpose

An **Invoice** is generated after dispatch to bill the customer.

Invoice generation uses the **Delivery Challan as its source**.

---

# Invoice Fields

| Field           | Description             |
| --------------- | ----------------------- |
| invoiceNumber   | Unique invoice number   |
| deliveryChallan | Linked delivery challan |
| vehicleNumber   | Transport vehicle       |
| subtotal        | Total before GST        |
| cgst            | Central GST             |
| sgst            | State GST               |
| igst            | Integrated GST          |
| gstPercentage   | GST rate                |
| totalAmount     | Final invoice amount    |
| invoiceDate     | Date of invoice         |
| dueDate         | Payment due date        |
| billStatus      | PAID / UNPAID           |

---

# GST Logic

GST is automatically calculated based on the **customer's state**.

## Maharashtra Customer

```
CGST = 9%
SGST = 9%
```

Example:

```
Subtotal = ₹10,000

CGST = 900
SGST = 900

Total = ₹11,800
```

---

## Other State Customer

```
IGST = 18%
```

Example:

```
Subtotal = ₹10,000

IGST = ₹1,800

Total = ₹11,800
```

---

# Invoice Number Format

Invoices follow an industry standard format.

```
INV-YYYY-00001
```

Example:

```
INV-2026-00001
INV-2026-00002
INV-2026-00003
```

---

# Bill Status

Invoices support two payment states.

| Status | Description       |
| ------ | ----------------- |
| PAID   | Payment completed |
| UNPAID | Payment pending   |

If the bill status is **UNPAID**, the system requires a **due date**.

---

# Invoice PDF

The system automatically generates a **GST-compliant invoice PDF** containing:

* Invoice number
* Customer details
* GST numbers
* Delivery challan reference
* Item summary
* GST breakdown
* Total payable amount

---

# Email Integration

The system automatically sends documents to customers.

### Order Creation

Customer receives **order confirmation email**.

### Delivery Challan Creation

Customer receives **dispatch notification email** with DC PDF.

### Invoice Generation

Customer receives **invoice email** with invoice PDF.

---

# API Endpoints

## Delivery Challan APIs

```
POST   /api/delivery-challans
GET    /api/delivery-challans/{id}
GET    /api/delivery-challans
PATCH  /api/delivery-challans/{id}/dispatch
GET    /api/delivery-challans/{id}/pdf
```

---

## Invoice APIs

```
POST   /api/invoices
GET    /api/invoices/{id}
GET    /api/invoices/{id}/pdf
```

---

# Key Features

* Weight-based billing
* Multiple DCs per order
* Automatic GST calculation
* Delivery challan generation
* Invoice generation
* PDF document creation
* Email notifications
* Dispatch quantity validation
* Automatic order completion detection
* Pattern availability management

---

# Future Enhancements

Possible improvements for the billing module:

* Payment tracking
* Partial invoice payments
* Outstanding balance management
* Payment reminders
* Accounting integration
* GST return export

---

# Conclusion

The Billing Module provides a complete dispatch and billing system for the Foundry ERP.
It ensures accurate tracking of dispatch quantities, GST-compliant invoice generation, and automated communication with customers.
