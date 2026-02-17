CREATE TABLE IF NOT EXISTS metal_categories (
                                                id BIGSERIAL PRIMARY KEY,
                                                name VARCHAR(100) UNIQUE NOT NULL,
    active BOOLEAN DEFAULT TRUE
    );

CREATE TABLE IF NOT EXISTS metal_types (
                                           id BIGSERIAL PRIMARY KEY,
                                           category_id BIGINT NOT NULL,
                                           name VARCHAR(100) NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (category_id) REFERENCES metal_categories(id) ON DELETE CASCADE,
    UNIQUE (category_id, name)
    );

INSERT INTO metal_categories (name, active) VALUES
                                                ('Ferrous', true),
                                                ('Non-Ferrous', true),
                                                ('Alloy Steel', true)
    ON CONFLICT (name) DO NOTHING;
