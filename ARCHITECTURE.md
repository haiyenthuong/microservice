# Architecture Documentation - Event-Driven Microservices Refactor

## 📋 Phase 1: API Gateway Implementation

### 🚨 Problem: Shared Secret Key Anti-pattern

#### Current Architecture (BEFORE - Anti-pattern)

```
┌─────────────────────────────────────────────────────────────────┐
│                         Client                                  │
└───────────────────────────┬─────────────────────────────────────┘
                            │
        ┌───────────────────┼───────────────────┐
        │                   │                   │
        ▼                   ▼                   ▼
┌─────────────┐      ┌─────────────┐      ┌─────────────┐
│  CMS        │      │  ORDER      │      │  PAYMENT    │
│  Service    │      │  Service    │      │  Service    │
│             │      │             │      │             │
│ ┌─────────┐ │      │ ┌─────────┐ │      │ ┌─────────┐ │
│ │JwtService│ │      │ │JwtService│ │      │ │JwtService│ │
│ │Security │ │      │ │Security │ │      │ │Security │ │ │
│ │Config   │ │      │ │Config   │ │      │ │Config   │ │ │
│ └─────────┘ │      │ └─────────┘ │      │ └─────────┘ │
│             │      │             │      │             │
│ Secret Key  │      │ Secret Key  │      │ Secret Key  │
│ (Copy-Paste)│      │ (Copy-Paste) │      │ (Copy-Paste) │
└─────────────┘      └─────────────┘      └─────────────┘
```

#### Vấn đề

1. **Code Duplication**: `JwtService`, `SecurityConfig` bị copy-paste sang N services
2. **Maintenance Nightmare**: Thay đổi secret key phải cập nhật N services
3. **Security Risk**: Một service bị lộ secret key ⇒ hacker có thể fake token cho tất cả services
4. **Performance Waste**: Mỗi service đều phải decrypt JWT token
5. **Inconsistency**: Khó đảm bảo tất cả services dùng cùng một version của validation logic

### ✅ Solution: Centralized Authentication via API Gateway

#### Target Architecture (AFTER - Event-Driven Ready)

```
┌─────────────────────────────────────────────────────────────────┐
│                         Client                                   │
└───────────────────────────┬─────────────────────────────────────┘
                            │
                            │ JWT Token
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                      API Gateway (8080)                          │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │         Centralized Authentication                       │   │
│  │  - JwtService (single source of truth)                  │   │
│  │  - SecurityConfig (one place to maintain)               │   │
│  │  - AuthorizeFilter (validate JWT once)                  │   │
│  └─────────────────────────────────────────────────────────┘   │
│                              │                                   │
│                              │ Inject Headers:                   │
│                              │ - X-User-Id                       │
│                              │ - X-User-Name                     │
│                              │ - X-User-Fullname                 │
└──────────────────────────────┼───────────────────────────────────┘
                               │
        ┌──────────────────────┼──────────────────────┐
        │                      │                      │
        ▼                      ▼                      ▼
┌─────────────┐        ┌─────────────┐        ┌─────────────┐
│  CMS        │        │  ORDER      │        │  PAYMENT    │
│  Service    │        │  Service    │        │  Service    │
│             │        │             │        │             │
│ ┌─────────┐ │        │ ┌─────────┐ │        │ ┌─────────┐ │
│ │Read     │ │        │ │Read     │ │        │ │Read     │ │
│ │Headers  │ │        │ │Headers  │ │        │ │Headers  │ │
│ └─────────┘ │        │ └─────────┘ │        │ └─────────┘ │
│             │        │             │        │             │
│ NO JWT      │        │ NO JWT      │        │ NO JWT      │
│ Validation  │        │ Validation  │        │ Validation  │
└─────────────┘        └─────────────┘        └─────────────┘
```

#### Lợi ích

1. ✅ **Single Responsibility**: Authentication chỉ làm tại 1 chỗ
2. ✅ **Easier Maintenance**: Thay đổi secret key ⇒ update 1 service (gateway)
3. ✅ **Better Security**: Secret key chỉ tồn tại tại gateway
4. ✅ **Better Performance**: Services không cần decrypt JWT
5. ✅ **Flexibility**: Dễ dàng thêm rate limiting, caching, monitoring

### 🔄 Authentication Flow Detail

#### Step 1: Client sends request with JWT

```http
GET /api/orders/123 HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huLmRvZSIsInVzZXJJZCI6IjEyM2U0NTY3IiwiZnVsbG5hbWUiOiJKb2huIERvZSJ9.xxx
```

#### Step 2: LoggingFilter logs incoming request

```
2024-05-20 10:00:00.000 INFO  --- INCOMING REQUEST ===
2024-05-20 10:00:00.001 INFO  --- Method: GET
2024-05-20 10:00:00.002 INFO  --- Path: /api/orders/123
2024-05-20 10:00:00.003 INFO  --- ========================
```

#### Step 3: AuthorizeFilter validates JWT

```java
// Pseudo-code
String token = extractToken(request); // "Bearer xxx" → "xxx"

// Validate JWT (centralized logic)
Claims claims = jwtService.extractAllClaims(token);

// Extract user info
String userId = claims.get("userId");
String username = claims.getSubject();
String fullname = claims.get("fullname");

// Inject headers
ServerHttpRequest mutatedRequest = request.mutate()
    .header("X-User-Id", userId)
    .header("X-User-Name", username)
    .header("X-User-Fullname", fullname)
    .build();
```

#### Step 4: Gateway forwards request to Order Service

```http
GET /orders/123 HTTP/1.1
Host: order-service:8082
X-User-Id: 123e4567-e89b-12d3-a456-426614174000
X-User-Name: john.doe
X-User-Fullname: John Doe
```

#### Step 5: Order Service processes request

```java
@RestController
@RequestMapping("/orders")
public class OrderController {

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(
        @PathVariable String id,
        @RequestHeader("X-User-Id") String userId,
        @RequestHeader("X-User-Name") String username,
        @RequestHeader("X-User-Fullname") String fullname
    ) {
        // Sử dụng thông tin user từ headers
        log.info("User {} ({}) is accessing order {}", fullname, userId, id);

        // Không cần validate JWT lại!
        // Business logic here...
    }
}
```

### 🧱 Moving Towards Event-Driven Architecture

API Gateway là **Foundation block** cho Event-Driven Architecture:

#### Current Phase (Phase 1): Request-Response with Centralized Auth
```
Client → Gateway (Auth) → Service (Business Logic) → Response
```

#### Next Phases: Adding Events
```
Client → Gateway (Auth) → Service → Publish Event → Other Services Subscribe
                                        ↓
                                    Database
```

#### Benefits for Event-Driven

1. **Consistent User Context**: Events can include `userId`, `username` from headers
2. **Security**: Authenticated context được maintain trong event chain
3. **Traceability**: X-User-Id header giúp track user actions trong event logs

### 📊 Technology Stack

| Component | Technology | Purpose |
|-----------|-----------|---------|
| Gateway | Spring Cloud Gateway | Reactive gateway for routing |
| Auth | JWT (jjwt) | Stateless authentication |
| Reactive | WebFlux + Reactor | Non-blocking I/O |
| Logging | Logback | Request/response logging |
| Config | Spring Boot Configuration | Externalized configuration |

### 🔐 Security Considerations

#### Production Checklist

- [ ] Use environment variables for JWT secret
- [ ] Enable HTTPS/TLS
- [ ] Implement rate limiting
- [ ] Add request correlation IDs
- [ ] Monitor authentication failures
- [ ] Set up JWT refresh token mechanism
- [ ] Rotate secret keys periodically
- [ ] Disable TestController in production

#### Example Environment Variables

```bash
# .env (production)
JWT_SECRET=${VAULT_JWT_SECRET}  # Lấy từ Vault/Secret Manager
JWT_EXPIRATION=3600000           # 1 giờ
GATEWAY_TEST_ENABLED=false
SPRING_PROFILES_ACTIVE=prod
```

### 📈 Performance Implications

#### Before (Anti-pattern)
- **Request to CMS**: 1 JWT decrypt
- **Request to Order**: 1 JWT decrypt
- **Request to Payment**: 1 JWT decrypt
- **Total**: 3 JWT decrypts per multi-service call

#### After (Gateway)
- **Request to any service**: 1 JWT decrypt (at gateway)
- **Total**: 1 JWT decrypt per request

**Performance Gain**: ~66% reduction in cryptographic operations

### 🔄 Migration Path

#### Step 1: Deploy Gateway (Current)
- Deploy api-gateway at port 8080
- Keep existing services running
- Gradually migrate client traffic

#### Step 2: Remove JWT from Services
- Remove `JwtService` from services
- Remove `SecurityConfig` from services
- Add `@RequestHeader` parameters to controllers

#### Step 3: Update Clients
- Update client base URLs to point to gateway
- Remove JWT validation from clients

#### Step 4: Event-Driven Integration (Future)
- Add message broker (Kafka/RabbitMQ)
- Services publish events instead of direct calls
- Gateway remains as auth boundary

### 📚 Related Patterns

1. **API Gateway Pattern**: Single entry point for microservices
2. **Ambassador Pattern**: Gateway acts as ambassador for external services
3. **Sidecar Pattern**: (Alternative) Auth sidecar for each service
4. **Backend for Frontend**: Gateway aggregates multiple service calls

### 🎯 Success Metrics

- ✅ Zero JWT code duplication in services
- ✅ Single point of authentication management
- ✅ < 50ms latency overhead from gateway
- ✅ 99.9% uptime for authentication
- ✅ Clear audit trail via headers
