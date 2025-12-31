package com.dmcdoc.usermanagement.api.controller;

import com.dmcdoc.usermanagement.core.model.Restaurant;
import com.dmcdoc.usermanagement.core.repository.RestaurantRepository;
import com.dmcdoc.usermanagement.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantRepository restaurantRepository;

    /*
     * ==========================================================
     * LIST
     * ==========================================================
     */
    @GetMapping
    public ResponseEntity<List<Restaurant>> list(Authentication authentication) {

        // 👑 Super Admin → tous les restaurants
        if (isSuperAdmin(authentication)) {
            return ResponseEntity.ok(restaurantRepository.findAll());
        }

        // 👤 Utilisateur normal → uniquement son tenant
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(
                restaurantRepository.findAllByTenantId(tenantId));
    }

    /*
     * ==========================================================
     * GET BY ID
     * ==========================================================
     *
     * ⚠️ IMPORTANT
     * - UUID typé → Spring gère la conversion
     * - UUID invalide → MethodArgumentTypeMismatchException
     * - → GlobalExceptionHandler → 403
     */
    @GetMapping("/{id}")
    public ResponseEntity<Restaurant> getById(
            @PathVariable("id") UUID id,
            Authentication authentication) {

        // 👑 Super Admin → accès cross-tenant
        if (isSuperAdmin(authentication)) {
            return restaurantRepository.findById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        }

        // 👤 Utilisateur normal → tenant strict
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.status(403).build();
        }

        return restaurantRepository.findByIdAndTenantId(id, tenantId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(403).build());
    }

    /*
     * ==========================================================
     * UTIL
     * ==========================================================
     */
    private boolean isSuperAdmin(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_SUPER_ADMIN"::equals);
    }
}
