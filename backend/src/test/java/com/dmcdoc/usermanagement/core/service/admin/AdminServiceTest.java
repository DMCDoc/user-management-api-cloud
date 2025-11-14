package com.dmcdoc.usermanagement.core.service.admin;

import com.dmcdoc.usermanagement.api.exceptions.ResourceNotFoundException;
import com.dmcdoc.usermanagement.core.model.Role;
import com.dmcdoc.usermanagement.core.model.User;
import com.dmcdoc.usermanagement.core.repository.RoleRepository;
import com.dmcdoc.usermanagement.core.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminServiceTest {

    @Mock
    private UserRepository userRepo;
    @Mock
    private RoleRepository roleRepo;
    @InjectMocks
    private AdminService adminService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllUsers_returnsList() {
        User u = User.builder().id(UUID.randomUUID()).email("a@x.com").username("a").roles(new HashSet<>()).build();
        when(userRepo.findAll()).thenReturn(List.of(u));
        var res = adminService.getAllUsers();
        assertEquals(1, res.size());
        verify(userRepo).findAll();
    }

    @Test
    void addRoleToUser_success() {
        Role r = Role.builder().id(1L).name("ROLE_ADMIN").build();
        User u = User.builder().id(UUID.randomUUID()).email("b@x.com").username("b").roles(new HashSet<>()).build();

        when(userRepo.findById(u.getId())).thenReturn(Optional.of(u));
        when(roleRepo.findByName("ROLE_ADMIN")).thenReturn(Optional.of(r));
        adminService.addRoleToUser(u.getId(), "ROLE_ADMIN");
        assertTrue(u.getRoles().contains(r));
        verify(userRepo).save(u);
    }

    @Test
    void removeRoleFromUser_missingRole_throws() {
        User u = User.builder().id(UUID.randomUUID()).email("c@x.com").username("c").roles(new HashSet<>()).build();
        when(userRepo.findById(u.getId())).thenReturn(Optional.of(u));
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> adminService.removeRoleFromUser(u.getId(), "ROLE_NOT"));
        assertTrue(ex.getMessage().contains("User does not have role"));
    }

    @Test
    void addRole_userNotFound_throws() {
        when(userRepo.findById(UUID.randomUUID())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> adminService.addRoleToUser(UUID.randomUUID(), "ROLE_USER"));
    }

    @Test
    void addRole_roleNotFound_throws() {
        User u = User.builder().id(UUID.randomUUID()).email("d@x.com").username("d").roles(new HashSet<>()).build();
        when(userRepo.findById(u.getId())).thenReturn(Optional.of(u));
        when(roleRepo.findByName("ROLE_X")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> adminService.addRoleToUser(u.getId(), "ROLE_X"));
    }
}
