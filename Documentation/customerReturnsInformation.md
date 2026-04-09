# Customer Returns & Credit Note Management

## Overview
The Customer Return module manages the lifecycle of products returned by customers due to quality issues or other complaints. It ensures full traceability from the initial return receipt to final financial or production disposition.

---

## 1. Return Lifecycle

### 1.1 Receipt (`RECEIVED`)
When a customer returns items, a `CustomerReturn` entry is created. 
- **Automated Weight Calculation**: The system automatically calculates the `returned_weight` by fetching the `net_weight_kg` from the original `OrderItem`.
- **Return Number**: A unique number is generated in the format `RET-YYYY-XXXXX`.

### 1.2 Assessment (`ASSESSED`)
QA inspectors assess the physical parts and record:
- **QA Finding**: (e.g., Blowhole, Dimensional Error).
- **Root Cause**: Categorized by process (Melting, Moulding, etc.).
- **Inspector Remarks**: Detailed observations.

### 1.3 Disposition (`DISPOSITIONED`)
Management decides the final resolution. The system provides three automated workflows:

#### A. Replacement (`REPLACE`)
If a replacement is required, the system automates the creation of a new **Sales Order**:
- **Automatic Order Creation**: If no manual `replacement_order_id` is provided, the system calls the `OrderService` to create a new `DIRECT` order.
- **Data Inheritance**: The new order inherits the Customer, Material Grade, Metal Type, and Unit Price from the original transaction.
- **PO Reference**: The new order is tagged with `REPLACEMENT-RET-XXXXX` for easy tracking in the Order module.
- **Quantity**: The order quantity matches the `returned_quantity`.

#### B. Credit Note (`CREDIT_NOTE`)
If the customer is to be refunded/credited, a **Credit Note** is automatically generated in the Billing module:
- **Credit Note Entity**: A new `CreditNote` record is created and linked to the `CustomerReturn`.
- **Original Invoice Link**: The system automatically fetches and stores the **Original Invoice Number** associated with the returned product for audit compliance.
- **Numbering**: Uses the format `CN-YYYY-XXXXX`.
- **Automated GST Calculation**: The system calculates CGST/SGST or IGST based on the customer's state, using the GST percentage from the original order.
- **Amount**: The `credit_amount` provided during disposition is treated as the subtotal, and GST is added on top to match the original billing structure.

#### C. Scrap (`SCRAP_FOR_REMELT` / `SCRAP_FOR_SALE`)
If the part is non-reworkable:
- **Scrap Entry**: An automated `ScrapEntry` is created in the Scrap module.
- **Material Recovery**: The material grade is preserved to ensure accurate inventory of remeltable scrap.

---

## 2. Data Traceability
The system maintains a "Golden Chain" of IDs:
- `CustomerReturn` → `replacement_order_id` (Points to the new Sales Order)
- `CustomerReturn` → `credit_note_id` (Points to the Financial Credit Note)
- `CustomerReturn` → `original_invoice_number` (Link to the original sale)
- `CustomerReturn` → `scrap_entry_id` (Points to the Scrap Record)

---

## 3. Financial Integration
Credit notes are stored in the `credit_notes` table and are linked to the `Customer`, `Order`, and `Invoice`.
1. **Tax Filing**: Used to reduce the outward tax liability in GST returns.
2. **Ledger History**: Provides a complete trail of sales vs. returns for each customer.
3. **Audit Readiness**: Stores original invoice references as required by tax authorities for Credit Note issuance.

---

## 5. GST Reporting & Excel Export
The system includes a dedicated **GST Report Module** that accounts for both Invoices and Credit Notes to determine net tax liability.

### 5.1 Types of Reports
1.  **GSTR-1 B2B Report**: Now includes a "Credit / Debit Notes (Registered)" section.
2.  **Tax Liability Summary**: Aggregates total outward tax and subtracts credit note tax to show **Net Tax Liability**.
3.  **HSN Summary**: (Planned) To include quantity/value reductions from returns.
4.  **Document Summary**: Tracks the sequence of both Invoices and Credit Notes.

### 5.2 Report Generation Logic
Reports are generated using the `GstOutwardReportService`:
- **Liability Reduction**: The system fetches all outward credit notes for the period and subtracts their taxable value and GST from the invoice totals.
- **Validation**: Ensures all credit notes are linked to valid customers and orders.

### 5.3 Excel Export Flow
The `GstExcelExportService` generates `.xlsx` files with the following features:
- **B2B Export**: 
    - Separate section for **Invoices**.
    - Separate section for **Credit Notes** (showing Note Number and Original Invoice Number).
    - **Net Totals**: Final section showing Net Taxable Value and Net GST Liability.
- **Tax Liability Export**:
    - **Summary Sheet**: Shows Invoice Totals, Credit Note Totals, and final Net Liability.
    - **Monthly Breakdown**: Shows month-wise net tax after adjusting for returns.

---

## 6. Security & Audit
- **Roles**: Assessment/Disposition restricted to `ADMIN` or `QUALITY`. Finance reports restricted to `ADMIN` and `FINANCE`.
- **Audit Log**: Every credit note generation is logged in the `qa_tracking_log` and captured in the `gst_report_audit_log` during filing.
