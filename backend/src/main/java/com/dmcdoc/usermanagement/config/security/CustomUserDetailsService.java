package com.dmcdoc.usermanagement.config.security;

import com.dmcdoc.usermanagement.core.repository.UserRepository;
import com.dmcdoc.usermanagement.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

        private final UserRepository userRepository;

        @Override
        public UserDetails loadUserByUsername(String username)
                        throws UsernameNotFoundException {

                UUID tenantId = TenantContext.getTenantId();

                if (tenantId != null) {
                        return userRepository
                                        .findByUsernameAndTenantId(username, tenantId)
                                        .orElseThrow(() -> new UsernameNotFoundException("User not found in tenant"));
                }

                return userRepository
                                .findByUsername(username)
                                .orElseThrow(() -> new UsernameNotFoundException("Global user not found"));
        }
}
