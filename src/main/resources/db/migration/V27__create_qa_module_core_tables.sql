-- =====================================================
-- QA DEFECT CATALOG
-- =====================================================
CREATE TABLE IF NOT EXISTS qa_defect_catalog (
    id          BIGSERIAL PRIMARY KEY,
    code        VARCHAR(20) UNIQUE NOT NULL,
    name        VARCHAR(100) NOT NULL,
    category    VARCHAR(30) NOT NULL,
    severity    VARCHAR(15) NOT NULL DEFAULT 'MAJOR',
    description TEXT,
    is_active   BOOLEAN DEFAULT TRUE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by  VARCHAR(255),
    updated_by  VARCHAR(255)
);

-- SEED DEFECTS
INSERT INTO qa_defect_catalog (code, name, category, severity) VALUES
('BH-001', 'Blowhole', 'CASTING', 'MAJOR'),
('BH-002', 'Pinhole Porosity', 'CASTING', 'MAJOR'),
('SHR-001', 'Shrinkage Cavity', 'CASTING', 'CRITICAL'),
('SHR-002', 'Shrinkage Porosity', 'CASTING', 'MAJOR'),
('CRK-001', 'Hot Tear', 'CASTING', 'CRITICAL'),
('CRK-002', 'Cold Crack', 'CASTING', 'CRITICAL'),
('SND-001', 'Sand Inclusion', 'SURFACE', 'MAJOR'),
('SND-002', 'Sand Erosion', 'SURFACE', 'MAJOR'),
('MIS-001', 'Misrun', 'CASTING', 'CRITICAL'),
('CLD-001', 'Cold Shut', 'CASTING', 'MAJOR'),
('DIM-001', 'Oversize', 'DIMENSIONAL', 'MINOR'),
('DIM-002', 'Undersize', 'DIMENSIONAL', 'MAJOR'),
('SRF-001', 'Rough Surface', 'SURFACE', 'MINOR'),
('CHM-001', 'Out-of-Spec Chemistry', 'CHEMICAL', 'CRITICAL'),
('HRD-001', 'Hardness Failure', 'CHEMICAL', 'MAJOR');

-- =====================================================
-- QA INSPECTIONS
-- =====================================================
CREATE TABLE IF NOT EXISTS qa_inspections (
    id                      BIGSERIAL PRIMARY KEY,
    inspection_number       VARCHAR(50) UNIQUE NOT NULL,
    production_entry_id     UUID NOT NULL,
    production_item_id      UUID NOT NULL,
    order_id                UUID NOT NULL,
    order_item_id           UUID NOT NULL,
    heat_order_item_id      BIGINT,
    inspection_stage        VARCHAR(30) NOT NULL,
    inspection_type         VARCHAR(20) NOT NULL DEFAULT 'VISUAL',
    inspection_date         DATE NOT NULL,
    inspector_name          VARCHAR(100) NOT NULL,
    total_inspected         INTEGER NOT NULL DEFAULT 0,
    total_accepted          INTEGER NOT NULL DEFAULT 0,
    total_rejected          INTEGER NOT NULL DEFAULT 0,
    total_reworkable        INTEGER NOT NULL DEFAULT 0,
    result                  VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    status                  VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    remarks                 TEXT,
    created_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by              VARCHAR(255),
    updated_by              VARCHAR(255),
    CONSTRAINT chk_totals CHECK (total_accepted + total_rejected + total_reworkable <= total_inspected)
);

CREATE INDEX idx_qa_inspections_prod_entry ON qa_inspections(production_entry_id);
CREATE INDEX idx_qa_inspections_order ON qa_inspections(order_id);
CREATE INDEX idx_qa_inspections_date ON qa_inspections(inspection_date);
CREATE INDEX idx_qa_inspections_stage ON qa_inspections(inspection_stage);

-- =====================================================
-- QA INSPECTION FINDINGS
-- =====================================================
CREATE TABLE IF NOT EXISTS qa_inspection_findings (
    id                  BIGSERIAL PRIMARY KEY,
    inspection_id       BIGINT NOT NULL REFERENCES qa_inspections(id) ON DELETE CASCADE,
    defect_id           BIGINT NOT NULL REFERENCES qa_defect_catalog(id),
    quantity_affected   INTEGER NOT NULL DEFAULT 1,
    disposition         VARCHAR(20) NOT NULL DEFAULT 'REJECT',
    rework_instruction  TEXT,
    photo_urls          TEXT[],
    remarks             TEXT,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255)
);

CREATE INDEX idx_qa_findings_inspection ON qa_inspection_findings(inspection_id);
CREATE INDEX idx_qa_findings_defect ON qa_inspection_findings(defect_id);

-- =====================================================
-- QA REJECTIONS
-- =====================================================
CREATE TABLE IF NOT EXISTS qa_rejections (
    id                  BIGSERIAL PRIMARY KEY,
    rejection_number    VARCHAR(50) UNIQUE NOT NULL,
    inspection_id       BIGINT NOT NULL REFERENCES qa_inspections(id),
    production_entry_id UUID NOT NULL,
    production_item_id  UUID NOT NULL,
    order_id            UUID NOT NULL,
    order_item_id       UUID NOT NULL,
    heat_order_item_id  BIGINT,
    rejected_quantity   INTEGER NOT NULL,
    rejected_weight     DECIMAL(10,3),
    unit_weight         DECIMAL(10,3),
    material_grade      VARCHAR(30),
    primary_defect_id   BIGINT REFERENCES qa_defect_catalog(id),
    defect_summary      TEXT,
    disposition         VARCHAR(30) NOT NULL DEFAULT 'PENDING_REVIEW',
    disposition_date    DATE,
    disposition_by      VARCHAR(100),
    disposition_remarks TEXT,
    scrap_entry_id      BIGINT,
    status              VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255)
);

CREATE INDEX idx_qa_rejections_inspection ON qa_rejections(inspection_id);
CREATE INDEX idx_qa_rejections_order ON qa_rejections(order_id);
CREATE INDEX idx_qa_rejections_status ON qa_rejections(status);
CREATE INDEX idx_qa_rejections_disposition ON qa_rejections(disposition);

-- =====================================================
-- QA CUSTOMER RETURNS
-- =====================================================
CREATE TABLE IF NOT EXISTS qa_customer_returns (
    id                      BIGSERIAL PRIMARY KEY,
    return_number           VARCHAR(50) UNIQUE NOT NULL,
    customer_id             UUID NOT NULL,
    order_id                UUID NOT NULL,
    order_item_id           UUID NOT NULL,
    production_entry_id     UUID,
    heat_order_item_id      BIGINT,
    return_date             DATE NOT NULL,
    returned_quantity       INTEGER NOT NULL,
    returned_weight         DECIMAL(10,3),
    material_grade          VARCHAR(30),
    complaint_category      VARCHAR(30) NOT NULL,
    complaint_description   TEXT NOT NULL,
    customer_reference_no   VARCHAR(100),
    qa_assessment_date      DATE,
    qa_inspector_name       VARCHAR(100),
    qa_finding              VARCHAR(30),
    qa_remarks              TEXT,
    root_cause_category     VARCHAR(30),
    root_cause_description  TEXT,
    disposition             VARCHAR(30) NOT NULL DEFAULT 'PENDING_ASSESSMENT',
    disposition_date        DATE,
    disposition_by          VARCHAR(100),
    credit_amount           DECIMAL(12,2) DEFAULT 0,
    replacement_order_id    UUID,
    scrap_entry_id          BIGINT,
    inspection_id           BIGINT REFERENCES qa_inspections(id),
    status                  VARCHAR(20) NOT NULL DEFAULT 'RECEIVED',
    created_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by              VARCHAR(255),
    updated_by              VARCHAR(255)
);

CREATE INDEX idx_qa_returns_customer ON qa_customer_returns(customer_id);
CREATE INDEX idx_qa_returns_order ON qa_customer_returns(order_id);
CREATE INDEX idx_qa_returns_status ON qa_customer_returns(status);
CREATE INDEX idx_qa_returns_disposition ON qa_customer_returns(disposition);

-- =====================================================
-- QA TRACKING LOG
-- =====================================================
CREATE TABLE IF NOT EXISTS qa_tracking_log (
    id              BIGSERIAL PRIMARY KEY,
    reference_type  VARCHAR(20) NOT NULL,
    reference_id    BIGINT NOT NULL,
    from_status     VARCHAR(30),
    to_status       VARCHAR(30) NOT NULL,
    action          VARCHAR(50) NOT NULL,
    performed_by    VARCHAR(100) NOT NULL,
    remarks         TEXT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_qa_tracking_ref ON qa_tracking_log(reference_type, reference_id);
