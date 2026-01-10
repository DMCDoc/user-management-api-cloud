package com.dmcdoc.usermanagement.api.controller;

import com.dmcdoc.sharedcommon.dto.AdminUserUpdateRequest;
import com.dmcdoc.sharedcommon.dto.UserResponse;
import com.dmcdoc.usermanagement.core.mapper.UserMapper;
import com.dmcdoc.usermanagement.core.service.admin.AdminService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TENANT_ADMIN')")
public class AdminController {

    private final AdminService adminService;

    /*
     * ==========================================================
     * USERS
     * ==========================================================
     */

    @GetMapping("/users")
    public Page<UserResponse> listUsers(
            @RequestParam(required = false) String search,
            Pageable pageable) {

        return adminService
                .searchUsers(search, pageable)
                .map(UserMapper::toResponse);
    }

    @PutMapping("/users/{id}")
    public UserResponse updateUser(
            @PathVariable UUID id,
            @RequestBody AdminUserUpdateRequest body) {

        return UserMapper.toResponse(
                adminService.updateUser(id, body));
    }

    @PostMapping("/users/{id}/disable")
    public ResponseEntity<Void> disableUser(@PathVariable UUID id) {
        adminService.blockUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/users/{id}/enable")
    public ResponseEntity<Void> enableUser(@PathVariable UUID id) {
        adminService.unblockUser(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/users/{id}/reset-password")
    public Map<String, String> resetPassword(@PathVariable UUID id) {
        String tempPassword = adminService.resetPassword(id);
        return Map.of("temporaryPassword", tempPassword);
    }

    /*
     * ==========================================================
     * ROLES
     * ==========================================================
     */

    @PostMapping("/users/{id}/roles/{role}")
    public ResponseEntity<Void> addRole(
            @PathVariable UUID id,
            @PathVariable String role) {

        adminService.addRoleToUser(id, role);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/users/{id}/roles/{role}")
    public ResponseEntity<Void> removeRole(
            @PathVariable UUID id,
            @PathVariable String role) {

        adminService.removeRoleFromUser(id, role);
        return ResponseEntity.noContent().build();
    }

    /*
     * ==========================================================
     * STATS
     * ==========================================================
     */

    @GetMapping("/stats")
    public Map<String, Long> stats() {
        return adminService.getStats();
    }
}
