package com.hyundai.dms.security;

import com.hyundai.dms.common.enums.RoleName;
import com.hyundai.dms.module.user.entity.Permission;
import com.hyundai.dms.module.user.entity.Role;
import com.hyundai.dms.module.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CustomUserDetailsTest {

    private Role testRole;
    private User testUser;

    @BeforeEach
    void setUp() {
        testRole = Role.builder()
                .name(RoleName.SUPER_ADMIN)
                .description("Super Admin")
                .permissions(new HashSet<>())
                .menus(new HashSet<>())
                .build();
        testRole.setId(1L);

        testUser = User.builder()
                .username("admin")
                .email("admin@hyundai.in")
                .passwordHash("$2a$10$hashedpassword")
                .roles(Set.of(testRole))
                .isActive(true)
                .failedLoginAttempts(0)
                .forcePasswordChange(false)
                .build();
        testUser.setId(1L);
    }

    // ── Basic properties ──

    @Test
    @DisplayName("getUsername returns correct username")
    void getUsername() {
        CustomUserDetails details = new CustomUserDetails(testUser);
        assertEquals("admin", details.getUsername());
    }

    @Test
    @DisplayName("getPassword returns password hash")
    void getPassword() {
        CustomUserDetails details = new CustomUserDetails(testUser);
        assertEquals("$2a$10$hashedpassword", details.getPassword());
    }

    @Test
    @DisplayName("getEmail returns email")
    void getEmail() {
        CustomUserDetails details = new CustomUserDetails(testUser);
        assertEquals("admin@hyundai.in", details.getEmail());
    }

    @Test
    @DisplayName("getId returns user ID")
    void getId() {
        CustomUserDetails details = new CustomUserDetails(testUser);
        assertEquals(1L, details.getId());
    }

    @Test
    @DisplayName("getUser returns original User entity")
    void getUser() {
        CustomUserDetails details = new CustomUserDetails(testUser);
        assertSame(testUser, details.getUser());
    }

    // ── Account status ──

    @Test
    @DisplayName("isEnabled returns true when user is active")
    void isEnabled_active() {
        CustomUserDetails details = new CustomUserDetails(testUser);
        assertTrue(details.isEnabled());
    }

    @Test
    @DisplayName("isEnabled returns false when user is inactive")
    void isEnabled_inactive() {
        testUser.setIsActive(false);
        CustomUserDetails details = new CustomUserDetails(testUser);
        assertFalse(details.isEnabled());
    }

    @Test
    @DisplayName("isAccountNonLocked returns true when not locked")
    void isAccountNonLocked_notLocked() {
        CustomUserDetails details = new CustomUserDetails(testUser);
        assertTrue(details.isAccountNonLocked());
    }

    @Test
    @DisplayName("isAccountNonLocked returns false when recently locked")
    void isAccountNonLocked_locked() {
        testUser.setLockedAt(LocalDateTime.now());
        CustomUserDetails details = new CustomUserDetails(testUser);
        assertFalse(details.isAccountNonLocked());
    }

    @Test
    @DisplayName("isAccountNonLocked returns true when lock expired (31+ min)")
    void isAccountNonLocked_lockExpired() {
        testUser.setLockedAt(LocalDateTime.now().minusMinutes(31));
        CustomUserDetails details = new CustomUserDetails(testUser);
        assertTrue(details.isAccountNonLocked());
    }

    @Test
    @DisplayName("isAccountNonExpired always returns true")
    void isAccountNonExpired() {
        CustomUserDetails details = new CustomUserDetails(testUser);
        assertTrue(details.isAccountNonExpired());
    }

    @Test
    @DisplayName("isCredentialsNonExpired always returns true")
    void isCredentialsNonExpired() {
        CustomUserDetails details = new CustomUserDetails(testUser);
        assertTrue(details.isCredentialsNonExpired());
    }

    // ── Authorities ──

    @Test
    @DisplayName("Authorities include ROLE_ prefix")
    void authorities_includeRolePrefix() {
        CustomUserDetails details = new CustomUserDetails(testUser);
        Collection<? extends GrantedAuthority> auths = details.getAuthorities();

        assertTrue(auths.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN")));
    }

    @Test
    @DisplayName("Authorities include permission-based authorities for CREATE")
    void authorities_permissionCreate() {
        Permission p = Permission.builder()
                .moduleName("INVENTORY")
                .canCreate(true)
                .canRead(false)
                .canUpdate(false)
                .canDelete(false)
                .build();
        p.setId(1L);
        testRole.getPermissions().add(p);

        CustomUserDetails details = new CustomUserDetails(testUser);
        assertTrue(details.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("INVENTORY_CREATE")));
        assertFalse(details.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("INVENTORY_READ")));
    }

    @Test
    @DisplayName("Authorities include all CRUD permission authorities")
    void authorities_allCrud() {
        Permission p = Permission.builder()
                .moduleName("SALES")
                .canCreate(true)
                .canRead(true)
                .canUpdate(true)
                .canDelete(true)
                .build();
        p.setId(2L);
        testRole.getPermissions().add(p);

        CustomUserDetails details = new CustomUserDetails(testUser);
        Set<String> authStrings = new HashSet<>();
        details.getAuthorities().forEach(a -> authStrings.add(a.getAuthority()));

        assertTrue(authStrings.contains("SALES_CREATE"));
        assertTrue(authStrings.contains("SALES_READ"));
        assertTrue(authStrings.contains("SALES_UPDATE"));
        assertTrue(authStrings.contains("SALES_DELETE"));
        assertTrue(authStrings.contains("ROLE_SUPER_ADMIN"));
    }

    @Test
    @DisplayName("No duplicate authorities for same permission")
    void authorities_noDuplicates() {
        Permission p = Permission.builder()
                .moduleName("INVENTORY")
                .canCreate(true)
                .canRead(true)
                .canUpdate(false)
                .canDelete(false)
                .build();
        p.setId(1L);
        testRole.getPermissions().add(p);

        CustomUserDetails details = new CustomUserDetails(testUser);
        long count = details.getAuthorities().stream()
                .filter(a -> a.getAuthority().equals("INVENTORY_CREATE"))
                .count();
        assertEquals(1, count);
    }

    @Test
    @DisplayName("Empty permissions results in only role authority")
    void authorities_noPermissions() {
        CustomUserDetails details = new CustomUserDetails(testUser);
        assertEquals(1, details.getAuthorities().size());
        assertTrue(details.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN")));
    }
}
