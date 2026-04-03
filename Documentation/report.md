# Report Module Documentation

## Overview

The Foundry ERP reporting capability is split across multiple bounded areas:

- Core reporting package: `com.kalibyte.foundry.reports`
- Operational report endpoints in feature modules:
  - Production reports
  - Inventory reports
  - Labor reports

This document combines all report APIs and behavior in one place.

---

## Module Boundaries

### 1. Accounts Reports

Base path: `/api/reports/accounts`

Provides:

- Daily collections
- Collection summary
- Customer outstanding
- Customer ledger
- Receivables aging
- Cash flow
- Overdue invoice analysis
- Profit and loss

### 2. Expense Reports

Base path: `/api/reports/expenses`

Provides:

- Expense by head
- Expense by category
- Revenue analytics

### 3. GST Outward Reports

Base path: `/api/gst/outward`

Provides:

- GSTR-1 B2B
- GSTR-1 B2C large
- GSTR-1 B2C small
- HSN summary
- Document summary
- Sales register
- Tax liability summary
- Excel downloads for supported report types
- Access audit logging

### 4. Production Reports

Base path: `/api/reports/production`

Provides:

- Order-wise production progress
- Daily production report
- Monthly production report
- Dashboard summary

### 5. Inventory Reports

Base path: `/api/inventory/reports`

Provides:

- Inward report
- Issue report
- Item ledger
- Daily movement
- Stock summary
- Vendor summary

### 6. Labor Reports

Base path: `/api/labor-reports`

Provides:

- Weekly labor expense report
- Monthly labor expense report
- Yearly labor expense report
- Excel export of detailed labor attendance/earnings

---

## Security and Access Control

### URL-level rules

Configured in `SecurityConfig`:

- `/api/gst/**` -> `CA` or `ADMIN`
- `/api/reports/**` -> `ADMIN`, `MANAGER`, or `ACCOUNTANT`
- `/api/inventory/reports/**` -> `ADMIN`, `STORE`, or `FINANCE`
- `/api/labor-reports/**` -> `ADMIN`

### Method-level rules

Some report controllers also use `@PreAuthorize` at endpoint level.

Important: URL-level matcher checks apply before method-level checks. So any endpoint under `/api/reports/**` still requires `ADMIN|MANAGER|ACCOUNTANT` even if a method annotation includes other roles.

---

## API Catalog

## Accounts Reports (`/api/reports/accounts`)

- `GET /daily-collection?from&to`
- `GET /collection-summary?from&to`
- `GET /customer-outstanding?asOfDate`
- `GET /customer-ledger/{customerId}?from&to`
- `GET /aging?asOfDate`
- `GET /cash-flow?from&to`
- `GET /overdue?customerId&severity&minAmount&page&size`
- `GET /profit-loss?from&to`

## Expense Reports (`/api/reports/expenses`)

- `GET /by-head?from&to`
- `GET /by-category?from&to`
- `GET /revenue?from&to&customerId`

## GST Outward Reports (`/api/gst/outward`)

JSON endpoints:

- `POST /b2b`
- `POST /b2c-large`
- `POST /b2c-small`
- `POST /hsn-summary`
- `POST /document-summary`
- `POST /sales-register`
- `POST /tax-liability`

Excel download endpoints:

- `POST /b2b/download`
- `POST /b2c-large/download`
- `POST /b2c-small/download`
- `POST /hsn-summary/download`
- `POST /sales-register/download`
- `POST /tax-liability/download`

## Production Reports (`/api/reports/production`)

- `GET /order/{orderId}`
- `GET /daily?date`
- `GET /monthly?month&year`
- `GET /dashboard`

## Inventory Reports (`/api/inventory/reports`)

- `GET /inwards?startDate&endDate&vendorId&itemId&purchaseOrderId`
- `GET /issues?startDate&endDate&departmentId&itemId`
- `GET /items/{itemId}/ledger?startDate&endDate`
- `GET /daily-movement?date&category`
- `GET /stock-summary?category&belowReorderLevel&departmentId`
- `GET /vendor-summary?startDate&endDate&vendorId`

## Labor Reports (`/api/labor-reports`)

- `GET /weekly?date`
- `GET /monthly?date`
- `GET /yearly/{year}`
- `GET /export?startDate&endDate` (Excel)

---

## GST Request Contract

All GST endpoints accept `GstReportRequest` with `periodType`:

- `CUSTOM`: requires `fromDate`, `toDate`
- `MONTHLY`: requires `month`, `year`
- `QUARTERLY`: requires `quarter`, `year`
- `YEARLY`: requires `financialYear` (FY Apr-Mar)

Validation rules include:

- `toDate >= fromDate`
- Resolved period cannot exceed 1 year

---

## Computation Summary by Area

### Accounts

- Daily Collection:
  - Aggregates daily amount, payment-method split, transaction count
- Collection Summary:
  - Current period total, previous period total, growth percent
  - Method-wise split and top customers
- Customer Outstanding:
  - `outstanding = total invoiced - total paid` per customer
- Customer Ledger:
  - Merges invoices and payments into date-sorted ledger with running balance
- Aging:
  - Buckets receivables into current, 1-30, 31-60, 61-90, 90+
- Cash Flow:
  - Day-wise inflow/outflow and net flow for each date in range
- Overdue:
  - Severity classification: LOW, MEDIUM, HIGH, CRITICAL by overdue days
  - Bucket summary and customer grouping
- Profit and Loss:
  - Income = invoiced revenue + WIP value
  - COGS = furnace material + electricity + labor + non-furnace issue cost
  - Net profit = gross profit - general expenses
  - Includes monthly trend and expense breakdown percentages

### Expense

- By Head:
  - Groups by expense head with total amount and transaction count
- By Category:
  - Groups by expense category with totals and count
- Revenue:
  - Revenue, collections, outstanding, collection efficiency, average invoice
  - Monthly trend and top customer contribution share

### GST

- B2B:
  - Grouped by customer GSTIN with tax and invoice aggregates
- B2C (Large/Small):
  - Invoice-level outward tax summary based on classification
- HSN Summary:
  - Aggregates taxable and tax amounts by grade/item dimensions
- Document Summary:
  - Uses active and cancelled invoice counts and serial range
- Sales Register:
  - Invoice-level detailed outward register with GST breakdown
- Tax Liability:
  - Total output tax with B2B/B2C splits and monthly breakdown
- Audit:
  - Every GST report view/download logs user, report type, period, format, IP

### Production

- Order report:
  - Cumulative stage totals by order item
- Daily report:
  - Total production and dispatch by date
- Monthly report:
  - Day-wise totals plus monthly aggregate
- Dashboard:
  - Today totals, month-to-date totals, pending dispatch, active orders

### Inventory

- Inwards/Issues:
  - Document-level and item-level movement, qty/value aggregates
- Item Ledger:
  - Opening stock, inward/issue/adjustment transactions, running balance
- Daily Movement:
  - Per-item opening/inward/outward/closing for selected day
- Stock Summary:
  - Stock value, low stock and critical stock counts
- Vendor Summary:
  - PO, inward, pending value, ledger balance, supplied item mix

### Labor

- Weekly/Monthly/Yearly:
  - Total workers, total hours, total labor cost for period
- Detailed report:
  - Grouped by laborer with daily attendance detail
- Export:
  - XLSX generation using Apache POI

---

## Data Sources and Dependencies

Main repositories involved:

- Billing/Payment:
  - `InvoiceRepository`
  - `PaymentRepository`
- Expense:
  - `ExpenseRepository`
- Production:
  - `ProductionEntryRepository`
  - `ProductionItemRepository`
- Furnace/Energy:
  - `FurnaceHeatsRepository`
  - `ElectricityRateRepository`
- Inventory:
  - Inward, Issue, Item, PO, Vendor Ledger, Stock Adjustment repositories
- Labor:
  - `AttendanceRepository`
  - `WeeklyPayoutRepository`
- GST:
  - `GstInvoiceRepository`
  - `GstReportAuditLogRepository`

---

## Database Objects for Reporting

Notable migration:

- `V34__create_gst_report_module.sql`
  - Creates `gst_report_audit_log`
  - Adds indexes for GST audit lookups
  - Adds invoice indexes for GST query performance

---

## Notes for Implementation and QA

- Ensure role matrix tests cover URL-level + method-level authorization combinations.
- Add regression tests for date-range validation and empty-result periods.
- Validate that report downloads include correct filename suffix per period resolution.
- For large date windows, prefer indexed queries and pagination where already supported.

---

## Suggested Frontend Grouping

Recommended UI menu structure:

- Reports
  - Accounts Reports
  - Expense Reports
  - GST Reports
  - Production Reports
  - Inventory Reports
  - Labor Reports

This mirrors backend modules and keeps permission-based navigation clean.
