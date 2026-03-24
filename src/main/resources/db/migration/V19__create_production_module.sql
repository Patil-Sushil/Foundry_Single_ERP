-- ================================================================
-- V19: Production Module — Daily Production Tracking Per Order
-- Pipeline: Core Making → Pouring → Shot Blasting → Fettling → Dispatch
--
-- DESIGN NOTES:
--   • One production_entry per (order, date, shift) — enforced by unique constraint
--   • Each entry has N production_items, one per order_item being worked on
--   • Pipeline quantities are PER-DAY values (not cumulative)
--   • Cumulative totals are calculated at query time via SUM across entries
--   • Per-row constraints only enforce >= 0 (pipeline order is validated in app)
-- ================================================================
-- ────────────────────────────────────────────────────────────
-- TABLE: production_entries
-- One row = one shift's production work on one order
-- ────────────────────────────────────────────────────────────

CREATE TABLE production_entries
(
    id                          UUID         PRIMARY KEY,
    entry_number                VARCHAR(30)  NOT NULL UNIQUE,

    -- which order this entry is for
    order_id                    UUID         NOT NULL,

    -- when and which shift
    report_date                 DATE         NOT NULL,
    shift                       VARCHAR(20)  NOT NULL,

    -- workflow status
    status                      VARCHAR(30)  NOT NULL DEFAULT 'IN_PROGRESS',

    -- who operated
    operator_name               VARCHAR(100),
    remarks                     TEXT,

    -- ── ENTRY-LEVEL TOTALS (sum of all items in this entry) ──
    total_ready_cores           INTEGER      NOT NULL DEFAULT 0,
    total_poured_moulds         INTEGER      NOT NULL DEFAULT 0,
    total_shot_blasting_quantity INTEGER     NOT NULL DEFAULT 0,
    total_fettling_quantity     INTEGER      NOT NULL DEFAULT 0,
    total_dispatched_quantity   INTEGER      NOT NULL DEFAULT 0,

    -- ── AUDIT ──
    created_at                  TIMESTAMP    DEFAULT NOW(),
    updated_at                  TIMESTAMP,
    created_by                  VARCHAR(100),
    updated_by                  VARCHAR(100),

    -- ── SOFT DELETE ──
    is_deleted                  BOOLEAN      NOT NULL DEFAULT FALSE,

    -- ── FOREIGN KEYS ──
    CONSTRAINT fk_prod_entry_order
        FOREIGN KEY (order_id) REFERENCES orders (id),


    -- ── ENUM CHECKS ──
    CONSTRAINT chk_entry_shift
        CHECK (shift IN ('MORNING', 'AFTERNOON', 'NIGHT')),

    CONSTRAINT chk_entry_status
        CHECK (status IN ('IN_PROGRESS', 'COMPLETED', 'ON_HOLD', 'CANCELLED')),

    -- ── NON-NEGATIVE CHECKS ──
    CONSTRAINT chk_entry_cores_gte_zero
        CHECK (total_ready_cores >= 0),

    CONSTRAINT chk_entry_poured_gte_zero
        CHECK (total_poured_moulds >= 0),

    CONSTRAINT chk_entry_shot_gte_zero
        CHECK (total_shot_blasting_quantity >= 0),

    CONSTRAINT chk_entry_fettling_gte_zero
        CHECK (total_fettling_quantity >= 0),

    CONSTRAINT chk_entry_dispatch_gte_zero
        CHECK (total_dispatched_quantity >= 0)
);


-- ────────────────────────────────────────────────────────────
-- TABLE: production_items
-- One row = one order_item's production quantities for that shift
--
-- IMPORTANT: These are TODAY's values, not cumulative.
-- Example: If ordered 100 castings, and today we made 10 cores,
--          ready_cores = 10 (not 10 + previous days)
-- ────────────────────────────────────────────────────────────

CREATE TABLE production_items
(
    id                      UUID         PRIMARY KEY,

    -- parent entry
    production_entry_id     UUID         NOT NULL,

    -- which order item this tracks
    order_item_id           UUID         NOT NULL,

    -- denormalized for quick display (copied from order_item.part_name)
    item_name               VARCHAR(200) NOT NULL,

    -- pattern used (optional — not all items need patterns)
    pattern_id              UUID,

    -- denormalized from order_item.quantity for quick reference
    ordered_quantity        INTEGER      NOT NULL,

    -- ── PIPELINE QUANTITIES (today's values) ──
    ready_cores             INTEGER      NOT NULL DEFAULT 0,
    poured_moulds           INTEGER      NOT NULL DEFAULT 0,
    shot_blasting_quantity  INTEGER      NOT NULL DEFAULT 0,
    fettling_quantity       INTEGER      NOT NULL DEFAULT 0,
    dispatched_quantity     INTEGER      NOT NULL DEFAULT 0,

    -- per-item notes
    item_remark             VARCHAR(500),

    -- ── AUDIT ──
    created_at              TIMESTAMP    DEFAULT NOW(),
    updated_at              TIMESTAMP,
    created_by              VARCHAR(100),
    updated_by              VARCHAR(100),

    -- ── SOFT DELETE ──
    is_deleted              BOOLEAN      NOT NULL DEFAULT FALSE,

    -- ── FOREIGN KEYS ──
    CONSTRAINT fk_prod_item_entry
        FOREIGN KEY (production_entry_id)
            REFERENCES production_entries (id) ON DELETE CASCADE,

    CONSTRAINT fk_prod_item_order_item
        FOREIGN KEY (order_item_id)
            REFERENCES order_items (id),

    CONSTRAINT fk_production_item_pattern
        FOREIGN KEY (pattern_id)
            REFERENCES patterns (id),

    -- ── NON-NEGATIVE CHECKS (no cross-column comparison) ──
    --
    -- WHY no "poured_moulds <= ready_cores" check?
    -- Because these are DAILY values. On Day 2, you might pour 20 moulds
    -- from Day 1's cores while only making 5 new cores today.
    -- Pipeline order is enforced CUMULATIVELY in application code.
    --
    CONSTRAINT chk_item_cores
        CHECK (ready_cores >= 0),

    CONSTRAINT chk_item_poured
        CHECK (poured_moulds >= 0),

    CONSTRAINT chk_item_shot
        CHECK (shot_blasting_quantity >= 0),

    CONSTRAINT chk_item_fettling
        CHECK (fettling_quantity >= 0),

    CONSTRAINT chk_item_dispatch
        CHECK (dispatched_quantity >= 0),

    CONSTRAINT chk_item_ordered_qty
        CHECK (ordered_quantity > 0)
);


-- ────────────────────────────────────────────────────────────
-- INDEXES: production_entries
-- ────────────────────────────────────────────────────────────

-- lookup by order
CREATE INDEX idx_prod_entries_order
    ON production_entries (order_id);

-- date-based queries (daily/monthly reports)
CREATE INDEX idx_prod_entries_date
    ON production_entries (report_date);

-- status filtering
CREATE INDEX idx_prod_entries_status
    ON production_entries (status);

-- Partial unique index — only enforced on active (non-deleted) rows
CREATE UNIQUE INDEX uq_prod_order_date_shift_active
    ON production_entries (order_id, report_date, shift)
    WHERE is_deleted = FALSE;

-- soft-delete filter (partial index for active records)
CREATE INDEX idx_prod_entries_active
    ON production_entries (id)
    WHERE is_deleted = FALSE;

-- composite: order + date range queries
CREATE INDEX idx_prod_entries_order_date
    ON production_entries (order_id, report_date);

-- composite: date + soft-delete (most common query pattern)
CREATE INDEX idx_prod_entries_date_active
    ON production_entries (report_date)
    WHERE is_deleted = FALSE;


-- ────────────────────────────────────────────────────────────
-- INDEXES: production_items
-- ────────────────────────────────────────────────────────────

-- lookup by entry (loading items for an entry)
CREATE INDEX idx_prod_items_entry
    ON production_items (production_entry_id);

-- lookup by order_item (cumulative totals across all entries)
CREATE INDEX idx_prod_items_order_item
    ON production_items (order_item_id);

-- soft-delete filter
CREATE INDEX idx_prod_items_active
    ON production_items (id)
    WHERE is_deleted = FALSE;

-- composite: order_item + soft-delete (pipeline totals query)
CREATE INDEX idx_prod_items_order_item_active
    ON production_items (order_item_id)
    WHERE is_deleted = FALSE;

-- pattern lookup
CREATE INDEX idx_prod_items_pattern
    ON production_items (pattern_id)
    WHERE pattern_id IS NOT NULL;

CREATE TABLE electricity_rates (
    id BIGSERIAL PRIMARY KEY ,
    rate_per_unit DOUBLE PRECISION NOT NULL DEFAULT 0,
    effective_from DATE NOT NULL ,
    effective_to DATE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP
)