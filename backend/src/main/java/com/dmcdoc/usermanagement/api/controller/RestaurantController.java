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

        if (isSuperAdmin(authentication)) {
            return ResponseEntity.ok(restaurantRepository.findAll());
        }

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
     */
    @GetMapping("/{id}")
    public ResponseEntity<Restaurant> getById(@PathVariable String id, Authentication auth) {
        UUID restaurantId;
        try {
            restaurantId = UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(403).build(); // ✅ TEST 3
        }

        // 👑 Cas SuperAdmin : On cherche partout
        if (isSuperAdmin(auth)) {
            return restaurantRepository.findById(restaurantId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build()); // ✅ TEST 10 (404 attendu)
        }

        // 👤 Cas Normal : On cherche uniquement dans le tenant
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null)
            return ResponseEntity.status(403).build();

        return restaurantRepository.findByIdAndTenantId(restaurantId, tenantId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(403).build()); // ✅ TEST 14 & 15 (403 attendu)
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
