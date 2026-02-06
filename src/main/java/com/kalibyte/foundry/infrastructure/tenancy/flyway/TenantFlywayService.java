package com.kalibyte.foundry.infrastructure.tenancy.flyway;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

@Service
@RequiredArgsConstructor
@Slf4j
public class TenantFlywayService {

    private final DataSource dataSource;

    public void migrateTenantSchema(String schemaName) {

        log.info("Migrating tenant schema: {}", schemaName);

        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .schemas(schemaName)
                .locations("classpath:db/migration/tenant")
                .baselineOnMigrate(true)
                .baselineVersion("1")
                .validateOnMigrate(true)
                .outOfOrder(false)
                .load();

        flyway.migrate();
    }
}
