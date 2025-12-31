package com.dmcdoc.usermanagement.config.security;

import com.dmcdoc.usermanagement.core.repository.UserRepository;
import com.dmcdoc.usermanagement.tenant.TenantContext;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Récupérer le tenant de manière optionnelle
        UUID tenantId = TenantContext.getTenantId();
        System.out.println("RECHERCHE USER: " + username + " DANS TENANT: " + tenantId);

        // Si on a un tenant (cas normal), on cherche par username ET tenant
        if (tenantId != null) {
            return userRepository
                    .findByUsernameAndTenantId(username, tenantId)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found in tenant: " + username));
        }

        // Cas Super Admin : on cherche uniquement par username (en base centrale)
        return userRepository
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Global user not found: " + username));
    }
}

