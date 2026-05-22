//package com.gateway.infrastructure.config;
//
//import io.jsonwebtoken.JwtException;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.test.util.ReflectionTestUtils;
//
//import java.time.Instant;
//import java.util.Date;
//
//import static org.junit.jupiter.api.Assertions.*;
//
///**
// * Unit Test cho JwtService
// *
// * Test các chức năng:
// * - Tạo và validate JWT token
// * - Extract các claims từ token
// * - Xử lý exception khi token không hợp lệ
// */
//@ExtendWith(MockitoExtension.class)
//class JwtServiceTest {
//
//    @InjectMocks
//    private JwtService jwtService;
//
//    private static final String TEST_SECRET = "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437";
//    private static final String TEST_USER_ID = "123e4567-e89b-12d3-a456-426614174000";
//    private static final String TEST_USERNAME = "testuser";
//    private static final String TEST_FULLNAME = "Test User";
//
//    @BeforeEach
//    void setUp() {
//        // Set secret key bằng reflection vì @Value không hoạt động trong unit test
//        ReflectionTestUtils.setField(jwtService, "jwtSecret", TEST_SECRET);
//        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 86400000L);
//    }
//
//    @Test
//    void testGetSigningKey() {
//        // Test getter method
//        String secret = jwtService.getJwtSecret();
//        assertEquals(TEST_SECRET, secret);
//    }
//
//    @Test
//    void testGetJwtExpiration() {
//        Long expiration = jwtService.getJwtExpiration();
//        assertEquals(86400000L, expiration);
//    }
//
//    @Test
//    void testExtractAllClaims_ValidToken() {
//        String validToken = createTestToken();
//
//        // Không throw exception
//        assertDoesNotThrow(() -> {
//            var claims = jwtService.extractAllClaims(validToken);
//            assertNotNull(claims);
//            assertEquals(TEST_USERNAME, claims.getSubject());
//            assertEquals(TEST_USER_ID, claims.get("userId"));
//            assertEquals(TEST_FULLNAME, claims.get("fullname"));
//        });
//    }
//
//    @Test
//    void testExtractAllClaims_InvalidToken_ThrowsException() {
//        String invalidToken = "invalid.token.here";
//
//        assertThrows(JwtException.class, () -> {
//            jwtService.extractAllClaims(invalidToken);
//        });
//    }
//
//    @Test
//    void testExtractUserId_ValidToken() {
//        String validToken = createTestToken();
//
//        assertDoesNotThrow(() -> {
//            String userId = jwtService.extractUserId(validToken);
//            assertEquals(TEST_USER_ID, userId);
//        });
//    }
//
//    @Test
//    void testExtractUsername_ValidToken() {
//        String validToken = createTestToken();
//
//        assertDoesNotThrow(() -> {
//            String username = jwtService.extractUsername(validToken);
//            assertEquals(TEST_USERNAME, username);
//        });
//    }
//
//    @Test
//    void testExtractFullname_ValidToken() {
//        String validToken = createTestToken();
//
//        assertDoesNotThrow(() -> {
//            String fullname = jwtService.extractFullname(validToken);
//            assertEquals(TEST_FULLNAME, fullname);
//        });
//    }
//
//    @Test
//    void testExtractFullname_TokenWithoutFullname_ReturnsNull() {
//        // Tạo token không có claim fullname
//        String tokenWithoutFullname = io.jsonwebtoken.Jwts.builder()
//                .subject(TEST_USERNAME)
//                .claim("userId", TEST_USER_ID)
//                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(
//                        TEST_SECRET.getBytes(java.nio.charset.StandardCharsets.UTF_8)))
//                .compact();
//
//        assertDoesNotThrow(() -> {
//            String fullname = jwtService.extractFullname(tokenWithoutFullname);
//            assertNull(fullname);
//        });
//    }
//
//    @Test
//    void testIsTokenExpired_NotExpired() {
//        String validToken = createTestToken();
//
//        assertFalse(jwtService.isTokenExpired(validToken));
//    }
//
//    @Test
//    void testValidateToken_ValidToken_ReturnsTrue() {
//        String validToken = createTestToken();
//
//        assertTrue(jwtService.validateToken(validToken));
//    }
//
//    @Test
//    void testValidateToken_InvalidToken_ReturnsFalse() {
//        String invalidToken = "invalid.token.here";
//
//        assertFalse(jwtService.validateToken(invalidToken));
//    }
//
//    @Test
//    void testExtractExpiration_ValidToken() {
//        String validToken = createTestToken();
//
//        assertDoesNotThrow(() -> {
//            Date expiration = jwtService.extractExpiration(validToken);
//            assertNotNull(expiration);
//            assertTrue(expiration.after(new Date()));
//        });
//    }
//
//    /**
//     * Helper method để tạo test JWT token
//     */
//    private String createTestToken() {
//        Instant now = Instant.now();
//        Instant expiration = now.plusSeconds(86400);
//
//        return io.jsonwebtoken.Jwts.builder()
//                .subject(TEST_USERNAME)
//                .claim("userId", TEST_USER_ID)
//                .claim("fullname", TEST_FULLNAME)
//                .issuedAt(Date.from(now))
//                .expiration(Date.from(expiration))
//                .issuer("api-gateway")
//                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(
//                        TEST_SECRET.getBytes(java.nio.charset.StandardCharsets.UTF_8)))
//                .compact();
//    }
//}
