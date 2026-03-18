-- ================================================================
-- V18: Production Module — Daily Production Tracking Per Order
-- Pipeline: Core Making → Pouring → Knockout → Shot Blasting
--                       → Fettling → Dispatch
-- ================================================================

CREATE TABLE production_entries (
                                    id UUID PRIMARY KEY,
                                    entry_number VARCHAR(30) NOT NULL UNIQUE,
                                    order_id UUID NOT NULL,
                                    report_date DATE NOT NULL,
                                    shift VARCHAR(20) NOT NULL,
                                    status VARCHAR(30) NOT NULL DEFAULT 'IN_PROGRESS',
                                    operator_name VARCHAR(100),
                                    remarks TEXT,

                                    total_ready_cores INTEGER NOT NULL DEFAULT 0,
                                    total_poured_moulds INTEGER NOT NULL DEFAULT 0,
                                    total_shot_blasting_quantity INTEGER NOT NULL DEFAULT 0,
                                    total_fettling_quantity INTEGER NOT NULL DEFAULT 0,
                                    total_dispatched_quantity INTEGER NOT NULL DEFAULT 0,

                                    created_at TIMESTAMP DEFAULT NOW(),
                                    updated_at TIMESTAMP,
                                    created_by VARCHAR(100),
                                    updated_by VARCHAR(100),
                                    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,

                                    CONSTRAINT fk_prod_entry_order FOREIGN KEY (order_id) REFERENCES orders(id),

                                    CONSTRAINT uq_prod_order_date_shift UNIQUE (order_id, report_date, shift),

                                    CONSTRAINT chk_entry_shift CHECK (shift IN ('MORNING','AFTERNOON','NIGHT')),
                                    CONSTRAINT chk_entry_status CHECK (status IN ('IN_PROGRESS','COMPLETED','ON_HOLD','CANCELLED')),

                                    CONSTRAINT chk_entry_cores_gte_zero CHECK (total_ready_cores >= 0),
                                    CONSTRAINT chk_entry_poured_gte_zero CHECK (total_poured_moulds >= 0),
                                    CONSTRAINT chk_entry_shot_gte_zero CHECK (total_shot_blasting_quantity >= 0),
                                    CONSTRAINT chk_entry_fettling_gte_zero CHECK (total_fettling_quantity >= 0),
                                    CONSTRAINT chk_entry_dispatch_gte_zero CHECK (total_dispatched_quantity >= 0)
);

CREATE TABLE production_items (
                                  id UUID PRIMARY KEY,
                                  production_entry_id UUID NOT NULL,
                                  order_item_id UUID NOT NULL,

                                  item_name VARCHAR(200) NOT NULL,
                                  pattern_number VARCHAR(100),
                                  ordered_quantity INTEGER NOT NULL,

                                  ready_cores INTEGER NOT NULL DEFAULT 0,
                                  poured_moulds INTEGER NOT NULL DEFAULT 0,
                                  shot_blasting_quantity INTEGER NOT NULL DEFAULT 0,
                                  fettling_quantity INTEGER NOT NULL DEFAULT 0,
                                  dispatched_quantity INTEGER NOT NULL DEFAULT 0,

                                  item_remark VARCHAR(500),

                                  created_at TIMESTAMP DEFAULT NOW(),
                                  updated_at TIMESTAMP,
                                  created_by VARCHAR(100),
                                  updated_by VARCHAR(100),
                                  is_deleted BOOLEAN NOT NULL DEFAULT FALSE,

                                  CONSTRAINT fk_prod_item_entry FOREIGN KEY (production_entry_id)
                                      REFERENCES production_entries(id) ON DELETE CASCADE,

                                  CONSTRAINT fk_prod_item_order_item FOREIGN KEY (order_item_id)
                                      REFERENCES order_item(id),

                                  CONSTRAINT chk_item_cores CHECK (ready_cores >= 0),
                                  CONSTRAINT chk_item_poured CHECK (poured_moulds >= 0 AND poured_moulds <= ready_cores),
                                  CONSTRAINT chk_item_shot CHECK (shot_blasting_quantity >= 0),
                                  CONSTRAINT chk_item_fettling CHECK (fettling_quantity >= 0),
                                  CONSTRAINT chk_item_dispatch CHECK (dispatched_quantity >= 0)
);

-- INDEXES
CREATE INDEX idx_prod_entries_order ON production_entries(order_id);
CREATE INDEX idx_prod_entries_date ON production_entries(report_date);
CREATE INDEX idx_prod_entries_status ON production_entries(status);
CREATE INDEX idx_prod_entries_deleted ON production_entries(is_deleted);

CREATE INDEX idx_prod_items_entry ON production_items(production_entry_id);
CREATE INDEX idx_prod_items_order_item ON production_items(order_item_id);
CREATE INDEX idx_prod_items_deleted ON production_items(is_deleted);