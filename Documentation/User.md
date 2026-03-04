# User Management Module

## Overview

The **User Management module** is responsible for managing system users within the Foundry ERP system.
It allows administrators to create, manage, disable, and monitor users who interact with the ERP platform.

This module integrates with the **Authentication and Authorization system** to enforce **role-based access control (RBAC)** and ensure secure access to ERP resources.

---

# Technology Stack

| Component           | Technology                  |
| ------------------- | --------------------------- |
| Framework           | Spring Boot                 |
| Security            | Spring Security             |
| Authentication      | JWT                         |
| ORM                 | Spring Data JPA / Hibernate |
| Database            | PostgreSQL                  |
| Password Encryption | BCrypt                      |
| API Documentation   | Swagger / OpenAPI           |

---

# Module Architecture

```text
Controller
   ↓
Service Layer
   ↓
Repository Layer
   ↓
Database
```

The module follows a **layered architecture** to ensure maintainability and scalability.

---

# Database Schema

## Users Table

Stores system user accounts.

| Column     | Type      | Description           |
| ---------- | --------- | --------------------- |
| id         | UUID      | Primary key           |
| name       | VARCHAR   | Full name of the user |
| email      | VARCHAR   | Unique login email    |
| password   | VARCHAR   | Encrypted password    |
| phone      | VARCHAR   | Contact number        |
| enabled    | BOOLEAN   | Account active status |
| created_at | TIMESTAMP | Account creation time |

---

## Role Table

Defines system roles.

| Column      | Type    | Description      |
| ----------- | ------- | ---------------- |
| id          | UUID    | Primary key      |
| name        | VARCHAR | Role name        |
| description | VARCHAR | Role description |

Example roles:

```
ADMIN
SALES
DESIGN
PRODUCTION
FINANCE
STORE
```

---

## User Role Mapping

```text
user_role
```

This table creates a **many-to-many relationship** between users and roles.

| Column  | Description            |
| ------- | ---------------------- |
| user_id | references users table |
| role_id | references role table  |

A user can have one or more roles depending on system requirements.

---

# Key Features

## User Creation

Administrators can create new system users.

Key validations include:

* Email must be unique
* Password must satisfy security policy
* Role must exist in the system

Password is encrypted using:

```
BCryptPasswordEncoder
```

---

## Get All Users

Administrators can retrieve a paginated list of users.

Supports:

* Pagination
* Sorting
* Role inspection

Example endpoint:

```
GET /api/admin?page=0&size=10
```

---

## Get User by ID

Retrieve details of a specific user.

```
GET /api/admin/{id}
```

---

## Disable User

Instead of deleting a user permanently, accounts can be disabled.

```
PATCH /api/admin/{id}/disable
```

Disabling a user prevents login while preserving historical records.

---

## Delete User (Soft Delete)

Users are not permanently removed from the database.

Instead, the system disables the account:

```
enabled = false
```

This ensures that past activities such as orders, quotations, and audits remain traceable.

---

# User APIs

## Create User

```
POST /api/admin/create-user
```

### Request Example

```json
{
  "name": "Sales Executive",
  "email": "sales@foundry.com",
  "password": "Sales@123",
  "phone": "9876543210",
  "role": "SALES"
}
```

### Response Example

```json
{
  "success": true,
  "message": "User created successfully"
}
```

---

## Get All Users

```
GET /api/admin?page=0&size=10
```

Returns a paginated list of system users.

---

## Get User by ID

```
GET /api/admin/{id}
```

---

## Disable User

```
PATCH /api/admin/{id}/disable
```

Disables a user account.

---

## Delete User

```
DELETE /api/admin/{id}
```

Performs a soft delete by disabling the account.

---

# Password Management

Users can update their password using the authentication module.

Password change rules:

* Current password must match
* New password must be different
* Password must follow security policy

Password policy:

* Minimum 8 characters
* Maximum 20 characters
* At least one uppercase letter
* At least one lowercase letter
* At least one number
* At least one special character

---

# Security & Access Control

The module uses **Role-Based Access Control (RBAC)**.

Example role protection:

```java
@PreAuthorize("hasRole('ADMIN')")
```

Only users with **ADMIN role** can:

* Create users
* Disable users
* Delete users
* View all users

---

# DTO Structure

## UserRegistrationRequest

Used when creating a new user.

Fields include:

* name
* email
* password
* phone
* role

---

## UserResponse

Returned when retrieving user details.

Fields include:

* id
* name
* email
* phone
* roles
* enabled
* createdAt

Sensitive data such as passwords are never exposed in responses.

---

# Service Responsibilities

The service layer manages:

* User creation
* Password encryption
* Role assignment
* Duplicate email validation
* Pagination handling
* Account disabling
* DTO mapping

---

# Repository Layer

UserRepository extends:

```
JpaRepository<User, UUID>
```

Key repository methods:

```
existsByEmail(String email)
findByEmail(String email)
findAll(Pageable pageable)
```

These methods support authentication and user management features.

---

# Exception Handling

Custom exceptions used in this module:

| Exception              | Purpose                  |
| ---------------------- | ------------------------ |
| BusinessException      | Business rule violations |
| UserNotFoundException  | User does not exist      |
| DuplicateUserException | Email already exists     |

Errors are handled globally using:

```
GlobalExceptionHandler
```

---

# Future Enhancements

Planned improvements for this module include:

* User role update API
* Password reset by admin
* Account lock after multiple failed login attempts
* User activity logs
* Multi-role user support
* Tenant-based user management

---

# Summary

The User Management module provides secure and controlled management of system users in the Foundry ERP platform.

Key capabilities include:

* Secure user creation
* Role-based access control
* Password encryption
* Account management
* Soft deletion strategy
* Integration with authentication module

This module ensures that only authorized users can access and manage ERP resources.
