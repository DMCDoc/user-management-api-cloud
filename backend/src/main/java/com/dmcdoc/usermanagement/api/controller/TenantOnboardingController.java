package com.dmcdoc.usermanagement.api.controller;

import com.dmcdoc.usermanagement.config.security.JwtService;
import com.dmcdoc.usermanagement.core.model.User;
import com.dmcdoc.usermanagement.core.model.Tenant;
import com.dmcdoc.usermanagement.core.model.Restaurant;
import com.dmcdoc.usermanagement.core.service.TenantService;
import com.dmcdoc.usermanagement.core.service.RestaurantService;
import com.dmcdoc.usermanagement.core.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/onboarding")
@RequiredArgsConstructor
public class TenantOnboardingController {

    private final TenantService tenantService;
    private final RestaurantService restaurantService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * Creates a tenant → restaurant → admin user hierarchy.
     */
    @PostMapping("/register-restaurant")
    public ResponseEntity<RegisterRestaurantResponse> registerRestaurant(
            @Valid @RequestBody RegisterRestaurantRequest request) {

        // 1. Tenant
        Tenant tenant = tenantService.createTenant(
                request.getTenantKey(),
                request.getRestaurantName(),
                request.getMetadata());

        // 2. Restaurant
        Restaurant restaurant = restaurantService.createRestaurant(
                tenant.getId(),
                request.getRestaurantName(),
                request.getAddress(),
                request.getMetadata());

        // 3. Admin User
        User admin = userService.createAdminForTenant(
                tenant.getId(),
                request.getAdminEmail(),
                passwordEncoder.encode(request.getAdminPassword()),
                request.getAdminFirstName(),
                request.getAdminLastName());

        // 4. JWT (tenant-aware)
        String jwt = jwtService.generateToken(admin, tenant.getId().toString());

        // 5. Response
        RegisterRestaurantResponse resp = new RegisterRestaurantResponse(
                tenant.getId().toString(),
                restaurant.getId().toString(),
                admin.getEmail(),
                jwt);

        return ResponseEntity
                .created(URI.create("/api/tenants/" + tenant.getId()))
                .body(resp);
    }

    // -------------------------------------------------------------------------
    // DTOs
    // -------------------------------------------------------------------------

    public static class RegisterRestaurantRequest {

        private String tenantKey; // unique identifier / subdomain
        private String restaurantName;
        private String address;
        private String metadata;

        private String adminEmail;
        private String adminPassword;
        private String adminFirstName;
        private String adminLastName;

        // getters + setters
        public String getTenantKey() {
            return tenantKey;
        }

        public void setTenantKey(String tenantKey) {
            this.tenantKey = tenantKey;
        }

        public String getRestaurantName() {
            return restaurantName;
        }

        public void setRestaurantName(String restaurantName) {
            this.restaurantName = restaurantName;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getMetadata() {
            return metadata;
        }

        public void setMetadata(String metadata) {
            this.metadata = metadata;
        }

        public String getAdminEmail() {
            return adminEmail;
        }

        public void setAdminEmail(String adminEmail) {
            this.adminEmail = adminEmail;
        }

        public String getAdminPassword() {
            return adminPassword;
        }

        public void setAdminPassword(String adminPassword) {
            this.adminPassword = adminPassword;
        }

        public String getAdminFirstName() {
            return adminFirstName;
        }

        public void setAdminFirstName(String adminFirstName) {
            this.adminFirstName = adminFirstName;
        }

        public String getAdminLastName() {
            return adminLastName;
        }

        public void setAdminLastName(String adminLastName) {
            this.adminLastName = adminLastName;
        }
    }

    public static class RegisterRestaurantResponse {
        private String tenantId;
        private String restaurantId;
        private String adminEmail;
        private String adminToken;

        public RegisterRestaurantResponse(
                String tenantId,
                String restaurantId,
                String adminEmail,
                String adminToken) {
            this.tenantId = tenantId;
            this.restaurantId = restaurantId;
            this.adminEmail = adminEmail;
            this.adminToken = adminToken;
        }

        public String getTenantId() {
            return tenantId;
        }

        public String getRestaurantId() {
            return restaurantId;
        }

        public String getAdminEmail() {
            return adminEmail;
        }

        public String getAdminToken() {
            return adminToken;
        }
    }
}
