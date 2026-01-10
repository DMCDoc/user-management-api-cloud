package com.dmcdoc.usermanagement.api.controller;

import com.dmcdoc.sharedcommon.dto.AdminUserResponse;
import com.dmcdoc.usermanagement.core.service.admin.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping
    public Page<AdminUserResponse> searchUsers(
            @RequestParam(required = false, defaultValue = "") String q,
            Pageable pageable) {
        return adminService.searchUsers(q, pageable);
    }

    @PostMapping("/{id}/block")
    public void blockUser(@PathVariable UUID id) {
        adminService.blockUser(id);
    }

    @PostMapping("/{id}/unblock")
    public void unblockUser(@PathVariable UUID id) {
        adminService.unblockUser(id);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable UUID id) {
        adminService.deleteUser(id);
    }

    @GetMapping("/count")
    public long countUsers() {
        return adminService.countUsersForTenant();
    }
}
