-- =====================================================
-- SCRAP ENTRIES - Main scrap tracking table
-- =====================================================
CREATE TABLE scrap_entries (
    id                      BIGSERIAL PRIMARY KEY,
    scrap_number            VARCHAR(50) UNIQUE NOT NULL,
    scrap_date              DATE NOT NULL DEFAULT CURRENT_DATE,
    
    -- Source identification
    scrap_source            VARCHAR(30) NOT NULL,
    source_reference_id     VARCHAR(100),
    source_reference_type   VARCHAR(50),
    
    -- Links to source records
    heat_id                 BIGINT,
    inspection_id           BIGINT,
    customer_return_id      BIGINT,
    
    -- Material details
    grade                   VARCHAR(50),
    total_weight            DECIMAL(15,3) NOT NULL,
    total_value             DECIMAL(15,2) DEFAULT 0,
    
    -- Verification workflow
    confidence_level        VARCHAR(20) DEFAULT 'UNKNOWN',
    verification_method     VARCHAR(30),
    physical_condition      VARCHAR(30),
    visual_grade_assessment VARCHAR(50),
    requires_testing        BOOLEAN DEFAULT FALSE,
    
    -- Verification details
    verified_by             VARCHAR(100),
    verified_at             TIMESTAMP,
    verification_notes      TEXT,
    
    -- Approval details
    approved_by             VARCHAR(100),
    approved_at             TIMESTAMP,
    approval_decision       VARCHAR(30),
    approval_notes          TEXT,
    final_grade             VARCHAR(50),
    
    -- Rejection details (if not approved)
    rejection_reason        TEXT,
    
    -- Inward confirmation (when returned to inventory)
    inward_confirmed_by     VARCHAR(100),
    inward_confirmed_at     TIMESTAMP,
    
    -- Links to destination records
    material_inward_id      BIGINT,
    inventory_item_id       BIGINT,
    scrap_sale_id           BIGINT,
    
    -- Status
    status                  VARCHAR(30) DEFAULT 'PENDING_VERIFICATION' NOT NULL,
    remarks                 TEXT,
    
    -- Audit
    created_at              TIMESTAMP DEFAULT NOW(),
    created_by              VARCHAR(100),
    updated_at              TIMESTAMP,
    updated_by              VARCHAR(100),
    
    -- Constraints
    CONSTRAINT check_scrap_source CHECK (scrap_source IN (
        'PROCESS_SCRAP', 
        'PRODUCTION_REJECTION', 
        'CUSTOMER_RETURN', 
        'UNKNOWN_YARD_SCRAP'
    )),
    CONSTRAINT check_confidence_level CHECK (confidence_level IN (
        'HIGH', 'MEDIUM', 'LOW', 'VERIFIED', 'UNKNOWN'
    )),
    CONSTRAINT check_verification_method CHECK (verification_method IS NULL OR verification_method IN (
        'AUTO_FROM_HEAT', 
        'VISUAL_INSPECTION', 
        'SPECTROMETER_TEST', 
        'ORDER_LINKAGE', 
        'PHYSICAL_MARKING', 
        'DEFAULT_MIXED'
    )),
    CONSTRAINT check_physical_condition CHECK (physical_condition IS NULL OR physical_condition IN (
        'CLEAN', 'RUSTY', 'SAND_INCLUSION', 'PAINTED', 'DAMAGED', 'MIXED_GRADES'
    )),
    CONSTRAINT check_approval_decision CHECK (approval_decision IS NULL OR approval_decision IN (
        'APPROVE_REMELT', 'APPROVE_MIXED', 'REJECT_TO_SELL', 'REJECT_CONTAMINATED'
    )),
    CONSTRAINT check_scrap_status CHECK (status IN (
        'PENDING_VERIFICATION', 
        'VERIFIED', 
        'APPROVED_FOR_RETURN', 
        'REJECTED_FOR_RETURN', 
        'RETURNED_TO_INVENTORY', 
        'SOLD', 
        'DISPOSED'
    ))
);

-- Indexes
CREATE INDEX idx_scrap_source ON scrap_entries(scrap_source);
CREATE INDEX idx_scrap_status ON scrap_entries(status);
CREATE INDEX idx_scrap_date ON scrap_entries(scrap_date);
CREATE INDEX idx_scrap_grade ON scrap_entries(grade);
CREATE INDEX idx_scrap_heat ON scrap_entries(heat_id);
CREATE INDEX idx_scrap_inspection ON scrap_entries(inspection_id);
CREATE INDEX idx_scrap_inward ON scrap_entries(material_inward_id);

-- Add FK from furnace_heats now that scrap_entries exists
ALTER TABLE furnace_heats
ADD CONSTRAINT fk_heat_scrap_entry 
FOREIGN KEY (process_scrap_entry_id) REFERENCES scrap_entries(id);

-- Comments
COMMENT ON TABLE scrap_entries IS 
'Main table for tracking all scrap material in the foundry. Each entry represents a batch of scrap from a specific source with verification and approval workflow.';

COMMENT ON COLUMN scrap_entries.scrap_source IS 
'Origin: PROCESS_SCRAP (runners/risers), PRODUCTION_REJECTION (QA failures), CUSTOMER_RETURN (returned goods), UNKNOWN_YARD_SCRAP (found material)';

COMMENT ON COLUMN scrap_entries.confidence_level IS 
'How confident we are about the grade: HIGH (from heat), MEDIUM (visual), LOW (unknown), VERIFIED (lab tested)';

COMMENT ON COLUMN scrap_entries.status IS 
'Workflow: PENDING_VERIFICATION → VERIFIED → APPROVED_FOR_RETURN → RETURNED_TO_INVENTORY. Or REJECTED_FOR_RETURN → SOLD/DISPOSED';
