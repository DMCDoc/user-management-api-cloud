package com.dmcdoc.usermanagement.tenant;

import java.util.UUID;

/**
 * Tenant système unique.
 * Utilisé uniquement pour les rôles et données globales (super-admin).
 */
public final class SystemTenant {

    private SystemTenant() {
    }

    public static final UUID SYSTEM_TENANT = 
    UUID.fromString("00000000-0000-0000-0000-000000000000");
}
