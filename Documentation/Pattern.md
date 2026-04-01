# Pattern Module Documentation

## Overview

The **Pattern Module** manages all pattern-related information used in the foundry manufacturing process.
Patterns are physical models used to create molds for casting metal parts. This module enables tracking, storage, lifecycle management, and usage of patterns within the system.

The module supports:

* Pattern master management
* Pattern lifecycle tracking
* Rack storage management
* Pattern usage reference in enquiries
* Customer-provided pattern tracking

---

# 1. Pattern Master

The **Pattern Master** stores all patterns owned or managed by the foundry.

Each pattern contains essential details such as:

* Pattern Number (auto-generated)
* Pattern Name
* Pattern Type
* Pattern Material
* Pattern Status
* Rack Storage Location

Patterns can be referenced in **Enquiry Items** when a casting part requires a specific pattern.

---

# 2. Pattern Number Generation

Each pattern is assigned a **unique pattern number** generated using a database sequence.

Example format:

```
PAT-00001
PAT-00002
PAT-00003
```

### Implementation

A PostgreSQL sequence is used:

```
pattern_number_seq
```

This ensures:

* Unique pattern numbers
* Concurrency-safe generation
* No duplicate numbers
* High performance

The pattern number is generated in the service layer during pattern creation.

---

# 3. Pattern Status Lifecycle

Patterns move through different lifecycle states during their usage.

### PatternStatus Enum

| Status            | Description                              |
| ----------------- | ---------------------------------------- |
| AVAILABLE         | Pattern is ready for use                 |
| IN_USE            | Pattern currently used for production    |
| UNDER_MAINTENANCE | Pattern undergoing repair or maintenance |
| SCRAPPED          | Pattern is permanently unusable          |

### Default Status

When a pattern is created, the default status is:

```
AVAILABLE
```

---

# 4. Rack Storage Management

Each pattern can be stored in a specific rack location in the pattern storage area.

Field:

```
rackNumber
```

Example:

```
R-12-A
R-21-B
R-05-C
```

This helps quickly locate patterns in the foundry storage.

---

# 5. Pattern Entity Structure

### Table: `patterns`

| Column         | Type      | Description                     |
| -------------- | --------- | ------------------------------- |
| id             | UUID      | Primary key                     |
| pattern_number | VARCHAR   | Unique generated pattern number |
| name           | VARCHAR   | Pattern name                    |
| type           | VARCHAR   | Pattern type                    |
| material       | VARCHAR   | Pattern material                |
| status         | VARCHAR   | Pattern lifecycle status        |
| rack_number    | VARCHAR   | Storage rack location           |
| tenant_id      | UUID      | Multi-tenant support            |
| created_at     | TIMESTAMP | Record creation timestamp       |
| updated_at     | TIMESTAMP | Record update timestamp         |
| created_by     | VARCHAR   | User who created record         |
| updated_by     | VARCHAR   | User who last updated record    |

Indexes:

* `idx_patterns_name`
* `idx_patterns_status`
* `idx_patterns_rack`

---

# 6. Pattern Types

Pattern types define the physical structure of the pattern used in casting.

Example values:

```
SINGLE_PIECE
SPLIT_PATTERN
MATCH_PLATE
```

Stored as Enum in the application and as String in the database.

---

# 7. Pattern Material

Pattern material indicates the material used to manufacture the pattern.

Example values:

```
WOOD
ALUMINIUM
CAST_IRON
RESIN
```

Stored as Enum in the application.

---

# 8. Pattern Receipt (Customer Patterns)

Sometimes customers provide their own patterns for casting.

These are tracked separately in the **Pattern Receipt** table.

### Table: `pattern_receipt`

| Column       | Type      | Description               |
| ------------ | --------- | ------------------------- |
| id           | UUID      | Primary key               |
| inward_date  | DATE      | Pattern received date     |
| outward_date | DATE      | Pattern returned date     |
| name         | VARCHAR   | Pattern name              |
| type         | VARCHAR   | Pattern type              |
| material     | VARCHAR   | Pattern material          |
| created_at   | TIMESTAMP | Record creation timestamp |
| updated_at   | TIMESTAMP | Record update timestamp   |

Customer-provided patterns are linked to enquiry items.

---

# 9. Pattern Usage in Enquiry

Patterns are referenced inside **Enquiry Items**.

Two scenarios exist:

### Scenario 1 — Company Pattern

```
pattern_provided_by_customer = false
pattern_id is used
```

### Scenario 2 — Customer Pattern

```
pattern_provided_by_customer = true
pattern_receipt_id is used
```

Database constraint ensures correct logic:

```
CHECK (
 pattern_provided_by_customer = TRUE AND pattern_receipt_id IS NOT NULL
 OR
 pattern_provided_by_customer = FALSE AND pattern_id IS NOT NULL
)
```

This prevents invalid data combinations.

---

# 10. Pattern APIs

### Create Pattern

```
POST /api/patterns
```

Creates a new pattern and generates a unique pattern number.

---

### Get All Patterns

```
GET /api/patterns?page=0&size=10&sort=createdAt,desc
```

Returns paginated list of patterns.

---

### Get Pattern By ID

```
GET /api/patterns/{id}
```

Returns a single pattern by its ID.

---

### Update Pattern

```
PUT /api/patterns/{id}
```

Updates pattern details such as name, type, material, and rack location.

Pattern number cannot be modified.

---

### Change Pattern Status

```
PATCH /api/patterns/{id}/status
```

Updates the lifecycle status of the pattern.

Business rules are enforced during status changes.

Example:

```
Cannot scrap pattern while it is IN_USE
```

---

# 11. Security

Pattern APIs are protected using role-based authorization.

| API            | Allowed Roles            |
| -------------- | ------------------------ |
| Create Pattern | ADMIN, SALES             |
| Update Pattern | ADMIN                    |
| Change Status  | ADMIN, PRODUCTION        |
| View Patterns  | ADMIN, SALES, PRODUCTION |

---

# 12. Audit Support

All pattern records include audit fields inherited from `BaseEntity`.

Fields:

```
created_at
updated_at
created_by
updated_by
```

This allows tracking of changes and accountability.

---

# 13. Key Benefits

The Pattern Module provides:

* Centralized pattern management
* Pattern lifecycle tracking
* Storage rack management
* Safe concurrent pattern number generation
* Integration with enquiry module
* Customer pattern tracking
* Role-based access control

---

# Conclusion

The Pattern Module ensures efficient management of foundry patterns and integrates seamlessly with the enquiry and production workflows.

It provides a scalable and maintainable foundation for managing pattern inventory and lifecycle within the Foundry ERP system.
