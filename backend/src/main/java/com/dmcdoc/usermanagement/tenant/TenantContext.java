package com.dmcdoc.usermanagement.tenant;

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
        if (tenantId == null && !isBypassEnabled()) {
            // Renvoie un 403 propre que MockMvc pourra intercepter
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "Access Denied");
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
