package com.kalibyte.foundry.tenant.account.service;

import com.kalibyte.foundry.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchemaManager {

    private final DataSource dataSource;

    public void createSchema(String schemaName) {
        log.info("Creating schema: {}", schemaName);
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE SCHEMA IF NOT EXISTS " + schemaName);
        } catch (SQLException e) {
            throw new BusinessException("Failed to create schema: " + schemaName);
        }
    }

    public void runMigrations(String schemaName) {
        log.info("Running migrations for schema: {}", schemaName);
        Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration/tenant")
                .schemas(schemaName)
                .baselineOnMigrate(true)
                .load()
                .migrate();
    }
}
