package com.dmcdoc.usermanagement.config.security;

import com.dmcdoc.usermanagement.core.repository.RestaurantRepository;
import com.dmcdoc.usermanagement.core.repository.RoleRepository;
import com.dmcdoc.usermanagement.tenant.SystemTenant;
import com.dmcdoc.usermanagement.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("tenantSecurity")
@RequiredArgsConstructor
public class TenantSecurity {

    private final RoleRepository roleRepository;
    private final RestaurantRepository restaurantRepository;

    /**
     * Point d’entrée unique appelé depuis @PreAuthorize.
     */
    public boolean check(Authentication authentication, UUID resourceId) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return false;
        }

        return belongsToTenant(resourceId, tenantId);
    }

    /**
     * Sécurité multi-tenant centralisée.
     * Gère explicitement chaque type de ressource autorisée.
     */
    protected boolean belongsToTenant(UUID resourceId, UUID tenantId) {

        /*
         * =========================
         * ROLE
         * =========================
         */

        if (SystemTenant.SYSTEM_TENANT.equals(tenantId)) {
            if (roleRepository.existsById(resourceId)) {
                return true;
            }
        } else {
            if (roleRepository.existsByIdAndTenantId(resourceId, tenantId)) {
                return true;
            }
        }

        /*
         * =========================
         * RESTAURANT
         * =========================
         */

        return restaurantRepository.existsByIdAndTenantId(resourceId, tenantId);
    }
}
/*
 * ✔ Centralisation de la règle multi-tenant
 * 
 * ✔ Fail-safe (refus par défaut)
 * 
 * ✔ Compatible SpEL
 * 
 * ✔ Indépendant du web / controller
 * 
 * ✔ Prêt pour sécurité fine par ressource
 * 
 * ✔ Aucune dette technique
 */
