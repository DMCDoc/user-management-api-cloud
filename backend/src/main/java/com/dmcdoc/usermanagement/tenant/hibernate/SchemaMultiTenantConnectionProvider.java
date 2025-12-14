package com.dmcdoc.usermanagement.tenant.hibernate;

import lombok.RequiredArgsConstructor;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
@RequiredArgsConstructor
public class SchemaMultiTenantConnectionProvider
    implements MultiTenantConnectionProvider<String> {

    private final DataSource dataSource;

    @Override
    public Connection getAnyConnection() throws SQLException {
        return DataSourceUtils.getConnection(dataSource);
    }

    @Override
            public Connection getConnection(String tenantIdentifier)
                throws SQLException {

            String tenant = tenantIdentifier == null ? "public" : tenantIdentifier;
            Connection connection = getAnyConnection();
            connection.createStatement()
                .execute("SET search_path TO \"" + tenant + "\"");
            return connection;
            }

            @Override
            public void releaseConnection(
                String tenantIdentifier,
                Connection connection) throws SQLException {

            connection.createStatement()
                .execute("SET search_path TO public");
            connection.close();
            }

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }
    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public boolean isUnwrappableAs(Class<?> unwrapType) {
        return unwrapType.isInstance(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T unwrap(Class<T> unwrapType) {
        if (isUnwrappableAs(unwrapType)) {
            return (T) this;
        }
        return null;
    }
}
