# Payment Module Documentation

## Overview

The **Payment Module** is part of the **Accounts system** in the Foundry ERP.
It manages **customer payments against invoices**, maintains **payment history**, prevents **overpayments**, updates **invoice status**, and sends **email notifications** to customers when payments are received.

This module supports **multiple payments for a single invoice**, allowing partial payments until the invoice is fully settled.

---

# Business Flow

```
Customer
   ↓
Order
   ↓
Delivery Challan
   ↓
Invoice
   ↓
Payment (Multiple allowed)
```

Example:

```
Invoice Total = 10000

Payment 1 = 4000
Payment 2 = 3000
Payment 3 = 3000
```

Result:

```
Total Paid = 10000
Remaining = 0
Invoice Status = PAID
```

---

# Module Folder Structure

```
accounts
│
├── controller
│      PaymentController.java
│
├── dto
│   ├── request
│   │      PaymentCreateRequest.java
│   │
│   └── response
│          PaymentResponse.java
│          PaymentSummaryResponse.java
│
├── entity
│   ├── enums
│   │      PaymentMethod.java
│   │      PaymentStatus.java
│   │
│   └── Payment.java
│
├── repository
│      PaymentRepository.java
│
├── service
│   ├── impl
│   │      PaymentServiceImpl.java
│   │
│   └── PaymentService.java
│
├── mapper
│      PaymentMapper.java
│
└── util
       PaymentNumberGenerator.java
```

---

# Database Design

## Table: payments

| Column           | Type      | Description               |
| ---------------- | --------- | ------------------------- |
| id               | UUID      | Primary key               |
| payment_number   | VARCHAR   | Unique payment identifier |
| invoice_id       | UUID      | Related invoice           |
| customer_id      | UUID      | Customer making payment   |
| payment_date     | DATE      | Date of payment           |
| payment_method   | ENUM      | Payment mode              |
| amount_paid      | DECIMAL   | Amount paid               |
| reference_number | VARCHAR   | Transaction reference     |
| remarks          | TEXT      | Additional notes          |
| status           | ENUM      | Payment status            |
| created_at       | TIMESTAMP | Record creation time      |
| updated_at       | TIMESTAMP | Record update time        |

---

# Entity Relationships

```
Invoice (1)
   ↓
Payment (Many)

Customer (1)
   ↓
Payment (Many)
```

This allows **multiple payments per invoice**.

---

# Enums

## PaymentMethod

```
CASH
UPI
BANK_TRANSFER
CHEQUE
CARD
```

## PaymentStatus

```
SUCCESS
FAILED
REFUNDED
```

---

# MapStruct Mapping

MapStruct is used to convert **Entity → DTO** automatically.

Example mapper:

```
@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(source = "invoice.id", target = "invoiceId")
    PaymentResponse toResponse(Payment payment);

}
```

Benefits:

* No manual mapping
* Cleaner code
* Compile-time safety
* Better performance

---

# API Endpoints

## Create Payment

```
POST /api/payments
```

Request:

```
{
  "invoiceId": "uuid",
  "amountPaid": 4000,
  "paymentMethod": "UPI",
  "referenceNumber": "TXN12345",
  "remarks": "Partial payment"
}
```

Response:

```
{
  "success": true,
  "data": {
    "paymentNumber": "PAY-2026-cc140d7c",
    "invoiceId": "uuid",
    "amountPaid": 4000,
    "paymentMethod": "UPI",
    "status": "SUCCESS"
  }
}
```

---

## Get Payment By ID

```
GET /api/payments/{id}
```

---

## Get Payments By Invoice

```
GET /api/payments/invoice/{invoiceId}
```

Returns **all payment history for the invoice**.

Example response:

```
[
  {
    "paymentNumber": "PAY-2026-1",
    "amountPaid": 4000
  },
  {
    "paymentNumber": "PAY-2026-2",
    "amountPaid": 3000
  }
]
```

---

## Get All Payments

```
GET /api/payments
```

Returns all payment records.

---

# Business Logic

## Prevent Overpayment

If:

```
Invoice Total = 10000
Customer Pays = 11000
```

System throws error:

```
Payment exceeds invoice amount
```

This is validated before saving the payment.

---

# Invoice Status Update

After every payment, invoice status is updated.

Logic:

```
TotalPaid = sum(payments)
InvoiceTotal = invoice.totalAmount
```

Status Rules:

| Condition       | Status         |
| --------------- | -------------- |
| No payment      | UNPAID         |
| Partial payment | PARTIALLY_PAID |
| Fully paid      | PAID           |

Example:

```
Invoice = 442500
Paid = 442500
Status = PAID
```

---

# Email Notification

After payment, the system automatically sends an email to the customer.

Email example:

```
Subject: Payment Received

Dear Customer,

We received your payment.

Invoice Number: INV-2026-00002
Invoice Amount: ₹442500
Paid Now: ₹40000
Remaining Balance: ₹402500

Thank you.
```

The system uses:

```
EmailService.sendEmail()
```

---

# Payment Number Generation

Payments use unique identifiers:

```
PAY-2026-cc140d7c
```

Format:

```
PAY-{YEAR}-{RANDOM_UUID}
```

Generated using `PaymentNumberGenerator`.

---

# Example Payment Scenario

Invoice:

```
Invoice Total = 442500
```

Payments:

```
4000
40000
40000
358500
```

Total Paid:

```
442500
```

Result:

```
Remaining = 0
Invoice Status = PAID
```

---

# Validation Rules

| Rule                     | Description                               |
| ------------------------ | ----------------------------------------- |
| Invoice must exist       | Payment cannot be created without invoice |
| Payment must be positive | Amount must be > 0                        |
| Overpayment blocked      | Cannot exceed invoice total               |
| Payment method required  | Must specify payment mode                 |

---

# Advantages of This Module

* Supports **multiple payments per invoice**
* Prevents **overpayment errors**
* Maintains **complete payment history**
* Automatically updates **invoice status**
* Sends **payment confirmation emails**
* Uses **MapStruct for clean DTO mapping**

---

# Future Enhancements

The following features can be added later:

### Customer Outstanding Report

Example:

```
Customer: ABC Industries
Total Invoices: 1,200,000
Total Paid: 800,000
Outstanding: 400,000
```

### Payment Ledger

```
Customer Payment History
Date | Payment | Invoice | Balance
```

### Dashboard Metrics

```
Total Revenue
Daily Collections
Pending Payments
```

---

# Conclusion

The **Payment Module** completes the **Accounts workflow** of the Foundry ERP system.

Final ERP flow:

```
Customer
   ↓
Enquiry
   ↓
Quotation
   ↓
Order
   ↓
Production
   ↓
Delivery Challan
   ↓
Invoice
   ↓
Payment
```

This module ensures accurate **financial tracking**, **customer transparency**, and **automated accounting processes** within the ERP.
