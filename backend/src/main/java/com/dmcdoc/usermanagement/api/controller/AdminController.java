package com.dmcdoc.usermanagement.api.controller;

import com.dmcdoc.sharedcommon.dto.AdminUserResponse;
import com.dmcdoc.usermanagement.core.service.admin.AdminService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public ResponseEntity<Page<AdminUserResponse>> searchUsers(
            @RequestParam(required = false) String query,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(
                adminService.searchUsers(query, pageable));
    }

    @PostMapping("/{userId}/block")
    public ResponseEntity<Void> blockUser(@PathVariable java.util.UUID userId) {
        adminService.blockUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userId}/unblock")
    public ResponseEntity<Void> unblockUser(@PathVariable java.util.UUID userId) {
        adminService.unblockUser(userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable java.util.UUID userId) {
        adminService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countUsers() {
        return ResponseEntity.ok(adminService.countUsersForTenant());
    }
}
