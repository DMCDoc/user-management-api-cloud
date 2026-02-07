package com.dmcdoc.usermanagement.support;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@TestConfiguration
public class TestRequestLoggingConfig {

    @Bean
    public OncePerRequestFilter testLoggingFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
                System.out.println("--- TEST LOG FILTER BEFORE: " + request.getMethod() + " " + request.getRequestURI());
                filterChain.doFilter(request, response);
                System.out.println("--- TEST LOG FILTER AFTER STATUS: " + response.getStatus());
            }
        };
    }
}
