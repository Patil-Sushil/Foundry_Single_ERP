package com.kalibyte.foundry.tenant.account.service;

import com.kalibyte.foundry.common.exception.BusinessException;
import com.kalibyte.foundry.common.util.ContextUtil;
import com.kalibyte.foundry.tenant.account.entity.TenantEntity;
import com.kalibyte.foundry.tenant.account.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TenantService {

    private final TenantRepository tenantRepository;
    private final SchemaManager schemaManager;

    @Transactional
    public TenantEntity createTenant(String name, String address, String gstNumber) {
        // 1. Generate code and schema name
        String code = name.toLowerCase().replaceAll("\\s+", "_") + "_" + System.currentTimeMillis();
        String schemaName = "tenant_" + code;

        // 2. Create Tenant Record
        TenantEntity tenant = new TenantEntity();
        tenant.setName(name);
        tenant.setCode(code);
        tenant.setAddress(address);
        tenant.setGstNumber(gstNumber);
        tenant.setSchemaName(schemaName);
        tenant.setStatus("ACTIVE");
        tenant = tenantRepository.save(tenant);

        // 3. Create Schema & Run Migrations
        schemaManager.createSchema(schemaName);
        schemaManager.runMigrations(schemaName);

        return tenant;
    }

    public TenantEntity findByCode(String code) {
        return tenantRepository.findByCode(code)
                .orElseThrow(() -> new BusinessException("Tenant not found with code: " + code));
    }
}