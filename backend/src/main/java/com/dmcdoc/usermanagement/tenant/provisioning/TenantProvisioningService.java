// Provisioning d’un tenant (AUTOMATIQUE)

package com.dmcdoc.usermanagement.tenant.provisioning;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TenantProvisioningService {

    private final DataSource dataSource;

    public void provisionTenant(UUID tenantId) {

        String schema = tenantId.toString();

        try (var conn = dataSource.getConnection()) {
            conn.createStatement()
                    .execute("CREATE SCHEMA IF NOT EXISTS \"" + schema + "\"");
        } catch (Exception e) {
            throw new IllegalStateException("Schema creation failed", e);
        }

        // Note: migrations (Flyway) intentionally omitted here to avoid a hard dependency
        // The real provisioning flow should run DB migrations for the new schema.
    }
}
