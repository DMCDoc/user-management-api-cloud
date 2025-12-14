package com.dmcdoc.usermanagement.config;

import com.dmcdoc.usermanagement.tenant.TenantFilterInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final TenantFilterInterceptor tenantFilterInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // ⚠️ DEV ONLY — à restreindre en prod
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tenantFilterInterceptor)
                .addPathPatterns("/api/**");
    }
}

/*
 * ✔️ Zéro conflit
 * ✔️ Ordre clair :
 * 
 * CORS → Web
 * 
 * Interceptor → Sécurité ORM
 * ✔️ Compatible :
 * 
 * JWT
 * 
 * OAuth2
 * 
 * Hibernate Filter
 * 
 * Future DB schema / DB par tenant
 */