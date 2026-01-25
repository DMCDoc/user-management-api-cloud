package com.dmcdoc.usermanagement;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.dmcdoc.usermanagement.config.jpa.HibernateTenantFilterConfig;
import com.dmcdoc.usermanagement.security.TestSecurityConfig;

import jakarta.transaction.Transactional;

@SpringBootTest
@Import({TestSecurityConfig.class, HibernateTenantFilterConfig.class})
@ActiveProfiles("test")
@Transactional

class ContextLoadsTest {

    @Test
    void contextLoads() {
    }
}
