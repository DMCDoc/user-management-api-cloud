package com.dmcdoc.usermanagement;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import com.dmcdoc.usermanagement.tenant.TenantProperties;

@EnableConfigurationProperties(TenantProperties.class)
@SpringBootApplication
@EnableScheduling
public class UserManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserManagementApplication.class, args);
    }
}
