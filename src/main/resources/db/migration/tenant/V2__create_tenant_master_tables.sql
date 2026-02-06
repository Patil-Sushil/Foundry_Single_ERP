CREATE TABLE IF NOT EXISTS tenant_master (
                                                    id BIGSERIAL PRIMARY KEY,
                                                    tenant_id BIGINT NOT NULL,
                                                    master_type VARCHAR(50) NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    active BOOLEAN DEFAULT TRUE,

    CONSTRAINT fk_tenant_master_tenant
    FOREIGN KEY (tenant_id)
    REFERENCES public.tenant(id)
    ON DELETE CASCADE
    );
