package com.dmcdoc.usermanagement.config.security;

import java.security.Principal;
import java.util.UUID;

public class JwtPrincipal implements Principal {

        private final String username;
        private final UUID tenantId;
        private final boolean superAdmin;

        public JwtPrincipal(String username, UUID tenantId, boolean superAdmin) {
                this.username = username;
                this.tenantId = tenantId;
                this.superAdmin = superAdmin;
        }

        @Override
        public String getName() {
                return username;
        }

        public String getUsername() {
                return username;
        }

        public UUID getTenantId() {
                return tenantId;
        }

        public boolean isSuperAdmin() {
                return superAdmin;
        }
}
