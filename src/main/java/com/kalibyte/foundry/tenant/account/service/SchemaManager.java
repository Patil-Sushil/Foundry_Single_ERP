package com.kalibyte.foundry.tenant.account.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Component
@RequiredArgsConstructor
@Slf4j
public class SchemaManager {

    private final DataSource dataSource;

    public void createSchemaIfNotExists(String schemaName) {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE SCHEMA IF NOT EXISTS " + schemaName);
            log.info("Schema ensured: {}", schemaName);

        } catch (Exception e) {
            throw new RuntimeException("Failed to create schema " + schemaName, e);
        }
    }
}
