package com.dmcdoc.usermanagement.api.controller;

import com.dmcdoc.sharedcommon.dto.UserDto;
import com.dmcdoc.usermanagement.core.mapper.UserMapper;
import com.dmcdoc.usermanagement.core.model.Role;
import com.dmcdoc.usermanagement.core.service.admin.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        var users = adminService.getAllUsers().stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
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
}
