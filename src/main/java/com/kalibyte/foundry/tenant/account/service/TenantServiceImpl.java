package com.kalibyte.foundry.tenant.account.service;

import com.kalibyte.foundry.infrastructure.tenancy.flyway.TenantFlywayService;
import com.kalibyte.foundry.tenant.account.entity.TenantEntity;
import com.kalibyte.foundry.tenant.account.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TenantServiceImpl implements TenantService {

    private final TenantRepository tenantRepository;
    private final SchemaManager schemaManager;
    private final TenantFlywayService tenantFlywayService;

    @Override
    @Transactional
    public TenantEntity createTenant(String name, String address, String gstNumber) {

        // 1️⃣ Generate safe tenant code
        String code = name.toLowerCase()
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_|_$", "");

        // 2️⃣ Check if tenant already exists
        if (tenantRepository.findByCode(code).isPresent()) {
            throw new IllegalStateException("Tenant already exists with code: " + code);
        }

        String schemaName = "tenant_" + code;

        log.info("Creating tenant [{}] with schema [{}]", name, schemaName);

        // 3️⃣ Create schema FIRST
        schemaManager.createSchemaIfNotExists(schemaName);

        // 4️⃣ Run Flyway migrations for tenant schema
        tenantFlywayService.migrateTenantSchema(schemaName);

        // 5️⃣ Persist tenant metadata ONLY after schema + tables exist
        TenantEntity tenant = new TenantEntity();
        tenant.setName(name);
        tenant.setCode(code);
        tenant.setAddress(address);
        tenant.setGstNumber(gstNumber);
        tenant.setSchemaName(schemaName);
        tenant.setStatus("ACTIVE");

        tenantRepository.save(tenant);

        log.info("Tenant [{}] successfully initialized with schema [{}]", name, schemaName);

        return tenant;
    }

    @Override
    public TenantEntity findById(Long tenantId) {
        return tenantRepository.findById(tenantId)
                .orElseThrow(() ->
                        new RuntimeException("Tenant not found with id: " + tenantId));
    }
}
