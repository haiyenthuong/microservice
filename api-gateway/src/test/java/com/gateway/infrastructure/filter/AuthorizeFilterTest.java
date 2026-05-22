//package com.gateway.infrastructure.filter;
//
//import com.gateway.infrastructure.config.JwtService;
//import io.jsonwebtoken.security.Keys;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.cloud.gateway.filter.GatewayFilterChain;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpStatus;
//import org.springframework.mock.web.server.MockServerHttpRequest;
//import org.springframework.mock.web.server.MockServerHttpResponse;
//import org.springframework.web.server.ServerWebExchange;
//import reactor.core.publisher.Mono;
//import reactor.test.StepVerifier;
//
//import java.nio.charset.StandardCharsets;
//import java.time.Instant;
//import java.util.Date;
//
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
///**
// * Unit Test cho AuthorizeFilter
// *
// * Test các chức năng:
// * - Validate JWT token trong Authorization header
// * - Inject user headers vào request
// * - Reject request khi token không hợp lệ
// */
//@ExtendWith(MockitoExtension.class)
//class AuthorizeFilterTest {
//
//    @Mock
//    private JwtService jwtService;
//
//    @InjectMocks
//    private AuthorizeFilter authorizeFilter;
//
//    private GatewayFilterChain filterChain;
//    private ServerWebExchange exchange;
//
//    private static final String TEST_SECRET = "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437";
//    private static final String VALID_TOKEN = createValidToken();
//    private static final String INVALID_TOKEN = "invalid.token.here";
//
//    @BeforeEach
//    void setUp() {
//        exchange = mock(ServerWebExchange.class);
//        filterChain = mock(GatewayFilterChain.class);
//
//        // Mock request
//        MockServerHttpRequest request = MockServerHttpRequest.get("/api/orders/123")
//                .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN)
//                .build();
//
//        MockServerHttpResponse response = new MockServerHttpResponse();
//
//        when(exchange.getRequest()).thenReturn(request);
//        when(exchange.getResponse()).thenReturn(response);
//        when(exchange.mutate()).thenReturn(mock(ServerWebExchange.Builder.class));
//    }
//
//    @Test
//    void testApply_ReturnsGatewayFilter() {
//        AuthorizeFilter.Config config = new AuthorizeFilter.Config();
//
//        var filter = authorizeFilter.apply(config);
//
//        assertNotNull(filter);
//    }
//
//    @Test
//    void testFilter_ValidToken_CallsChain() {
//        // Setup
//        when(jwtService.validateToken(any())).thenReturn(true);
//        when(jwtService.extractAllClaims(any())).thenReturn(io.jsonwebtoken.Jwts.claims()
//                .add("userId", "123e4567-e89b-12d3-a456-426614174000")
//                .add("fullname", "Test User")
//                .subject("testuser")
//                .build());
//
//        when(filterChain.filter(any())).thenReturn(Mono.empty());
//
//        // Execute
//        var filter = authorizeFilter.apply(new AuthorizeFilter.Config());
//        StepVerifier.create(filter.filter(exchange, filterChain))
//                .verifyComplete();
//
//        // Verify
//        verify(filterChain, times(1)).filter(any());
//    }
//
//    @Test
//    void testFilter_InvalidToken_DoesNotCallChain() {
//        // Setup
//        MockServerHttpRequest request = MockServerHttpRequest.get("/api/orders/123")
//                .header(HttpHeaders.AUTHORIZATION, "Bearer " + INVALID_TOKEN)
//                .build();
//
//        when(exchange.getRequest()).thenReturn(request);
//        when(jwtService.validateToken(INVALID_TOKEN)).thenReturn(false);
//
//        when(filterChain.filter(any())).thenReturn(Mono.empty());
//
//        // Execute
//        var filter = authorizeFilter.apply(new AuthorizeFilter.Config());
//        StepVerifier.create(filter.filter(exchange, filterChain))
//                .verifyComplete();
//
//        // Verify - chain should NOT be called for invalid token
//        verify(filterChain, never()).filter(any());
//    }
//
//    @Test
//    void testFilter_MissingAuthorizationHeader_DoesNotCallChain() {
//        // Setup - request without Authorization header
//        MockServerHttpRequest request = MockServerHttpRequest.get("/api/orders/123")
//                .build();
//
//        when(exchange.getRequest()).thenReturn(request);
//
//        when(filterChain.filter(any())).thenReturn(Mono.empty());
//
//        // Execute
//        var filter = authorizeFilter.apply(new AuthorizeFilter.Config());
//        StepVerifier.create(filter.filter(exchange, filterChain))
//                .verifyComplete();
//
//        // Verify - chain should NOT be called when auth header is missing
//        verify(filterChain, never()).filter(any());
//    }
//
//    @Test
//    void testFilter_InvalidAuthorizationFormat_DoesNotCallChain() {
//        // Setup - Authorization header without "Bearer" prefix
//        MockServerHttpRequest request = MockServerHttpRequest.get("/api/orders/123")
//                .header(HttpHeaders.AUTHORIZATION, INVALID_TOKEN)
//                .build();
//
//        when(exchange.getRequest()).thenReturn(request);
//
//        when(filterChain.filter(any())).thenReturn(Mono.empty());
//
//        // Execute
//        var filter = authorizeFilter.apply(new AuthorizeFilter.Config());
//        StepVerifier.create(filter.filter(exchange, filterChain))
//                .verifyComplete();
//
//        // Verify
//        verify(filterChain, never()).filter(any());
//    }
//
//    /**
//     * Helper method để tạo valid test token
//     */
//    private static String createValidToken() {
//        Instant now = Instant.now();
//        Instant expiration = now.plusSeconds(86400);
//
//        return io.jsonwebtoken.Jwts.builder()
//                .subject("testuser")
//                .claim("userId", "123e4567-e89b-12d3-a456-426614174000")
//                .claim("fullname", "Test User")
//                .issuedAt(Date.from(now))
//                .expiration(Date.from(expiration))
//                .issuer("api-gateway")
//                .signWith(Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8)))
//                .compact();
//    }
//}
