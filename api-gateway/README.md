# API Gateway Module

## 📋 Tổng quan

API Gateway là entry point duy nhất cho tất cả client requests trong hệ thống Microservices. Nó chịu trách nhiệm:

- ✅ **Centralized Authentication**: Xác thực JWT token tập trung
- ✅ **Request Routing**: Định tuyến requests đến các microservices phù hợp
- ✅ **Header Injection**: Chuyển tiếp thông tin user qua HTTP headers
- ✅ **CORS Handling**: Xử lý Cross-Origin requests
- ✅ **Logging**: Ghi log tất cả requests/responses

## 🏗️ Kiến trúc

```
┌─────────────────────────────────────────────────────────────────┐
│                         Client Application                        │
└─────────────────────────────┬───────────────────────────────────┘
                              │
                              │ JWT Token (Authorization: Bearer xxx)
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      API Gateway (Port 8080)                      │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │              LoggingFilter (Global)                      │   │
│  │  - Log incoming requests                                 │   │
│  │  - Log outgoing responses                                │   │
│  └─────────────────────────────────────────────────────────┘   │
│                              ▼                                   │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │              AuthorizeFilter (Per Route)                 │   │
│  │  - Validate JWT token                                    │   │
│  │  - Extract user info (userId, username, fullname)        │   │
│  │  - Inject headers:                                       │   │
│  │    * X-User-Id                                           │   │
│  │    * X-User-Name                                         │   │
│  │    * X-User-Fullname                                     │   │
│  └─────────────────────────────────────────────────────────┘   │
│                              ▼                                   │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │              Router (Based on Path)                      │   │
│  │  /api/cms/**     → cms-service (8081)                    │   │
│  │  /api/orders/**  → order-service (8082)                  │   │
│  │  /api/payments/** → payment-service (8083)               │   │
│  └─────────────────────────────────────────────────────────┘   │
└─────────────────────────────┬───────────────────────────────────┘
                              │
                              │ Headers: X-User-Id, X-User-Name, X-User-Fullname
                              ▼
      ┌───────────────────────┼───────────────────────┐
      │                       │                       │
      ▼                       ▼                       ▼
┌─────────┐            ┌──────────┐            ┌────────────┐
│  CMS    │            │  ORDER   │            │  PAYMENT   │
│ Service │            │ Service  │            │  Service   │
│  :8081  │            │  :8082   │            │   :8083    │
└─────────┘            └──────────┘            └────────────┘
```

## 🚀 Cách chạy

### 1. Build project
```bash
cd microservice
mvn clean install
```

### 2. Chạy API Gateway
```bash
cd api-gateway
mvn spring-boot:run
```

Hoặc chạy trực tiếp từ JAR:
```bash
java -jar api-gateway/target/api-gateway-1.0.0.jar
```

### 3. Verify Gateway đang chạy
```bash
curl http://localhost:8080/actuator/health
```

## 📡 API Routes

| Path | Destination | Description |
|------|-------------|-------------|
| `/api/cms/**` | cms-service:8081 | CMS APIs |
| `/api/orders/**` | order-service:8082 | Order APIs |
| `/api/payments/**` | payment-service:8083 | Payment APIs |
| `/public/**` | - | Public APIs (no auth) |

## 🔐 Authentication Flow

### 1. Client có JWT token
```bash
curl -X GET http://localhost:8080/api/orders/123 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

### 2. Gateway xử lý request
1. LoggingFilter log request incoming
2. AuthorizeFilter validate JWT token
3. Extract user info từ token claims
4. Inject headers vào request:
   - `X-User-Id: 123e4567-e89b-12d3-a456-426614174000`
   - `X-User-Name: john.doe`
   - `X-User-Fullname: John Doe`
5. Forward request đến destination service

### 3. Microservice nhận request với headers
```java
// Trong microservice controller
@GetMapping("/orders/{id}")
public ResponseEntity<Order> getOrder(
    @PathVariable String id,
    @RequestHeader("X-User-Id") String userId,
    @RequestHeader("X-User-Name") String username,
    @RequestHeader("X-User-Fullname") String fullname
) {
    // Sử dụng thông tin user từ headers thay vì validate JWT lại
    log.info("User {} ({}) is accessing order {}", fullname, userId, id);
    // ...
}
```

## 🧪 Testing

### Generate JWT Token (Development only)

Enable test endpoint trong `application.yml`:
```yaml
gateway:
  test:
    enabled: true
```

Generate token:
```bash
curl -X POST http://localhost:8080/public/generate-token \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "123e4567-e89b-12d3-a456-426614174000",
    "username": "testuser",
    "fullname": "Test User"
  }'
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsInVzZXJJZCI6IjEyM2U0NTY3LWU4OWItMTJkMy1hNDU2LTQyNjYxNDE3NDAwMCIsImZ1bGxuYW1lIjoiVGVzdCBVc2VyIiwiaXNzIjoiYXBpLWdhdGV3YXkiLCJpYXQiOjE2OTAwMDAwMDAsImV4cCI6MTY5MDA4NjQwMH0.xxx",
  "type": "Bearer",
  "expiresAt": "2024-07-22T10:00:00Z",
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "username": "testuser"
}
```

### Test với JWT Token
```bash
# Test order service
curl -X GET http://localhost:8080/api/orders/123 \
  -H "Authorization: Bearer <your-token-here>"

# Test payment service
curl -X POST http://localhost:8080/api/payments/create \
  -H "Authorization: Bearer <your-token-here>" \
  -H "Content-Type: application/json" \
  -d '{"orderId": "123", "amount": 100000}'
```

## ⚙️ Configuration

### application.yml
```yaml
server:
  port: 8080

jwt:
  secret: 5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437
  expiration: 86400000  # 24 giờ

spring:
  cloud:
    gateway:
      routes:
        - id: cms-service
          uri: http://localhost:8081
          predicates:
            - Path=/api/cms/**
          filters:
            - StripPrefix=2
            - AuthorizeFilter
```

### Environment Variables
```bash
export JWT_SECRET="your-secret-key"
export JWT_EXPIRATION="86400000"
export SERVER_PORT="8080"
```

## 🔧 Lỗi thường gặp

### 1. JWT Token không hợp lệ
```
Status: 401 Unauthorized
Response: {"error": "Unauthorized", "message": "Invalid or expired JWT token"}
```
**Giải pháp**: Kiểm tra token đã đúng chưa, hoặc token đã hết hạn chưa

### 2. Authorization header bị thiếu
```
Status: 401 Unauthorized
Response: {"error": "Unauthorized", "message": "Authorization header is required"}
```
**Giải pháp**: Thêm `Authorization: Bearer <token>` vào request headers

### 3. Destination service không available
```
Status: 503 Service Unavailable
```
**Giải pháp**: Kiểm tra service destination có đang chạy chưa

## 📝 JWT Token Structure

```json
{
  "sub": "john.doe",              // Username (subject)
  "userId": "123e4567-...",       // User ID
  "fullname": "John Doe",         // Full name
  "iss": "api-gateway",           // Issuer
  "iat": 1690000000,              // Issued at
  "exp": 1690086400               // Expiration
}
```

## 🎯 Lợi ích

1. **Single Authentication Point**: JWT chỉ được validate tại gateway
2. **Reduced Code Duplication**: Không cần copy JwtService sang mỗi service
3. **Easier Maintenance**: Thay đổi secret key chỉ cần sửa tại 1 chỗ
4. **Better Performance**: Microservices không cần decrypt JWT nữa
5. **Consistent Security**: Mọi requests đều đi qua cùng 1 authentication flow

## 🚨 Security Notes

⚠️ **QUAN TRỌNG CHO PRODUCTION**:

1. Không hardcode secret key trong code/config
   ```yaml
   jwt:
     secret: ${JWT_SECRET}  # Sử dụng environment variable
   ```

2. Sử dụng HTTPS trong production
3. Enable rate limiting
4. Monitor và log security events
5. Rotate secret keys định kỳ
6. Disable TestController trong production

## 📚 Xem thêm

- [Spring Cloud Gateway Documentation](https://spring.io/projects/spring-cloud-gateway)
- [JWT Best Practices](https://tools.ietf.org/html/rfc8725)
- [Microservices Patterns](https://microservices.io/patterns/apigateway.html)
