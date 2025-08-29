package com.example.usermanagement.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration @ConfigurationProperties(prefix = "security.jwt")
public class JwtExclusionProperties {
    private List<String> excludePaths;

    public List<String> getExcludePaths() {
        return excludePaths;
    }

    public void setExcludePaths(List<String> excludePaths) {
        this.excludePaths = excludePaths;
    }
}
