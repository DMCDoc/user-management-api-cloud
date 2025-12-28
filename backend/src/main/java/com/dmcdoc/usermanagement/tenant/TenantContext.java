package com.dmcdoc.usermanagement.tenant;

import org.springframework.security.access.AccessDeniedException;

import java.util.UUID;

public final class TenantContext {

    private static final ThreadLocal<UUID> CURRENT_TENANT = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> BYPASS = ThreadLocal.withInitial(() -> false);

    private TenantContext() {
    }

    /*
     * =========================
     * TENANT
     * =========================
     */

    public static void setTenantId(UUID tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    public static UUID getTenantId() {
        return CURRENT_TENANT.get();
    }

    public static UUID getTenantIdRequired() {
        UUID tenantId = CURRENT_TENANT.get();
        if (tenantId == null) {
            throw new AccessDeniedException("Tenant not resolved");
        }
        return tenantId;
    }

    public static boolean isResolved() {
        return CURRENT_TENANT.get() != null;
    }

    /*
     * =========================
     * BYPASS (SUPER ADMIN / BOOTSTRAP)
     * =========================
     */

    public static void enableBypass() {
        BYPASS.set(true);
    }

    public static void disableBypass() {
        BYPASS.set(false);
    }

    public static boolean isBypassEnabled() {
        return Boolean.TRUE.equals(BYPASS.get());
    }

    /*
     * =========================
     * CLEANUP
     * =========================
     */

    public static void clear() {
        CURRENT_TENANT.remove();
        BYPASS.remove();
    }
}
