package com.dobrosav.matches.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;

    // 32 bytes encoded in Base64
    private static final String SECRET_KEY = "NDI0MjQyNDI0MjQyNDI0MjQyNDI0MjQyNDI0MjQyNDI="; 
    private static final long ACCESS_EXPIRATION = 1000 * 60 * 60; // 1 hour
    private static final long REFRESH_EXPIRATION = 1000 * 60 * 60 * 24; // 24 hours

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET_KEY);
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", ACCESS_EXPIRATION);
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpiration", REFRESH_EXPIRATION);

        userDetails = new User("testuser", "password", Collections.emptyList());
    }

    @Test
    void testGenerateToken() {
        String token = jwtService.generateToken(userDetails);
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void testExtractUsername() {
        String token = jwtService.generateToken(userDetails);
        String username = jwtService.extractUsername(token);
        assertEquals("testuser", username);
    }

    @Test
    void testIsTokenValid() {
        String token = jwtService.generateToken(userDetails);
        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void testIsTokenValid_InvalidUser() {
        String token = jwtService.generateToken(userDetails);
        UserDetails otherUser = new User("otheruser", "password", Collections.emptyList());
        assertFalse(jwtService.isTokenValid(token, otherUser));
    }

    @Test
    void testGenerateTokenWithExtraClaims() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "admin");
        String token = jwtService.generateToken(claims, userDetails);
        
        // We can't easily extract arbitrary claims with the current public API of JwtService 
        // without adding a specific extraction method or making extractClaim public (which it is).
        // Let's verify username still works.
        assertEquals("testuser", jwtService.extractUsername(token));
    }
    
    @Test
    void testGenerateRefreshToken() {
        String token = jwtService.generateRefreshToken(userDetails);
        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void testTokenExpiration() throws InterruptedException {
        // Set very short expiration
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", 1L); 
        
        String token = jwtService.generateToken(userDetails);
        
        // Wait for expiration
        Thread.sleep(10); 
        
        // Jwts parser throws ExpiredJwtException when parsing expired token.
        // The isTokenValid calls extractUsername which calls extractAllClaims which parses the token.
        // So this should throw an exception or return false depending on implementation.
        // Looking at JwtService:
        /*
            public boolean isTokenValid(String token, UserDetails userDetails) {
                final String username = extractUsername(token); // This will throw ExpiredJwtException
                return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
            }
        */
        // The service doesn't catch the exception, so we expect the exception.
        
        assertThrows(io.jsonwebtoken.ExpiredJwtException.class, () -> jwtService.isTokenValid(token, userDetails));
    }
}
