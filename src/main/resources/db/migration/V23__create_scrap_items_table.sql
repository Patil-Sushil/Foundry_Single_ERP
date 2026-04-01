-- =====================================================
-- SCRAP ITEMS - Line items within a scrap entry
-- =====================================================
CREATE TABLE scrap_items (
    id                      BIGSERIAL PRIMARY KEY,
    scrap_entry_id          BIGINT NOT NULL,
    
    -- Item identification
    item_id                 BIGINT,
    item_name               VARCHAR(255) NOT NULL,
    item_code               VARCHAR(50),
    grade                   VARCHAR(50),
    
    -- Scrap type classification
    scrap_type              VARCHAR(30),
    
    -- Quantity
    quantity                INTEGER,
    weight                  DECIMAL(15,3) NOT NULL,
    
    -- Cost tracking
    unit_cost               DECIMAL(12,2),
    total_cost              DECIMAL(15,2),
    
    -- Defect information (for rejections)
    defect_type             VARCHAR(50),
    
    -- Disposition
    recyclability           VARCHAR(20) NOT NULL,
    destination             VARCHAR(30),
    
    -- Links to destination records
    material_inward_id      BIGINT,
    scrap_sale_id           BIGINT,
    
    -- Inventory status
    in_inventory            BOOLEAN DEFAULT FALSE,
    inventory_item_id       BIGINT,
    
    -- Link back to inspection defect (if from rejection)
    inspection_defect_id    BIGINT,
    
    -- Audit
    created_at              TIMESTAMP DEFAULT NOW(),
    
    -- Foreign Keys
    CONSTRAINT fk_scrap_item_entry
        FOREIGN KEY (scrap_entry_id) REFERENCES scrap_entries(id) ON DELETE CASCADE,
    
    CONSTRAINT fk_scrap_item_inventory_item
        FOREIGN KEY (inventory_item_id) REFERENCES items(id)
);

-- Indexes
CREATE INDEX idx_scrap_items_entry ON scrap_items(scrap_entry_id);
CREATE INDEX idx_scrap_items_inventory ON scrap_items(inventory_item_id);

-- Comments
COMMENT ON TABLE scrap_items IS 'Detailed items within a scrap entry, linking specific products or components to their scrap disposition.';
