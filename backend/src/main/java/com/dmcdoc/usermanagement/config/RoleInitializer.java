package com.dmcdoc.usermanagement.config;

import com.dmcdoc.usermanagement.core.model.Role;
import com.dmcdoc.usermanagement.core.repository.RoleRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component @RequiredArgsConstructor
public class RoleInitializer {

    private final RoleRepository roleRepository;

    @PostConstruct
    public void initRoles() {
        List<String> defaultRoles = List.of("ROLE_USER", "ROLE_ADMIN");

        for (String roleName : defaultRoles) {
            roleRepository.findByName(roleName).orElseGet(() -> {
                Role role = Role.builder().id(UUID.randomUUID()).name(roleName).build();
                System.out.println(">>> Initialisation des rôles en DB");
                return roleRepository.save(role);
            });
        }
    }
}
