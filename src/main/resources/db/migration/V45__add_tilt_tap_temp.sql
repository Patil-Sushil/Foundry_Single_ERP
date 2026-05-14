ALTER TABLE furnace_heats ADD COLUMN tilting_temp DOUBLE PRECISION DEFAULT 0;
ALTER TABLE furnace_heats ADD COLUMN tapping_temp DOUBLE PRECISION DEFAULT 0;

COMMENT ON COLUMN furnace_heats.tilting_temp IS 'added tilting temp ';
COMMENT ON COLUMN furnace_heats.tapping_temp IS 'added tapping temp ';

