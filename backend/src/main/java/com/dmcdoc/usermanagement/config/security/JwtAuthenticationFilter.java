package com.dmcdoc.usermanagement.config.security;

import com.dmcdoc.usermanagement.tenant.TenantContext;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // Log systématique pour voir si le filtre est déclenché
        System.out.println(">>> FILTRE JWT APPELÉ : " + request.getMethod() + " " + request.getRequestURI());

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);

        try {
            // 1. Extraction des Claims
            Claims claims;
            try {
                claims = jwtService.extractAllClaims(jwt);
            } catch (ExpiredJwtException e) {
                System.out.println("--- TOKEN EXPIRED ---");
                System.out.println("Exp: " + e.getClaims().getExpiration() + " | Now: " + new Date());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            String username = claims.getSubject();

            // 2. Extraction du TenantId avec conversion sécurisée
            Object tenantClaim = claims.get("tenantId");
            UUID tenantId = null;
            if (tenantClaim != null) {
                tenantId = UUID.fromString(tenantClaim.toString());
            }

            System.out.println("--- DEBUG JWT VALIDATION ---");
            System.out.println("Username: " + username);
            System.out.println("TenantID extrait du JWT: " + tenantId);
            System.out.println("Exp: " + claims.getExpiration());

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // 3. INJECTION DANS LE CONTEXTE (Crucial pour Hibernate et
                // CustomUserDetailsService)
                TenantContext.setTenantId(tenantId);

                try {
                    // 4. Chargement de l'utilisateur
                    // Le service va maintenant trouver le tenantId dans TenantContext
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    if (jwtService.isValid(jwt)) {
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        System.out.println("AUTH SUCCESS : " + username);
                    }
                } catch (Exception e) {
                    System.out.println("AUTH FAILURE : " + e.getMessage());
                    // On ne bloque pas forcément ici, on laisse Spring Security décider selon la
                    // config
                }
            }
        } catch (Exception ex) {
            System.out.println("DEBUG JWT GLOBAL ERROR: " + ex.getMessage());
            ex.printStackTrace();
        }

        filterChain.doFilter(request, response);
    }
}