-- =============================================
-- V20: GST Report Module - CA Role & Audit
-- =============================================

-- -----------------------------
--  GST REPORT AUDIT LOG
-- Tracks every report view/download by CA
-- -----------------------------
CREATE TABLE IF NOT EXISTS gst_report_audit_log (
                                                    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    report_type VARCHAR(50) NOT NULL,
    period_type VARCHAR(20) NOT NULL,
    period_from DATE NOT NULL,
    period_to DATE NOT NULL,
    export_format VARCHAR(10),
    ip_address VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_report_type CHECK (
                                         report_type IN (
                                         'GSTR1_B2B', 'GSTR1_B2C_LARGE', 'GSTR1_B2C_SMALL',
                                         'GSTR1_HSN_SUMMARY', 'GSTR1_DOCUMENT_SUMMARY',
                                         'SALES_REGISTER', 'TAX_LIABILITY_SUMMARY',
                                         'GSTR1_FULL'
                                                        )
    ),
    CONSTRAINT chk_period_type CHECK (
                                         period_type IN ('CUSTOM', 'MONTHLY', 'QUARTERLY', 'YEARLY')
    ),
    CONSTRAINT chk_export_format CHECK (
                                           export_format IS NULL OR export_format IN ('XLSX','CSV', 'PDF', 'JSON')
    )
    );

-- INDEXES
CREATE INDEX IF NOT EXISTS idx_gst_audit_user ON gst_report_audit_log(user_id);
CREATE INDEX IF NOT EXISTS idx_gst_audit_type ON gst_report_audit_log(report_type);
CREATE INDEX IF NOT EXISTS idx_gst_audit_period ON gst_report_audit_log(period_from, period_to);
CREATE INDEX IF NOT EXISTS idx_gst_audit_created ON gst_report_audit_log(created_at);

-- -----------------------------
-- ADDITIONAL INDEXES ON INVOICES FOR GST QUERIES
-- -----------------------------
CREATE INDEX IF NOT EXISTS idx_invoice_date_status ON invoices(invoice_date, bill_status);
CREATE INDEX IF NOT EXISTS idx_invoice_gst_date ON invoices(gst_type, invoice_date);