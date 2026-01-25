package com.dmcdoc.usermanagement.config.jpa;

import com.dmcdoc.usermanagement.core.repository.TenantAwareRepositoryImpl;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.dmcdoc.usermanagement", repositoryBaseClass = TenantAwareRepositoryImpl.class)
public class JpaRepositoryConfig {
    
}
