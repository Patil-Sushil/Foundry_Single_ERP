# Authentication & Authorization Module

## Overview

The **Authentication & Authorization module** is responsible for securing the Foundry ERP system by managing user identity, authentication, role-based access control (RBAC), and password management.

The system uses **Spring Security**, **JWT (JSON Web Tokens)**, and **Role-Based Authorization** to protect APIs and ensure that only authorized users can access specific resources.

---

# Technology Stack

| Component           | Technology                  |
| ------------------- | --------------------------- |
| Security Framework  | Spring Security             |
| Authentication      | JWT Token                   |
| Password Encryption | BCrypt                      |
| Database            | PostgreSQL                  |
| ORM                 | Hibernate / Spring Data JPA |
| Migration Tool      | Flyway                      |
| API Documentation   | Swagger / OpenAPI           |

---

# Architecture

```
Controller
   ↓
Service
   ↓
Repository
   ↓
Database
```

Authentication flow:

```
Login Request
     ↓
AuthenticationManager
     ↓
UserDetailsService
     ↓
Password Validation
     ↓
JWT Token Generation
     ↓
Client Receives Token
```

Subsequent API calls use the token for authorization.

---

# Database Schema

## Role Table

Stores system roles used for authorization.

| Column      | Type    | Description      |
| ----------- | ------- | ---------------- |
| id          | UUID    | Primary key      |
| name        | VARCHAR | Role name        |
| description | VARCHAR | Role description |

Example Roles:

```
ADMIN
SALES
DESIGN
PRODUCTION
FINANCE
STORE
```

---

## Users Table

Stores system user accounts.

| Column     | Type      | Description        |
| ---------- | --------- | ------------------ |
| id         | UUID      | Primary key        |
| name       | VARCHAR   | User name          |
| email      | VARCHAR   | Login email        |
| password   | VARCHAR   | Encrypted password |
| phone      | VARCHAR   | Contact number     |
| enabled    | BOOLEAN   | Account status     |
| created_at | TIMESTAMP | Creation timestamp |

---

## User Role Mapping

```
user_role
```

Many-to-many relationship between users and roles.

| Column  | Description      |
| ------- | ---------------- |
| user_id | references users |
| role_id | references role  |

---

## Audit Log

Tracks security related activities.

| Column    | Description        |
| --------- | ------------------ |
| id        | UUID               |
| user_id   | performing user    |
| action    | action description |
| timestamp | action time        |

---

# Security Components

## CustomUserDetails

Implements `UserDetails` and provides authenticated user information to Spring Security.

Responsibilities:

* Load user ID
* Load email as username
* Attach role authorities
* Provide authentication data to Spring Security

Example authority:

```
ROLE_ADMIN
ROLE_SALES
```

---

## JwtTokenProvider

Responsible for:

* Generating JWT tokens after successful login
* Extracting claims from tokens
* Validating token integrity
* Checking token expiration

JWT Claims include:

```
sub → user email
userId → user UUID
roles → user roles
iat → issued time
exp → expiration time
iss → token issuer
```

---

## JwtAuthenticationFilter

Intercepts every request and performs:

1. Extract token from Authorization header
2. Validate token
3. Load user details
4. Set authentication in SecurityContext

Example header:

```
Authorization: Bearer <jwt_token>
```

---

## SecurityConfig

Central configuration for Spring Security.

Features:

* Stateless authentication
* JWT filter integration
* Role-based endpoint protection
* CORS configuration
* Authentication provider configuration

Example access rules:

```
/api/auth/** → Public
/api/admin/** → ADMIN only
/api/enquiry/** → ADMIN, SALES
/api/production/** → ADMIN, PRODUCTION
/api/finance/** → ADMIN, FINANCE
```

---

# Authentication APIs

## Login

```
POST /api/auth/login
```

Authenticates user and returns JWT token.

### Request

```json
{
  "email": "admin@foundry.com",
  "password": "Admin@123"
}
```

### Response

```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "jwt_token_here",
    "id": "user_uuid",
    "email": "admin@foundry.com",
    "roles": ["ROLE_ADMIN"]
  }
}
```

---

# User Management APIs

## Create User (Admin Only)

```
POST /api/admin/create-user
```

### Request

```json
{
  "name": "Sales Executive",
  "email": "sales@foundry.com",
  "password": "Sales@123",
  "phone": "9876543210",
  "role": "SALES"
}
```

---

## Get All Users

```
GET /api/admin?page=0&size=10
```

Returns paginated list of system users.

---

## Get User By ID

```
GET /api/admin/{id}
```

---

## Delete User (Soft Delete)

```
DELETE /api/admin/{id}
```

The system does not remove records permanently.

Instead:

```
enabled = false
```

---

## Disable User

```
PATCH /api/admin/{id}/disable
```

Disables account access.

---

# Password Management

## Change Password

```
POST /api/auth/change-password
```

Users can update their own password.

### Request

```json
{
  "currentPassword": "OldPassword@123",
  "newPassword": "NewPassword@123"
}
```

Validation rules:

* Current password must match
* New password must be different
* Must satisfy password policy

Password policy:

* 8–20 characters
* Uppercase letter
* Lowercase letter
* Number
* Special character

---

# Security Best Practices Implemented

### Password Encryption

Passwords are stored using:

```
BCryptPasswordEncoder
```

---

### Stateless Authentication

The application does not maintain sessions.

Each request is authenticated using JWT.

---

### Role-Based Access Control

Endpoints are protected using annotations:

```
@PreAuthorize("hasRole('ADMIN')")
```

---

### Global Exception Handling

Security and business exceptions are handled using:

```
GlobalExceptionHandler
```

Ensuring consistent API responses.

---

# Default Admin Bootstrap

When the application starts and no users exist:

A default admin user is created automatically.

Configured via:

```
application.properties
```

Example:

```
app.default-admin.email=admin@foundry.com
app.default-admin.password=Admin@123
```

---

# Future Improvements

Planned security enhancements:

* Refresh token support
* Login rate limiting
* Multi-factor authentication
* Tenant-based authentication
* Password reset via email
* OAuth integration
* Audit trail improvements

---

# Summary

The Authentication module provides:

* Secure login system
* JWT-based authentication
* Role-based authorization
* Password management
* Admin user management
* Audit logging

This ensures secure and controlled access to the Foundry ERP system.

---
