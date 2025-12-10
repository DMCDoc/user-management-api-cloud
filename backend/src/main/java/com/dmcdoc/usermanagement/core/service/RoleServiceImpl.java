package com.dmcdoc.usermanagement.core.service;

import com.dmcdoc.usermanagement.core.model.Role;
import com.dmcdoc.usermanagement.core.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    public Role getOrCreate(String roleName) {

        return roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setId(UUID.randomUUID());
                    r.setName(roleName);
                    return roleRepository.save(r);
                });
    }
}
