-- V9: Material Issue
-- =============================================

-- -----------------------------
-- MATERIAL ISSUES
-- -----------------------------
CREATE TABLE material_issues (
    id BIGSERIAL PRIMARY KEY,
    issue_number VARCHAR(50) NOT NULL UNIQUE,
    department_id BIGINT NOT NULL REFERENCES departments(id),
    issued_by_user_id UUID, -- UUID
    issue_date DATE NOT NULL DEFAULT CURRENT_DATE,
    purpose VARCHAR(500),
    notes TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

-- -----------------------------
-- ISSUED ITEMS
-- -----------------------------
CREATE TABLE issued_items (
    id BIGSERIAL PRIMARY KEY,
    material_issue_id BIGINT NOT NULL REFERENCES material_issues(id) ON DELETE CASCADE,
    item_id BIGINT NOT NULL REFERENCES items(id),
    issued_quantity DECIMAL(15,3) NOT NULL,
    unit_rate DECIMAL(12,2) NOT NULL,
    amount DECIMAL(15,2) GENERATED ALWAYS AS (issued_quantity * unit_rate) STORED,
    notes VARCHAR(500)
);

-- -----------------------------
-- INDEXES
-- -----------------------------
CREATE INDEX idx_issues_department ON material_issues(department_id);
CREATE INDEX idx_issues_date ON material_issues(issue_date);
CREATE INDEX idx_issues_issued_by ON material_issues(issued_by_user_id);
CREATE INDEX idx_issued_items_issue ON issued_items(material_issue_id);
CREATE INDEX idx_issued_items_item ON issued_items(item_id);
