package com.dmcdoc.usermanagement.support;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@TestConfiguration
@EntityScan("com.dmcdoc.usermanagement")
@EnableJpaRepositories("com.dmcdoc.usermanagement")
public class JpaTestConfig {
}
