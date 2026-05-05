package com.shuttlebooking.config;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtProviderTest {

    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider("test-secret-key-that-is-long-enough-for-hmac-sha256-algorithm", 86400000);
    }

    @Test
    void generateToken_createsValidToken() {
        String token = jwtProvider.generateToken(1L, "test@example.com", "ROLE_USER");

        assertNotNull(token);
        assertTrue(jwtProvider.validateToken(token));
    }

    @Test
    void getEmailFromToken_returnsCorrectEmail() {
        String token = jwtProvider.generateToken(1L, "test@example.com", "ROLE_USER");

        String email = jwtProvider.getEmailFromToken(token);

        assertEquals("test@example.com", email);
    }

    @Test
    void parseToken_containsCorrectClaims() {
        String token = jwtProvider.generateToken(42L, "user@test.com", "ROLE_ADMIN");

        Claims claims = jwtProvider.parseToken(token);

        assertEquals("user@test.com", claims.getSubject());
        assertEquals(42L, claims.get("userId", Long.class));
        assertEquals("ROLE_ADMIN", claims.get("role", String.class));
    }

    @Test
    void validateToken_returnsFalse_forInvalidToken() {
        assertFalse(jwtProvider.validateToken("invalid.token.here"));
    }

    @Test
    void validateToken_returnsFalse_forExpiredToken() {
        JwtProvider expiredProvider = new JwtProvider("test-secret-key-that-is-long-enough-for-hmac-sha256-algorithm", -1);
        String token = expiredProvider.generateToken(1L, "test@example.com", "ROLE_USER");

        assertFalse(jwtProvider.validateToken(token));
    }
}
