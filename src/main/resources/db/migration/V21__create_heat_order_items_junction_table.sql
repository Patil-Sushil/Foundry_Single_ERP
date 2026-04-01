-- =====================================================
-- JUNCTION TABLE: heat_order_items
-- Links furnace heats to order items (Many-to-Many)
-- One heat can produce castings for multiple order items
-- =====================================================

CREATE TABLE heat_order_items (
    id                      BIGSERIAL PRIMARY KEY,
    
    -- Parent heat
    heat_id                 BIGINT NOT NULL,
    
    -- Order item being produced (NULL for stock production)
    order_item_id           UUID,
    
    -- Production details
    quantity_produced       INTEGER NOT NULL,        -- Number of pieces cast
    weight_produced         DECIMAL(15,3) NOT NULL,  -- Total weight in kg
    piece_weight            DECIMAL(10,3),           -- Weight per piece (optional)
    
    -- For stock production (when order_item_id is NULL)
    stock_item_name         VARCHAR(255),            -- e.g., "Bushing Type-A"
    stock_item_code         VARCHAR(50),             -- Internal code
    
    -- Notes
    remarks                 TEXT,
    
    -- Audit
    created_at              TIMESTAMP DEFAULT NOW(),
    created_by              VARCHAR(100),
    updated_at              TIMESTAMP,
    updated_by              VARCHAR(100),
    
    -- Foreign Keys
    CONSTRAINT fk_heat_order_item_heat 
        FOREIGN KEY (heat_id) REFERENCES furnace_heats(id) ON DELETE CASCADE,
    
    CONSTRAINT fk_heat_order_item_order_item 
        FOREIGN KEY (order_item_id) REFERENCES order_items(id),
    
    -- Business Rules
    CONSTRAINT check_item_source CHECK (
        (order_item_id IS NOT NULL) OR 
        (stock_item_name IS NOT NULL AND stock_item_name != '')
    ),
    
    CONSTRAINT check_quantity_positive CHECK (quantity_produced > 0),
    CONSTRAINT check_weight_positive CHECK (weight_produced > 0)
);

-- Indexes
CREATE INDEX idx_heat_order_items_heat ON heat_order_items(heat_id);
CREATE INDEX idx_heat_order_items_order_item ON heat_order_items(order_item_id);
CREATE INDEX idx_heat_order_items_created_at ON heat_order_items(created_at);

-- Comments
COMMENT ON TABLE heat_order_items IS 
'Junction table linking furnace heats to order items. One heat can produce castings for multiple order items. For stock production, order_item_id is NULL.';

COMMENT ON COLUMN heat_order_items.order_item_id IS 
'FK to order_items. NULL for stock production (non-order based). If not NULL, the order_item.material_grade MUST match heat.grade (validated in application).';

COMMENT ON COLUMN heat_order_items.quantity_produced IS 
'Number of pieces cast in this heat for this order item. Gross count (including runners/risers attached).';

COMMENT ON COLUMN heat_order_items.weight_produced IS 
'Total casting weight for this order item in this heat (excluding runners/risers which are tracked separately in furnace_heats.runner_weight).';

COMMENT ON COLUMN heat_order_items.stock_item_name IS 
'Item name for stock production (when no order exists). Required if order_item_id is NULL.';
