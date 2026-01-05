package com.dmcdoc.usermanagement.api.controller;

import com.dmcdoc.sharedcommon.dto.UserResponse;
import com.dmcdoc.usermanagement.core.service.admin.AdminService;
import com.dmcdoc.usermanagement.tenant.TenantContext;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN')")
public class AdminController {

    private final AdminService adminService;

    /**
     * Liste paginée des utilisateurs du tenant
     */
    @GetMapping
    public ResponseEntity<Page<UserResponse>> listUsers(
            @RequestParam(required = false) String search,
            Pageable pageable) {

        UUID tenantId = TenantContext.getTenantId();

        Page<UserResponse> users = adminService.listUsers(
                tenantId,
                search,
                pageable);

        return ResponseEntity.ok(users);
    }

    /**
     * Désactiver un utilisateur
     */
    @PatchMapping("/{userId}/disable")
    public ResponseEntity<Void> disableUser(@PathVariable UUID userId) {

        adminService.disableUser(
                userId,
                TenantContext.getTenantId());

        return ResponseEntity.noContent().build();
    }

    /**
     * Réactiver un utilisateur
     */
    @PatchMapping("/{userId}/enable")
    public ResponseEntity<Void> enableUser(@PathVariable UUID userId) {

        adminService.enableUser(
                userId,
                TenantContext.getTenantId());

        return ResponseEntity.noContent().build();
    }

    /**
     * Suppression d’un utilisateur
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {

        adminService.deleteUser(
                userId,
                TenantContext.getTenantId());

        return ResponseEntity.noContent().build();
    }
}
