# 🚀 Quick Start Guide - API Gateway

## 1. Kiểm tra điều kiện tiên quyết

```bash
# Kiểm tra Java version
java -version  # Cần Java 21+

# Kiểm tra Maven
mvn -version

# Kiểm tra các ports có đang được sử dụng không
netstat -an | grep "8080\|8081\|8082\|8083"
```

## 2. Build toàn bộ project

```bash
# Từ thư mục gốc microservice
cd d:/1.Project/microservice

# Clean và install toàn bộ modules
mvn clean install

# Hoặc skip tests để build nhanh hơn
mvn clean install -DskipTests
```

## 3. Chạy API Gateway

```bash
# Chạy Gateway (đơn lẻ)
cd api-gateway
mvn spring-boot:run

# Hoặc chạy từ project root
mvn -pl api-gateway spring-boot:run
```

**Gateway sẽ chạy trên port 8080**

## 4. Test Gateway Health Check

```bash
# Health check
curl http://localhost:8080/actuator/health

# Expected output:
# {"status":"UP"}
```

## 5. Generate JWT Token (Testing)

### Enable Test Endpoint

Trong `api-gateway/src/main/resources/application.yml`:
```yaml
gateway:
  test:
    enabled: true  # Đổi từ false → true
```

Restart gateway sau khi thay đổi.

### Generate Token

```bash
curl -X POST http://localhost:8080/public/generate-token \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "123e4567-e89b-12d3-a456-426614174000",
    "username": "testuser",
    "fullname": "Test User"
  }'
```

**Lưu token trả về để dùng cho các requests sau.**

## 6. Test API Routing với JWT

### Test 1: Request với Valid Token

```bash
# Thay <YOUR_TOKEN> bằng token từ bước 5
curl -X GET http://localhost:8080/api/orders/123 \
  -H "Authorization: Bearer <YOUR_TOKEN>" \
  -v
```

**Expected behavior:**
- Gateway validate token thành công
- Headers được inject: `X-User-Id`, `X-User-Name`, `X-User-Fullname`
- Request được forward đến order-service (8082)

### Test 2: Request không có Authorization Header

```bash
curl -X GET http://localhost:8080/api/orders/123
```

**Expected:**
```json
{
  "timestamp": "2024-05-20T10:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Authorization header is required"
}
```

### Test 3: Request với Invalid Token

```bash
curl -X GET http://localhost:8080/api/orders/123 \
  -H "Authorization: Bearer invalid.token.here"
```

**Expected:**
```json
{
  "timestamp": "2024-05-20T10:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid or expired JWT token"
}
```

## 7. Kiểm tra Logs

### Gateway Logs

```bash
# Xem logs trong console hoặc file
tail -f api-gateway/logs/api-gateway.log
```

**Expected logs khi có request:**
```
2024-05-20 10:00:00.000 DEBUG --- INCOMING REQUEST ===
2024-05-20 10:00:00.001 DEBUG --- Method: GET
2024-05-20 10:00:00.002 DEBUG --- Path: /api/orders/123
2024-05-20 10:00:00.003 INFO  --- Authenticated user - ID: 123e4567..., Username: testuser
2024-05-20 10:00:00.004 DEBUG --- Injected headers - X-User-Id: 123e4567...
2024-05-20 10:00:00.005 DEBUG === OUTGOING RESPONSE ===
2024-05-20 10:00:00.006 DEBUG --- Status: 200 OK
2024-05-20 10:00:00.007 DEBUG --- Duration: 45 ms
```

## 8. Test với Postman hoặc Thunder Client

### Cấu hình Request

1. **Method**: GET
2. **URL**: `http://localhost:8080/api/orders/123`
3. **Headers**:
   - `Authorization`: `Bearer <YOUR_TOKEN>`

### Test Cases

| Test Case | Authorization Header | Expected Status |
|-----------|---------------------|-----------------|
| Valid Token | `Bearer <valid-token>` | 200 OK (hoặc 404 nếu order không tồn tại) |
| No Token | (không có header) | 401 Unauthorized |
| Invalid Format | `invalid-token` (không có "Bearer ") | 401 Unauthorized |
| Expired Token | `Bearer <expired-token>` | 401 Unauthorized |

## 9. Docker Quick Start

### Build và chạy với Docker Compose

```bash
# Từ thư mục gốc microservice
cd d:/1.Project/microservice

# Build và start tất cả services
docker-compose up --build

# Hoặc run trong background
docker-compose up -d --build
```

### Test với Docker

```bash
# Gateway health check
curl http://localhost:8080/actuator/health

# Generate token
curl -X POST http://localhost:8080/public/generate-token \
  -H "Content-Type: application/json" \
  -d '{"userId": "123", "username": "dockeruser", "fullname": "Docker User"}'

# Test API
curl -X GET http://localhost:8080/api/orders/123 \
  -H "Authorization: Bearer <TOKEN>"
```

### Stop Docker services

```bash
docker-compose down
```

## 10. Xác nhận Gateway hoạt động đúng

### Checklist

- [ ] Gateway start thành công trên port 8080
- [ ] Health check trả về `{"status":"UP"}`
- [ ] Generate token endpoint hoạt động
- [ ] Request với valid token được forward đến destination service
- [ ] Request không có token bị từ chối (401)
- [ ] Headers `X-User-Id`, `X-User-Name`, `X-User-Fullname` được inject
- [ ] Logs hiển thị authentication info

## 11. Troubleshooting

### Gateway không start được

**Symptom**: Port 8080 đã được sử dụng

**Solution**:
```bash
# Tìm process đang dùng port 8080
netstat -ano | findstr :8080

# Kill process đó (Windows)
taskkill /PID <PID> /F

# Hoặc đổi port trong application.yml
server:
  port: 9080  # Dùng port khác
```

### JWT Validation failed

**Symptom**: Token valid nhưng vẫn bị reject

**Checklist**:
1. Secret key trong Gateway match với secret key dùng generate token?
2. Token chưa expired?
3. Token format đúng? (`Bearer <token>`)

### Destination service không available

**Symptom**: 503 Service Unavailable

**Solution**:
```bash
# Kiểm tra service destination có running không
curl http://localhost:8082/actuator/health  # Order service

# Nếu không running, start service đó
cd order-service
mvn spring-boot:run
```

### Test endpoint không hoạt động

**Symptom**: 404 khi gọi `/public/generate-token`

**Solution**:
```yaml
# Trong application.yml
gateway:
  test:
    enabled: true  # Đảm bảo là true
```

Restart gateway sau khi thay đổi.

## 12. Next Steps

Sau khi Gateway đã hoạt động:

1. **Remove JWT logic từ services**: Xóa `JwtService` và `SecurityConfig` khỏi các services
2. **Update service controllers**: Đọc user info từ headers thay vì JWT
3. **Update client URLs**: Đổi base URLs để trỏ về gateway
4. **Monitor và tune logs**: Adjust logging levels cho production
5. **Add more filters**: Rate limiting, caching, etc.

## 📚 Additional Resources

- [API Gateway README](./api-gateway/README.md)
- [Architecture Documentation](./ARCHITECTURE.md)
- [Spring Cloud Gateway Docs](https://spring.io/projects/spring-cloud-gateway)
