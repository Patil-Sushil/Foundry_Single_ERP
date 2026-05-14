-- =============================================
-- V44: Create Casting Process Master Table
-- =============================================

CREATE TABLE IF NOT EXISTS casting_processes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL UNIQUE,
    code VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

-- Insert Default Records
INSERT INTO casting_processes (name, code) VALUES
('Green Sand Casting', 'GREEN_SAND'),
('Dry Sand Casting', 'DRY_SAND'),
('CO2 Sand Casting', 'CO2_SAND'),
('No Bake Sand Casting', 'NO_BAKE_SAND'),
('Resin Sand Casting', 'RESIN_SAND'),
('Shell Molding', 'SHELL_MOLDING'),
('Loam Sand Casting', 'LOAM_SAND'),
('Floor Molding', 'FLOOR_MOLDING'),
('Pit Molding', 'PIT_MOLDING'),
('Die Casting', 'DIE_CASTING'),
('Gravity Die Casting', 'GRAVITY_DIE_CASTING'),
('Pressure Die Casting', 'PRESSURE_DIE_CASTING'),
('Low Pressure Die Casting', 'LOW_PRESSURE_DIE_CASTING'),
('High Pressure Die Casting', 'HIGH_PRESSURE_DIE_CASTING'),
('Investment Casting', 'INVESTMENT_CASTING'),
('Lost Wax Casting', 'LOST_WAX_CASTING'),
('Lost Foam Casting', 'LOST_FOAM_CASTING'),
('Permanent Mold Casting', 'PERMANENT_MOLD_CASTING'),
('Slush Casting', 'SLUSH_CASTING'),
('Centrifugal Casting', 'CENTRIFUGAL_CASTING'),
('Semi-Centrifugal Casting', 'SEMI_CENTRIFUGAL_CASTING'),
('Centrifuge Casting', 'CENTRIFUGE_CASTING'),
('Plaster Mold Casting', 'PLASTER_MOLD_CASTING'),
('Ceramic Mold Casting', 'CERAMIC_MOLD_CASTING'),
('Vacuum Casting', 'VACUUM_CASTING'),
('Continuous Casting', 'CONTINUOUS_CASTING'),
('Squeeze Casting', 'SQUEEZE_CASTING'),
('Tilt Casting', 'TILT_CASTING'),
('Machine Molding', 'MACHINE_MOLDING'),
('Hand Molding', 'HAND_MOLDING'),
('Jolt Squeeze Molding', 'JOLT_SQUEEZE_MOLDING'),
('Air Set Casting', 'AIR_SET_CASTING')
ON CONFLICT (code) DO NOTHING;

-- Add casting_process_id to enquiry_item
ALTER TABLE enquiry_item ADD COLUMN casting_process_id UUID REFERENCES casting_processes(id);

-- Add casting_process_id to quotation_items
ALTER TABLE quotation_items ADD COLUMN casting_process_id UUID REFERENCES casting_processes(id);

-- Add casting_process_id to order_items
ALTER TABLE order_items ADD COLUMN casting_process_id UUID REFERENCES casting_processes(id);

-- Migrate existing data (Optional but recommended)
UPDATE enquiry_item ei
SET casting_process_id = cp.id
FROM casting_processes cp
WHERE UPPER(ei.casting_process) = cp.code;

UPDATE quotation_items qi
SET casting_process_id = cp.id
FROM casting_processes cp
WHERE UPPER(qi.casting_process) = cp.code;

UPDATE order_items oi
SET casting_process_id = cp.id
FROM casting_processes cp
WHERE UPPER(oi.casting_process) = cp.code;

-- Remove old columns
ALTER TABLE enquiry_item DROP COLUMN casting_process;
ALTER TABLE quotation_items DROP COLUMN casting_process;
ALTER TABLE order_items DROP COLUMN casting_process;
