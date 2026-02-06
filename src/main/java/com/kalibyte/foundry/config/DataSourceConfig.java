package com.kalibyte.foundry.config;

import com.kalibyte.foundry.common.util.ContextUtil;
import com.kalibyte.foundry.infrastructure.tenancy.hibernate.TenantIdentifierResolver;
import com.zaxxer.hikari.HikariDataSource;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DataSourceConfig {

    /* -----------------------------
     * PRIMARY DATASOURCE
     * ----------------------------- */
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.hikari")
    public DataSource dataSource(DataSourceProperties properties) {
        return properties
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    /* -----------------------------
     * MULTI-TENANT CONNECTION PROVIDER
     * ----------------------------- */
    @Bean
    public MultiTenantConnectionProvider multiTenantConnectionProvider(DataSource dataSource) {
        return new MultiTenantConnectionProvider() {

            @Override
            public Connection getAnyConnection() throws SQLException {
                return dataSource.getConnection();
            }

            @Override
            public void releaseAnyConnection(Connection connection) throws SQLException {
                connection.close();
            }

            @Override
            public Connection getConnection(Object tenantIdentifier) throws SQLException {
                Connection connection = getAnyConnection();
                String schema = tenantIdentifier != null
                        ? tenantIdentifier.toString()
                        : "public";

                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("SET search_path TO " + schema);
                }
                return connection;
            }

            @Override
            public void releaseConnection(Object tenantIdentifier, Connection connection)
                    throws SQLException {
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("SET search_path TO public");
                }
                connection.close();
            }

            @Override
            public boolean supportsAggressiveRelease() {
                return false;
            }

            @Override
            public boolean isUnwrappableAs(Class unwrapType) {
                return false;
            }

            @Override
            public <T> T unwrap(Class<T> unwrapType) {
                return null;
            }
        };
    }

    /* -----------------------------
     * ENTITY MANAGER FACTORY (ONLY ONE!)
     * ----------------------------- */
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            DataSource dataSource,
            JpaProperties jpaProperties,
            MultiTenantConnectionProvider multiTenantConnectionProvider,
            TenantIdentifierResolver tenantIdentifierResolver
    ) {

        Map<String, Object> props = new HashMap<>(jpaProperties.getProperties());

        props.put("hibernate.multiTenancy", "SCHEMA");
        props.put(
                org.hibernate.cfg.AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER,
                multiTenantConnectionProvider
        );
        props.put(
                org.hibernate.cfg.AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER,
                tenantIdentifierResolver
        );

        LocalContainerEntityManagerFactoryBean emf =
                new LocalContainerEntityManagerFactoryBean();

        emf.setDataSource(dataSource);
        emf.setPackagesToScan("com.kalibyte.foundry");
        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        emf.setJpaPropertyMap(props);

        return emf;
    }
}
