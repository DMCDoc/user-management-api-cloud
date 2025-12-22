package com.dmcdoc.usermanagement.tenant;

import java.util.UUID;

public final class TenantContext {

    private static final ThreadLocal<UUID> CURRENT = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> BYPASS = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void setTenantId(UUID tenantId) {
        CURRENT.set(tenantId);
        BYPASS.set(false);
    }

    public static void enableBypass() {
        CURRENT.remove();
        BYPASS.set(true);
    }

    public static UUID getTenantId() {
        return CURRENT.get();
    }

    public static boolean isResolved() {
        return getTenantId() != null && !isBypass();
    }

    public static boolean isBypass() {
        return Boolean.TRUE.equals(BYPASS.get());
    }

    public static void clear() {
        CURRENT.remove();
        BYPASS.remove();
    }

    public static UUID getTenantIdRequired() {
        if (isBypass()) {
            throw new IllegalStateException("Tenant bypass active");
        }
        UUID tenantId = CURRENT.get();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }
        return tenantId;
    }
}
