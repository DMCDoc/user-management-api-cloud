package com.example.usermanagement.security;

import com.example.usermanagement.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtils jwtUtils;
    private final UserRepository userRepository;

    public JWTFilter(JWTUtils jwtUtils, UserRepository userRepository) {
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/users/login") || path.equals("/users/register") 
                || path.equals("/actuator/health") || path.equals("/actuator/info");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        final String header = request.getHeader("Authorization");
        final String prefix = "Bearer ";

        // Ignorer le filtre pour /actuator/health
        if (request.getRequestURI().equals("/actuator/health")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (header == null || !header.startsWith(prefix)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = header.substring(prefix.length());

            if (jwtUtils.isTokenValid(token)) {
                String username = jwtUtils.extractUsername(token);
                userRepository.findByUsername(username).ifPresent(user -> {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            user.getUsername(),
                            null,
                            user.getAuthorities() // Utilisez les vraies autorisations si disponibles
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                });
            }

            filterChain.doFilter(request, response);

        } catch (JwtException | IllegalArgumentException ex) {
            // Nettoyer le contexte de sécurité
            SecurityContextHolder.clearContext();

            // Envoyer une réponse d'erreur claire
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid JWT token: " + ex.getMessage());
        }
        System.out.println("🔍 Requête interceptée : " + request.getMethod() + " " + request.getRequestURI());
    }
}