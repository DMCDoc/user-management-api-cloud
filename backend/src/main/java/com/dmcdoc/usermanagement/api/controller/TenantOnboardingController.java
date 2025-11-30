package com.dmcdoc.usermanagement.api.controller;

import com.dmcdoc.usermanagement.config.security.JwtService;
import com.dmcdoc.usermanagement.core.model.Restaurant;
import com.dmcdoc.usermanagement.core.model.Tenant;
import com.dmcdoc.usermanagement.core.model.User;
import com.dmcdoc.usermanagement.core.service.RestaurantService;
import com.dmcdoc.usermanagement.core.service.TenantService;
import com.dmcdoc.usermanagement.core.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class TenantOnboardingController {

    private final TenantService tenantService;
    private final RestaurantService restaurantService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;

    @PostMapping("/register-restaurant")
    public ResponseEntity<RegisterRestaurantResponse> registerRestaurant(
            @Valid @RequestBody RegisterRestaurantRequest request) {

        // tenant
        UUID tenantId = UUID.randomUUID();
        Tenant tenant = tenantService.createTenant(tenantId, request.getRestaurantName(), request.getTenantKey(),
                request.getMetadata());

        // restaurant
        UUID restaurantId = UUID.randomUUID();
        Restaurant restaurant = restaurantService.createRestaurant(restaurantId, tenantId, request.getRestaurantName(),
                request.getAddress(), request.getMetadata());

        // admin user
        UUID adminId = UUID.randomUUID();
        User admin = new User();
        admin.setId(adminId);
        admin.setEmail(request.getAdminEmail().toLowerCase().trim());
        admin.setUsername(request.getAdminEmail().toLowerCase().trim());
        admin.setPassword(passwordEncoder.encode(request.getAdminPassword()));
        admin.setTenantId(tenantId);
        admin.setEnabled(true);
        // assign role, depends on your Role entity flow: here I assume you have helper
        // in UserService
        userService.assignRolesAndSave(admin, List.of("ROLE_RESTAURANT_ADMIN"));

        // issue token
        var userDetails = userDetailsService.loadUserByUsername(admin.getUsername());
        String jwt = jwtService.generateToken(userDetails, tenantId.toString());

        RegisterRestaurantResponse resp = new RegisterRestaurantResponse();
        resp.setTenantId(tenantId.toString());
        resp.setRestaurantId(restaurantId.toString());
        resp.setAdminEmail(admin.getEmail());
        resp.setAdminToken(jwt);

        return ResponseEntity.created(URI.create("/api/tenants/" + tenantId)).body(resp);
    }

    // DTOs (getters/setters omitted for brevity - include them in your file)
    public static class RegisterRestaurantRequest {
        private String tenantKey; // unique short key (subdomain or identifier)
        private String restaurantName;
        private String address;
        private String metadata;
        private String adminEmail;
        private String adminPassword;
        private String adminFirstName;
        private String adminLastName;

        // getters/setters...
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

        public String getTenantId() {
            return tenantId;
        }

        public void setTenantId(String tenantId) {
            this.tenantId = tenantId;
        }

        public String getRestaurantId() {
            return restaurantId;
        }

        public void setRestaurantId(String restaurantId) {
            this.restaurantId = restaurantId;
        }

        public String getAdminEmail() {
            return adminEmail;
        }

        public void setAdminEmail(String adminEmail) {
            this.adminEmail = adminEmail;
        }

        public String getAdminToken() {
            return adminToken;
        }

        public void setAdminToken(String adminToken) {
            this.adminToken = adminToken;
        }
    }
}
