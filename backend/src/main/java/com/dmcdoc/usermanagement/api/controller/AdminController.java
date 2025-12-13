package com.dmcdoc.usermanagement.api.controller;

import com.dmcdoc.sharedcommon.dto.UserDto;
import com.dmcdoc.sharedcommon.dto.AdminUserUpdateRequest;
import com.dmcdoc.usermanagement.core.model.User;
import com.dmcdoc.usermanagement.core.mapper.UserMapper;
import com.dmcdoc.usermanagement.core.model.Role;
import com.dmcdoc.usermanagement.core.service.admin.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    // Stats
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> stats() {
        return ResponseEntity.ok(adminService.getStats());
    }

    // Paginated & searchable users
    @GetMapping("/users")
    public ResponseEntity<Page<UserDto>> users(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<User> p = adminService.searchUsers(
                search,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));

        return ResponseEntity.ok(p.map(UserMapper::toDto));
    }


    // Update user (partial)
    @PatchMapping("/users/{userId}")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable UUID userId,
            @RequestBody @Valid AdminUserUpdateRequest body) {

        User u = adminService.updateUser(null, userId, body.getEmail(), body.getUsername(), body.getEnabled(), null);
        return ResponseEntity.ok(UserMapper.toDto(u));
    }

    // Reset password (admin)
    @PostMapping("/users/{userId}/reset-password")
    public ResponseEntity<Map<String, String>> adminReset(@PathVariable UUID userId) {
        String temp = adminService.adminResetPassword(userId);
        // In prod, send email; here return temp for admin to copy
        return ResponseEntity.ok(Map.of("tempPassword", temp));
    }


    @GetMapping("/users/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable UUID id) {
        var user = adminService.getUserById(id);
        return ResponseEntity.ok(UserMapper.toDto(user));
    }

    @PostMapping("/users/{id}/roles/{roleName}")
    public ResponseEntity<Void> addRoleToUser(@PathVariable UUID id, @PathVariable String roleName) {
        adminService.addRoleToUser(id, roleName);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/users/{id}/roles/{roleName}")
    public ResponseEntity<Void> removeRoleFromUser(@PathVariable UUID id, @PathVariable String roleName) {
        adminService.removeRoleFromUser(id, roleName);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/roles")
    public ResponseEntity<List<Role>> getAllRoles() {
        return ResponseEntity.ok(adminService.getAllRoles());
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/users/{id}/block")
    public ResponseEntity<?> blockUser(@PathVariable UUID id) {
        adminService.blockUser(id);
        return ResponseEntity.ok("User blocked");
    }

    @PutMapping("/users/{id}/unblock")
    public ResponseEntity<?> unblockUser(@PathVariable UUID id) {
        adminService.unblockUser(id);
        return ResponseEntity.ok("User unblocked");
    }

    @PutMapping("/users/{id}/roles")
    public ResponseEntity<Void> updateRoles(
            @PathVariable UUID id,
            @RequestBody List<String> roles) {

        adminService.setUserRoles(id, roles);
        return ResponseEntity.ok().build();
    }

}
