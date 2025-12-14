package com.dmcdoc.usermanagement.core.service.admin;

import com.dmcdoc.usermanagement.api.exceptions.ResourceNotFoundException;
import com.dmcdoc.usermanagement.core.model.Role;
import com.dmcdoc.usermanagement.core.model.User;
import com.dmcdoc.usermanagement.core.repository.RoleRepository;
import com.dmcdoc.usermanagement.core.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/*
 * @ExtendWith(MockitoExtension.class)
 * public class AdminServiceUnitTest {
 * 
 * @Mock
 * private UserRepository userRepository;
 * 
 * @Mock
 * private RoleRepository roleRepository;
 * 
 * @Mock
 * private PasswordEncoder passwordEncoder;
 * 
 * private AdminService adminService;
 * 
 * @BeforeEach
 * void setUp() {
 * adminService = new AdminService(userRepository, roleRepository,
 * passwordEncoder);
 * }
 * 
 * @Test
 * void testSearchUsersWithBlankQuery() {
 * UUID id = UUID.randomUUID();
 * User user = new User();
 * user.setId(id);
 * user.setUsername("testuser");
 * user.setEmail("test@example.com");
 * 
 * Page<User> page = new PageImpl<>(List.of(user));
 * when(userRepository.findAll(any(Pageable.class))).thenReturn(page);
 * 
 * // Create a mock pageable
 * Pageable pageable = mock(Pageable.class);
 * Page<User> result = adminService.searchUsers("", pageable);
 * 
 * assertNotNull(result);
 * verify(userRepository).findAll(any(Pageable.class));
 * }
 * 
 * @Test
 * void testSearchUsersWithQuery() {
 * UUID id = UUID.randomUUID();
 * User user = new User();
 * user.setId(id);
 * user.setUsername("testuser");
 * user.setEmail("test@example.com");
 * 
 * Page<User> page = new PageImpl<>(List.of(user));
 * when(userRepository.
 * findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
 * anyString(), anyString(), any(Pageable.class)))
 * .thenReturn(page);
 * 
 * Pageable pageable = mock(Pageable.class);
 * Page<User> result = adminService.searchUsers("test", pageable);
 * 
 * assertNotNull(result);
 * verify(userRepository).
 * findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
 * eq("test"), eq("test"), any(Pageable.class));
 * }
 * 
 * @Test
 * void testGetStats() {
 * when(userRepository.count()).thenReturn(10L);
 * when(userRepository.countByRoles_Name("ROLE_ADMIN")).thenReturn(2L);
 * when(userRepository.countByEnabledFalse()).thenReturn(1L);
 * 
 * Map<String, Long> stats = adminService.getStats();
 * 
 * assertNotNull(stats);
 * assertEquals(10L, stats.get("totalUsers"));
 * assertEquals(2L, stats.get("admins"));
 * assertEquals(1L, stats.get("disabled"));
 * }
 * 
 * @Test
 * void testGetAllRoles() {
 * Role adminRole = new Role();
 * adminRole.setName("ROLE_ADMIN");
 * 
 * when(roleRepository.findAll()).thenReturn(List.of(adminRole));
 * 
 * List<Role> roles = adminService.getAllRoles();
 * 
 * assertNotNull(roles);
 * assertEquals(1, roles.size());
 * assertEquals("ROLE_ADMIN", roles.get(0).getName());
 * }
 * 
 * @Test
 * void testDeleteUser() {
 * UUID userId = UUID.randomUUID();
 * when(userRepository.existsById(userId)).thenReturn(true);
 * 
 * adminService.deleteUser(userId);
 * 
 * verify(userRepository).existsById(userId);
 * verify(userRepository).deleteById(userId);
 * }
 * 
 * @Test
 * void testDeleteUserNotFound() {
 * UUID userId = UUID.randomUUID();
 * when(userRepository.existsById(userId)).thenReturn(false);
 * 
 * assertThrows(ResourceNotFoundException.class, () ->
 * adminService.deleteUser(userId));
 * }
 * }
 */
