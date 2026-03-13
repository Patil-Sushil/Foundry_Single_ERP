-- Migration: Create Stock Adjustment Tables
-- Version: V17

CREATE TABLE stock_adjustments (
    id BIGSERIAL PRIMARY KEY,
    adjustment_number VARCHAR(50) NOT NULL UNIQUE,
    adjustment_date DATE NOT NULL,
    reason VARCHAR(255) NOT NULL,
    adjusted_by_user_id UUID,
    
    -- Audit Columns from BaseInventoryEntity
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE TABLE adjustment_items (
    id BIGSERIAL PRIMARY KEY,
    stock_adjustment_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    adjusted_quantity DECIMAL(15, 3) NOT NULL,
    unit_rate DECIMAL(12, 2), -- Nullable as per AdjustmentItem entity
    
    -- Audit Columns from BaseInventoryEntity
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),

    CONSTRAINT fk_adjustment_items_adjustment FOREIGN KEY (stock_adjustment_id) 
        REFERENCES stock_adjustments(id) ON DELETE CASCADE,
    CONSTRAINT fk_adjustment_items_item FOREIGN KEY (item_id) 
        REFERENCES items(id)
);

-- Indexes for performance
CREATE INDEX idx_stock_adjustments_number ON stock_adjustments(adjustment_number);
CREATE INDEX idx_adjustment_items_adjustment_id ON adjustment_items(stock_adjustment_id);
CREATE INDEX idx_adjustment_items_item_id ON adjustment_items(item_id);
