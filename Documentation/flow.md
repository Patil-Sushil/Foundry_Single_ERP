# Foundry ERP System Workflows

This document outlines the end-to-end business processes and independent operational workflows within the Foundry ERP system.

---

## 1. Core Sales & Production Workflow (Linear)

The primary "Golden Path" follows the lifecycle of a casting part from the initial customer interest to final payment.

### **Step 1: Enquiry**
- **Capture Requirements**: Record part names, metal grades, quantities, piece weights, and machining requirements.
- **Pattern Tracking**: Identify if the pattern is provided by the customer or exists in the foundry's master list.
- **Outcome**: An enquiry in `PENDING` status.

### **Step 2: Quotation**
- **Costing**: Calculate costs based on Net Weight, Rate per KG, and Quantity.
- **Submission**: Generate a professional PDF and **automatically email it to the customer**.
- **Lifecycle**: Revisions are tracked; the quotation moves from `DRAFT` → `SENT` → `APPROVED` or `REJECTED`.

### **Step 3: Sales Order**
- **Conversion**: Approved quotations are converted into Orders.
- **Direct Orders**: Orders can also be created directly for existing customers without a prior quotation.
- **Communication**: The system **automatically sends an Order Confirmation email** to the customer.
- **Outcome**: A unique order number (e.g., `ORD-2026-00001`) is generated.

### **Step 4: Production**
- **Daily Entries**: Supervisors log daily shift-wise production for specific orders.
- **Stage Tracking**: Pieces are tracked through `Cores` → `Moulds` → `Shot Blasting` → `Fettling`.
- **Traceability**: Production items are linked to specific furnace heats.

### **Step 5: Quality Assurance (QA)**
- **Inspections**: Items entering the `Fettling` stage trigger a QA Inspection.
- **Findings**: Defects are recorded from a master catalog.
- **Disposition**: Items are `Accepted` (ready for dispatch), `Rejected` (marked for scrap), or marked for `Rework`.

### **Step 6: Dispatch (Delivery Challan)**
- **Creation**: A Delivery Challan (DC) is generated for `Accepted` quantities.
- **Partial Dispatch**: Multiple DCs can be created for a single order until the total quantity is fulfilled.
- **Communication**: The system **automatically emails the Delivery Challan PDF** to the customer upon dispatch.
- **Logistics**: Transport vehicle and LR numbers are recorded.

### **Step 7: Invoicing**
- **Generation**: Tax Invoices are generated directly from Delivery Challans.
- **Tax Logic**: GST (CGST/SGST or IGST) is automatically calculated based on the customer's state.
- **Communication**: The system **automatically emails the GST-compliant Invoice PDF** to the customer.
- **Outcome**: A professional Invoice document is finalized.

### **Step 8: Payment**
- **Settlement**: Payments are recorded against specific invoices.
- **Communication**: The system **automatically sends a Payment Receipt email** acknowledging the transaction.
- **Tracking**: Supports partial payments, updating the invoice status from `UNPAID` → `PARTIALLY_PAID` → `PAID`.

---

## 2. Reverse Logistics & Financial Adjustments

Handling exceptions in the sales cycle to ensure financial and inventory accuracy.

### **A. Customer Returns (RMA)**
- **Receipt**: Log returned items with a unique Return Number (`RET-YYYY-XXXXX`).
- **QA Assessment**: Re-inspect returned goods to determine the cause of failure.
- **Automated Workflows**: 
    - **Replacement**: Automatically triggers a new `DIRECT` sales order for the same part and quantity.
    - **Credit Note**: Automatically generates a financial Credit Note linked to the original invoice.
    - **Scrap**: Routes returned parts to the scrap yard for remelting.

### **B. Credit Notes**
- **Tax Compliance**: Automatically calculates GST reductions to offset original tax liability.
- **Audit Trail**: Captures the **Original Invoice Number** to satisfy tax department audit requirements for returns.
- **Integration**: Reduces total taxable value in GST reports.

---

## 3. Independent Operational Workflows

These workflows run parallel to the core flow and provide the necessary resources and recycling loops.

### **A. Furnace Operations**
- **Melting Heats**: Track chemical composition (Si, C, Mg), temperature, and energy (kWh) per heat.
- **Auto-Issue**: Saving a heat automatically triggers a `Material Issue` from inventory for raw materials (Pig Iron, Scrap, Alloys).
- **KPIs**: Monitors `Power-to-Weight` ratios and furnace utilization.

### **B. Procurement & Accounts Payable**
- **Vendor Profiles**: Manage supplier contact info, GSTIN, and credit terms.
- **Purchase Orders (PO)**: Issue formal orders to vendors for raw materials or consumables.
- **Material Inward (GRN)**: Record received goods, check quality, and auto-increment stock levels.
- **Purchase Invoices**: Log vendor invoices against POs and GRNs to track accounts payable.
- **Vendor Ledger**: A complete financial history of transactions and outstanding dues for every supplier.

### **C. Inventory & Resource Management**
- **Consumption (Material Issue)**: Track issuance of materials to specific departments (e.g., Core Shop, Moulding, Maintenance).
- **Stock Adjustment**: Perform manual corrections for physical stock discrepancies with mandatory reason codes (e.g., Damage, Loss).
- **Valuation**: Uses Weighted Average Cost (WAC) for real-time inventory valuation.

### **D. Labor Management**
- **Attendance**: Daily check-in/out logs for permanent and contract workers.
- **Wages**: Multi-mode calculations: `Hourly`, `Daily`, or `Piece Rate` (per part produced).
- **Financials**: Integrated tracking of `Labor Advances` and weekly `Payout` settlement.

### **E. Scrap Recycling Loop**
- **Sources**: Process scrap (Runners/Risers) and QA Rejections.
- **Recovery**: Metallurgists verify the material grade.
- **Stock Return**: Approved scrap is "Inwarded" back into inventory as raw material, significantly reducing material costs.

### **F. Pattern Management**
- **Storage**: Track physical storage location (Shelf/Rack) of customer patterns.
- **Condition**: Monitor pattern condition and track maintenance/repair cycles.
- **History**: Log every time a pattern is issued to the core shop or returned to storage.

---

## 4. Reporting & Governance

### **A. GST & Compliance**
- **GSTR-1 Reports**: Automated B2B, B2C Large, and B2C Small outward supply reports.
- **Tax Offset**: Automatically subtracts Credit Notes from Sales Invoices for net tax liability.
- **Export**: Generates government-format styled Excel sheets for CA audit and filing.

### **B. Financial Analytics**
- **Receivables Aging**: Track overdue invoices by buckets (1-30, 31-60, 90+ days) to manage cash flow.
- **Revenue Trends**: Month-wise analysis of sales performance and growth.
- **Expense Breakdown**: Categorized view of direct and indirect factory overheads.

### **C. Security & Audit Framework**
- **Role-Based Access (RBAC)**: Fine-grained permissions for roles like `ADMIN`, `QUALITY`, `PRODUCTION`, `FINANCE`, and `CA`.
- **System Audit**: Every entity (Order, Invoice, Heat) automatically tracks `created_by`, `updated_by`, and timestamps via JPA Auditing.
- **QA Tracking**: Specialized logs for every state change in the quality and return lifecycle.

---

## 5. Workflow Integration Summary

| Integration Point | Source Module | Target Module | Action |
| :--- | :--- | :--- | :--- |
| **Material Issuance** | Furnace Heat | Inventory | Auto-deduct raw materials |
| **Scrap Generation** | Furnace Heat | Scrap Mgmt | Auto-create process scrap batch |
| **Quality Control** | Production | QA | Auto-create inspection draft after fettling |
| **Recycling** | Scrap Mgmt | Inventory | Auto-inward remelt material |
| **Returns-Production**| Customer Return | Order Mgmt | Auto-create Replacement Order |
| **Returns-Finance**   | Customer Return | Billing (CN) | Auto-create Credit Note with Tax offset |
| **Procurement-Stock** | Material Inward | Inventory | Auto-increment stock levels |
| **Procurement-Payable**| Purchase Invoice| Vendor Ledger | Auto-update vendor balance |
| **Billing Source**    | Dispatch (DC) | Invoicing | Carry quantity/weight to Invoice |
| **Order Completion**  | Dispatch (DC) | Order Mgmt | Auto-close order when qty fulfilled |
