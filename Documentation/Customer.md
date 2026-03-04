# Customer Management Module

## Overview

The **Customer Management module** manages all customer-related information in the Foundry ERP system.
It allows administrators and authorized users to create, update, search, and manage customer records used across the system for enquiries, quotations, orders, and billing.

This module ensures data consistency, prevents duplicate entries, and provides a scalable structure for customer data management.

---

# Technology Stack

| Component         | Technology                  |
| ----------------- | --------------------------- |
| Backend Framework | Spring Boot                 |
| ORM               | Spring Data JPA / Hibernate |
| Database          | PostgreSQL                  |
| Migration Tool    | Flyway                      |
| DTO Mapping       | ModelMapper                 |
| Validation        | Custom Validator            |
| API Documentation | Swagger / OpenAPI           |

---

# Module Architecture

```text
Controller
   ↓
Service Layer
   ↓
Repository Layer
   ↓
PostgreSQL Database
```

The architecture follows a **clean layered design** ensuring separation of concerns.

---

# Database Schema

## Customers Table

Stores all customer information.

| Column        | Type      | Description             |
| ------------- | --------- | ----------------------- |
| id            | UUID      | Primary key             |
| name          | VARCHAR   | Customer contact name   |
| email         | VARCHAR   | Unique customer email   |
| phone         | VARCHAR   | Unique contact number   |
| company_name  | VARCHAR   | Company name            |
| address       | VARCHAR   | Street address          |
| city          | VARCHAR   | City                    |
| state         | VARCHAR   | State                   |
| postal_code   | VARCHAR   | ZIP / postal code       |
| country       | VARCHAR   | Country                 |
| gst_number    | VARCHAR   | GST registration number |
| payment_terms | VARCHAR   | Payment conditions      |
| credit_limit  | DECIMAL   | Customer credit limit   |
| status        | VARCHAR   | ACTIVE / INACTIVE       |
| created_at    | TIMESTAMP | Record creation time    |

---

# Key Features

### Customer Creation

Allows authorized users to register new customers.

Key validations include:

* Email must be unique
* Phone number must be unique
* Required fields validation
* Default country assignment if missing
* Default status set to **ACTIVE**

---

### Customer Retrieval

Customers can be fetched using:

* Customer ID
* Phone number
* Paginated customer list

---

### Customer Update

Existing customer details can be updated while maintaining validation rules.

---

### Soft Delete

Customers are not permanently removed from the system.

Instead, the system performs a **soft delete** by updating the status field:

```text
status = INACTIVE
```

This ensures historical data integrity for:

* quotations
* orders
* billing records

---

# Customer APIs

## Create Customer

```text
POST /api/customers
```

### Request Example

```json
{
  "name": "ABC Industries",
  "email": "contact@abcindustries.com",
  "phone": "8989898989",
  "companyName": "ABC Industries Pvt Ltd",
  "address": "MIDC Industrial Area",
  "city": "Pune",
  "state": "Maharashtra",
  "postalCode": "411019",
  "country": "India",
  "gstNumber": "27ABCDE1234F1Z5",
  "paymentTerms": "NET30",
  "creditLimit": 200000
}
```

### Response Example

```json
{
  "success": true,
  "message": "Customer created successfully",
  "data": {
    "id": "customer_uuid",
    "name": "ABC Industries",
    "email": "contact@abcindustries.com"
  }
}
```

---

## Get Customer by ID

```text
GET /api/customers/{customerId}
```

Returns customer details for the specified ID.

---

## List Customers

```text
GET /api/customers?page=0&size=10&sort=name
```

Supports:

* Pagination
* Sorting

Example:

```text
GET /api/customers?page=0&size=20&sort=createdAt
```

---

## Update Customer

```text
PUT /api/customers/{customerId}
```

Updates customer details.

---

## Delete Customer (Soft Delete)

```text
DELETE /api/customers/{customerId}
```

Marks the customer as inactive.

---

## Find Customer by Phone

```text
GET /api/customers/phone/{phone}
```

Returns customer details using phone number lookup.

---

# Validation Rules

The system uses a dedicated validator:

```text
CustomerValidator
```

Validations include:

* Required fields validation
* Email format validation
* Phone number validation
* Business rules enforcement

---

# Exception Handling

The module uses custom exceptions:

| Exception                  | Purpose                       |
| -------------------------- | ----------------------------- |
| CustomerNotFoundException  | Customer does not exist       |
| DuplicateCustomerException | Email or phone already exists |

All exceptions are handled by the **GlobalExceptionHandler** to provide consistent API responses.

---

# DTO Structure

## CustomerRequest

Used when creating or updating a customer.

Contains fields such as:

* name
* email
* phone
* companyName
* address
* GST number
* creditLimit

---

## CustomerResponse

Returned by APIs to expose customer information without exposing internal entity structure.

---

# Repository Layer

CustomerRepository extends:

```text
JpaRepository<Customer, UUID>
```

Key repository methods:

```text
existsByEmail(String email)
existsByPhone(String phone)
findByPhone(String phone)
```

These methods support validation and quick lookups.

---

# Service Layer Responsibilities

The service layer manages business logic including:

* Validation
* Duplicate checks
* Default value assignment
* DTO mapping
* Soft deletion
* Pagination handling

---

# Security Considerations

Customer endpoints are secured using **Spring Security** and **JWT authentication**.

Access is typically allowed to:

* ADMIN
* SALES

Authorization example:

```java
@PreAuthorize("hasAnyRole('ADMIN','SALES')")
```

---

# Future Enhancements

Planned improvements include:

* Customer search and filtering
* Customer activity history
* Customer contact persons
* Multi-address support
* Customer credit tracking
* Integration with quotation and order modules

---

# Summary

The Customer module provides a reliable and scalable solution for managing customer data within the Foundry ERP system.

It ensures:

* Clean data structure
* Duplicate prevention
* Secure access control
* Integration readiness with other ERP modules such as enquiries, quotations, and orders.
