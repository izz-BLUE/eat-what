package com.fantuan.eatwhat.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtTokenServiceTest {

    private JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() {
        jwtTokenService = new JwtTokenService();
        ReflectionTestUtils.setField(jwtTokenService, "secret", "test-jwt-secret-key-at-least-32-bytes");
        ReflectionTestUtils.setField(jwtTokenService, "expirationSeconds", 3600L);
        jwtTokenService.init();
    }

    @Test
    void generateToken_success() {
        // When
        String token = jwtTokenService.generateToken(1L);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void parseToken_success() {
        // Given
        String token = jwtTokenService.generateToken(1L);

        // When
        Long userId = jwtTokenService.parseToken(token);

        // Then
        assertEquals(1L, userId);
    }

    @Test
    void parseToken_tamperedToken_returnsNull() {
        // Given
        String token = jwtTokenService.generateToken(1L);
        String tamperedToken = token + "tampered";

        // When
        Long userId = jwtTokenService.parseToken(tamperedToken);

        // Then
        assertNull(userId);
    }

    @Test
    void parseToken_expiredToken_returnsNull() throws InterruptedException {
        // Given
        ReflectionTestUtils.setField(jwtTokenService, "expirationSeconds", 1L);
        jwtTokenService.init();
        String token = jwtTokenService.generateToken(1L);

        // Wait for token to expire
        Thread.sleep(2000);

        // When
        Long userId = jwtTokenService.parseToken(token);

        // Then
        assertNull(userId);
    }

    @Test
    void parseToken_wrongKey_returnsNull() {
        // Given
        String token = jwtTokenService.generateToken(1L);

        // Create service with different key
        JwtTokenService otherService = new JwtTokenService();
        ReflectionTestUtils.setField(otherService, "secret", "different-secret-key-at-least-32-bytes-long");
        ReflectionTestUtils.setField(otherService, "expirationSeconds", 3600L);
        otherService.init();

        // When
        Long userId = otherService.parseToken(token);

        // Then
        assertNull(userId);
    }

    @Test
    void getExpirationSeconds_returnsConfiguredValue() {
        // When
        Long expiration = jwtTokenService.getExpirationSeconds();

        // Then
        assertEquals(3600L, expiration);
    }

    @Test
    void init_shortSecret_throwsException() {
        // Given
        JwtTokenService service = new JwtTokenService();
        ReflectionTestUtils.setField(service, "secret", "too-short");
        ReflectionTestUtils.setField(service, "expirationSeconds", 3600L);

        // When & Then
        assertThrows(IllegalArgumentException.class, service::init);
    }

    @Test
    void init_exact32Bytes_success() {
        // Given
        JwtTokenService service = new JwtTokenService();
        ReflectionTestUtils.setField(service, "secret", "12345678901234567890123456789012"); // exactly 32 bytes
        ReflectionTestUtils.setField(service, "expirationSeconds", 3600L);

        // When
        service.init();

        // Then
        String token = service.generateToken(1L);
        assertNotNull(token);
        assertEquals(1L, service.parseToken(token));
    }
}
