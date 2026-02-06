package com.kalibyte.foundry.infrastructure.tenancy.flyway;

import com.kalibyte.foundry.tenant.account.repository.TenantRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TenantSchemaStartupMigrator {

    private final TenantRepository tenantRepository;
    private final TenantFlywayService tenantFlywayService;

    @PostConstruct
    public void migrateAllTenantSchemas() {

        log.info(" Starting tenant schema migrations on startup...");

        tenantRepository.findAll().forEach(tenant -> {
            log.info("➡ Migrating schema: {}", tenant.getSchemaName());
            tenantFlywayService.migrateTenantSchema(tenant.getSchemaName());
        });

        log.info(" Tenant schema migrations completed");
    }
}
