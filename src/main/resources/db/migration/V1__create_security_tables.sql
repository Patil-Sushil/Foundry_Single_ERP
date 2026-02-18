-- =============================================
-- V1: Security & Audit Tables (UUID Version)
-- =============================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- -----------------------------
-- ROLE TABLE
-- -----------------------------
CREATE TABLE IF NOT EXISTS role (
                                    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200)
    );

-- -----------------------------
-- USERS TABLE
-- -----------------------------
CREATE TABLE IF NOT EXISTS users (
                                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100),
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    deleted BOOLEAN DEFAULT FALSE,   -- ✅ SOFT DELETE COLUMN
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- -----------------------------
-- USER ROLE MAPPING
-- -----------------------------
CREATE TABLE IF NOT EXISTS user_role (
                                         user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES role(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
    );

-- -----------------------------
-- AUDIT LOG
-- -----------------------------
CREATE TABLE IF NOT EXISTS audit_log (
                                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id),
    action VARCHAR(100) NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- -----------------------------
-- DEFAULT ROLES SEED
-- -----------------------------
INSERT INTO role (name, description) VALUES
                                         ('ADMIN', 'Full system access'),
                                         ('SALES', 'Handles enquiry and quotations'),
                                         ('DESIGN', 'Costing and technical review'),
                                         ('FINANCE', 'Approves quotations'),
                                         ('PRODUCTION', 'Production planning and tracking')
    ON CONFLICT (name) DO NOTHING;
