package com.hyundai.dms.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    // base64-encoded 256-bit key (same as application-test.yml)
    private static final String SECRET = "dGhpcyBpcyBhIDI1NiBiaXQgc2VjcmV0IGtleSBmb3IgSFMyNTYgand0IHNpZ25pbmcgdXNlZCBpbiBkbXM=";
    private static final long ACCESS_EXPIRY = 28_800_000L;   // 8 hours
    private static final long REFRESH_EXPIRY = 604_800_000L; // 7 days

    private UserDetails testUser;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET, ACCESS_EXPIRY, REFRESH_EXPIRY);
        testUser = new org.springframework.security.core.userdetails.User(
                "admin", "password",
                Collections.singleton(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"))
        );
    }

    // ── generateAccessToken ──

    @Test
    @DisplayName("generateAccessToken returns non-null, non-empty token")
    void generateAccessToken_returnsToken() {
        String token = jwtUtil.generateAccessToken(testUser, Map.of("role", "SUPER_ADMIN", "userId", 1L));
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    @DisplayName("generateAccessToken embeds custom claims")
    void generateAccessToken_containsClaims() {
        String token = jwtUtil.generateAccessToken(testUser, Map.of("role", "SUPER_ADMIN", "userId", 42L));
        String role = jwtUtil.extractClaim(token, claims -> claims.get("role", String.class));
        assertEquals("SUPER_ADMIN", role);
    }

    @Test
    @DisplayName("generateAccessToken sets correct subject to username")
    void generateAccessToken_usernameAsSubject() {
        String token = jwtUtil.generateAccessToken(testUser, Map.of());
        assertEquals("admin", jwtUtil.extractUsername(token));
    }

    // ── generateRefreshToken ──

    @Test
    @DisplayName("generateRefreshToken returns non-null token")
    void generateRefreshToken_returnsToken() {
        String token = jwtUtil.generateRefreshToken("admin");
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    @DisplayName("generateRefreshToken has correct subject")
    void generateRefreshToken_correctSubject() {
        String token = jwtUtil.generateRefreshToken("salesuser");
        assertEquals("salesuser", jwtUtil.extractUsername(token));
    }

    @Test
    @DisplayName("generateRefreshToken generates unique jti each time")
    void generateRefreshToken_uniqueJti() {
        String token1 = jwtUtil.generateRefreshToken("admin");
        String token2 = jwtUtil.generateRefreshToken("admin");
        assertNotEquals(token1, token2);
    }

    // ── validateToken ──

    @Test
    @DisplayName("validateToken returns true for valid token and matching user")
    void validateToken_valid() {
        String token = jwtUtil.generateAccessToken(testUser, Map.of());
        assertTrue(jwtUtil.validateToken(token, testUser));
    }

    @Test
    @DisplayName("validateToken returns false when username does not match")
    void validateToken_wrongUser() {
        String token = jwtUtil.generateAccessToken(testUser, Map.of());
        UserDetails otherUser = new org.springframework.security.core.userdetails.User(
                "otheruser", "password", Collections.emptyList()
        );
        assertFalse(jwtUtil.validateToken(token, otherUser));
    }

    @Test
    @DisplayName("validateToken throws for expired token")
    void validateToken_expired() {
        // Create a JwtUtil with 0ms expiry
        JwtUtil expiredJwtUtil = new JwtUtil(SECRET, 0L, 0L);
        String token = expiredJwtUtil.generateAccessToken(testUser, Map.of());

        assertThrows(ExpiredJwtException.class, () -> jwtUtil.validateToken(token, testUser));
    }

    // ── extractUsername ──

    @Test
    @DisplayName("extractUsername returns correct username from access token")
    void extractUsername_fromAccessToken() {
        String token = jwtUtil.generateAccessToken(testUser, Map.of());
        assertEquals("admin", jwtUtil.extractUsername(token));
    }

    @Test
    @DisplayName("extractUsername returns correct username from refresh token")
    void extractUsername_fromRefreshToken() {
        String token = jwtUtil.generateRefreshToken("manager");
        assertEquals("manager", jwtUtil.extractUsername(token));
    }

    // ── extractExpiration ──

    @Test
    @DisplayName("extractExpiration returns a future date for valid token")
    void extractExpiration_future() {
        String token = jwtUtil.generateAccessToken(testUser, Map.of());
        Date expiration = jwtUtil.extractExpiration(token);
        assertTrue(expiration.after(new Date()));
    }

    // ── Edge cases ──

    @Test
    @DisplayName("extractUsername throws for malformed token")
    void extractUsername_malformedToken() {
        assertThrows(MalformedJwtException.class, () -> jwtUtil.extractUsername("not.a.jwt"));
    }

    @Test
    @DisplayName("extractUsername throws for tampered token")
    void extractUsername_tamperedToken() {
        String token = jwtUtil.generateAccessToken(testUser, Map.of());
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";
        assertThrows(Exception.class, () -> jwtUtil.extractUsername(tampered));
    }

    // ── getRefreshTokenExpiryMs ──

    @Test
    @DisplayName("getRefreshTokenExpiryMs returns configured value")
    void getRefreshTokenExpiryMs() {
        assertEquals(REFRESH_EXPIRY, jwtUtil.getRefreshTokenExpiryMs());
    }
}
