package com.kalibyte.foundry.config;

import com.kalibyte.foundry.common.util.ContextUtil;
import com.zaxxer.hikari.HikariDataSource;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
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

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.hikari")
    public DataSource dataSource(DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            EntityManagerFactoryBuilder builder,
            DataSource dataSource,
            JpaProperties jpaProperties,
            MultiTenantConnectionProvider multiTenantConnectionProvider,
            CurrentTenantIdentifierResolver currentTenantIdentifierResolver) {

        Map<String, Object> props = new HashMap<>(jpaProperties.getProperties());
        props.put(org.hibernate.cfg.Environment.MULTI_TENANT_CONNECTION_PROVIDER, multiTenantConnectionProvider);
        props.put(org.hibernate.cfg.Environment.MULTI_TENANT_IDENTIFIER_RESOLVER, currentTenantIdentifierResolver);
        // Hibernate 6 requires setting MultiTenancyStrategy implicitly via provider/resolver or explicitly
        props.put("hibernate.multiTenancy", "SCHEMA");

        return builder
                .dataSource(dataSource)
                .packages("com.kalibyte.foundry")
                .properties(props)
                .build();
    }

    @Bean
    public CurrentTenantIdentifierResolver currentTenantIdentifierResolver() {
        return new CurrentTenantIdentifierResolver() {
            @Override
            public String resolveCurrentTenantIdentifier() {
                String tenant = ContextUtil.getTenant();
                return tenant != null ? tenant : "public";
            }

            @Override
            public boolean validateExistingCurrentSessions() {
                return true;
            }
        };
    }

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
                try {
                    String schema = (tenantIdentifier != null) ? tenantIdentifier.toString() : "public";
                    try (Statement statement = connection.createStatement()) {
                        statement.execute("SET search_path TO " + schema);
                    }
                } catch (SQLException e) {
                    throw new SQLException("Could not set search_path to " + tenantIdentifier, e);
                }
                return connection;
            }

            @Override
            public void releaseConnection(Object tenantIdentifier, Connection connection) throws SQLException {
                try (Statement statement = connection.createStatement()) {
                    statement.execute("SET search_path TO public");
                } catch (SQLException e) {
                    // Log error
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
}