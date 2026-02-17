-- V7: Create Quotation Tables (UUID version)

-- Create quotations table
CREATE TABLE IF NOT EXISTS quotations (
                                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id BIGINT NOT NULL,
    quotation_number VARCHAR(50) UNIQUE NOT NULL,
    quotation_date DATE,
    valid_until DATE,
    revision_no INTEGER DEFAULT 0,
    customer_id UUID NOT NULL,
    enquiry_id UUID,
    status VARCHAR(20) DEFAULT 'DRAFT',
    sub_total DECIMAL(19,2),
    discount DECIMAL(19,2),
    tax DECIMAL(19,2),
    total_amount DECIMAL(19,2),
    payment_terms VARCHAR(500),
    delivery_terms VARCHAR(500),
    delivery_location VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    FOREIGN KEY (customer_id) REFERENCES customer(id),
    FOREIGN KEY (enquiry_id) REFERENCES enquiry(id)
    );

-- Create quotation_items table
CREATE TABLE IF NOT EXISTS quotation_items (
                                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id BIGINT NOT NULL,
    quotation_id UUID NOT NULL,
    product_name VARCHAR(255),
    description TEXT,
    hsn_sac_code VARCHAR(20),
    part_name VARCHAR(255),
    drawing_number VARCHAR(100),
    material_grade VARCHAR(100),
    net_weight_kg DECIMAL(10,3),
    gross_weight_kg DECIMAL(10,3),
    pattern_status VARCHAR(20),
    quantity DECIMAL(15,3),
    unit VARCHAR(20),
    unit_price DECIMAL(19,2),
    discount_percent DECIMAL(5,2) DEFAULT 0,
    tax_percent DECIMAL(5,2) DEFAULT 0,
    total_price DECIMAL(19,2),
    line_total DECIMAL(19,2),
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    FOREIGN KEY (quotation_id) REFERENCES quotations(id) ON DELETE CASCADE
    );

-- Create quotation_costs table
CREATE TABLE IF NOT EXISTS quotation_costs (
                                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id BIGINT NOT NULL,
    quotation_item_id UUID UNIQUE NOT NULL,
    metal_cost DECIMAL(19,2),
    moulding_cost DECIMAL(19,2),
    melting_cost DECIMAL(19,2),
    machining_cost DECIMAL(19,2),
    overhead_cost DECIMAL(19,2),
    total_cost DECIMAL(19,2),
    margin_percent DECIMAL(5,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    FOREIGN KEY (quotation_item_id) REFERENCES quotation_items(id) ON DELETE CASCADE
    );

-- Create quotation_revisions table
CREATE TABLE IF NOT EXISTS quotation_revisions (
                                                   id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id BIGINT NOT NULL,
    quotation_id UUID NOT NULL,
    revision_no INTEGER NOT NULL,
    revision_date DATE NOT NULL,
    reason VARCHAR(500),
    previous_total DECIMAL(19,2),
    revised_total DECIMAL(19,2),
    changed_by VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    FOREIGN KEY (quotation_id) REFERENCES quotations(id),
    UNIQUE(quotation_id, revision_no)
    );

-- Create quotation_approvals table
CREATE TABLE IF NOT EXISTS quotation_approvals (
                                                   id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id BIGINT NOT NULL,
    quotation_id UUID NOT NULL,
    approval_level INTEGER NOT NULL,
    approver_id UUID,
    approver_name VARCHAR(255),
    status VARCHAR(20) DEFAULT 'PENDING',
    comments VARCHAR(1000),
    action_date TIMESTAMP,
    requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    FOREIGN KEY (quotation_id) REFERENCES quotations(id)
    );

-- Create indexes
CREATE INDEX idx_quotations_tenant ON quotations(tenant_id);
CREATE INDEX idx_quotations_customer ON quotations(customer_id);
CREATE INDEX idx_quotations_enquiry ON quotations(enquiry_id);
CREATE INDEX idx_quotations_status ON quotations(status);
CREATE INDEX idx_quotations_date ON quotations(quotation_date);
CREATE INDEX idx_quotations_number ON quotations(quotation_number);
CREATE INDEX idx_quotation_items_quotation ON quotation_items(quotation_id);
CREATE INDEX idx_quotation_items_tenant ON quotation_items(tenant_id);
CREATE INDEX idx_quotation_costs_item ON quotation_costs(quotation_item_id);
CREATE INDEX idx_quotation_revisions_quotation ON quotation_revisions(quotation_id);
CREATE INDEX idx_quotation_approvals_quotation ON quotation_approvals(quotation_id);
CREATE INDEX idx_quotation_approvals_status ON quotation_approvals(status);