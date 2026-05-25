# Microservice URL Restructuring Guide

## Overview
Đã tái cấu trúc URL cho tất cả microservices theo kiến trúc ngắn gọn, dễ đọc, không versioning.

## ✅ Completed Changes

### 1. API Gateway Configuration
**File**: [api-gateway/src/main/resources/application.yml](d:\1.Project\microservice\api-gateway\src\main\resources\application.yml)

**Gateway Routes:**
```yaml
routes:
  # Auth Service → /auth/**
  - id: auth-service
    uri: http://localhost:8084
    predicates:
      - Path=/auth/**
    filters:
      - StripPrefix=1

  # CMS Service → /cms/**
  - id: cms-service
    uri: http://localhost:8081
    predicates:
      - Path=/cms/**
    filters:
      - StripPrefix=1
      - AuthorizeFilter

  # Order Service → /orders/**
  - id: order-service
    uri: http://localhost:8082
    predicates:
      - Path=/orders/**
    filters:
      - StripPrefix=1
      - AuthorizeFilter

  # Payment Service → /payments/**
  - id: payment-service
    uri: http://localhost:8083
    predicates:
      - Path=/payments/**
    filters:
      - StripPrefix=1
      - AuthorizeFilter
```

**Excluded Paths (No Auth Required):**
```yaml
excluded-paths:
  - /health
  - /actuator/**
  - /public/**
  - /auth/login
  - /auth/register
  - /auth/refresh-token
  - /auth/actuator/**
  - /auth/swagger-ui/**
  - /auth/api-docs/**
```

### 2. Auth Service Configuration
**File**: [auth-service/src/main/resources/application.yml](d:\1.Project\microservice\auth-service\src\main\resources\application.yml)

**Changes:**
- ❌ Removed: `context-path: /auth-service`
- ✅ Context path: `/` (root)

**Controllers Updated:**
- `AuthController`: `/auth/**` (login, register, refresh-token, logout)
- `UserController`: `/users/**`
- `AuthorityController`: `/authorities/**`
- `GroupController`: `/groups/**`
- `ParameterController`: `/parameters/**`

### 3. CMS Service Configuration
**File**: [cms-service/src/main/resources/application.yml](d:\1.Project\microservice\cms-service\src\main\resources\application.yml)

**Changes:**
- ❌ Removed: `context-path: /cms-service`
- ✅ Context path: `/` (root)

### 4. Order Service Configuration
**File**: [order-service/src/main/resources/application.yml](d:\1.Project\microservice\order-service\src\main\resources\application.yml)

**Changes:**
- ❌ Removed: `context-path: /order-service`
- ✅ Context path: `/` (root)
- ✅ Controller: `OrderController`: `/orders/**`

### 5. Payment Service Configuration
**File**: [payment-service/src/main/resources/application.yml](d:\1.Project\microservice\payment-service\src\main\resources/application.yml)

**Changes:**
- ❌ Removed: `context-path: /payment-service`
- ✅ Context path: `/` (root)
- ✅ Controller: `PaymentController`: `/payments/**`

## 📋 URL Mapping Reference

### Before (Old Structure)
```
http://localhost:8080/api/auth/v1/auth/login
http://localhost:8080/api/auth/v1/auth/register
http://localhost:8080/api/auth/v1/users
http://localhost:8080/api/cms/v1/reports/sales
http://localhost:8080/api/orders/v1/orders
http://localhost:8080/api/payments/v1/payments
```

### After (New Structure)
```
http://localhost:8080/auth/login
http://localhost:8080/auth/register
http://localhost:8080/auth/users
http://localhost:8080/cms/sales-report
http://localhost:8080/orders
http://localhost:8080/payments
```

## 🎯 Complete API Endpoints

### Authentication APIs (Port 8084 → Gateway 8080)
```bash
# Public endpoints (no auth required)
POST   http://localhost:8080/auth/login
POST   http://localhost:8080/auth/register
POST   http://localhost:8080/auth/refresh-token

# Protected endpoints (require JWT)
GET    http://localhost:8080/auth/me
POST   http://localhost:8080/auth/change-password
POST   http://localhost:8080/auth/logout
```

### User Management APIs
```bash
GET    http://localhost:8080/auth/users
GET    http://localhost:8080/auth/users/{id}
GET    http://localhost:8080/auth/users/search?keyword=john
GET    http://localhost:8080/auth/users/type/1
POST   http://localhost:8080/auth/users
PUT    http://localhost:8080/auth/users/{id}
PUT    http://localhost:8080/auth/users/{id}/lock
PUT    http://localhost:8080/auth/users/{id}/unlock
DELETE http://localhost:8080/auth/users/{id}
```

### Authority Management APIs
```bash
GET    http://localhost:8080/auth/authorities
GET    http://localhost:8080/auth/authorities/{id}
GET    http://localhost:8080/auth/authorities/search?keyword=user
POST   http://localhost:8080/auth/authorities
PUT    http://localhost:8080/auth/authorities/{id}
DELETE http://localhost:8080/auth/authorities/{id}
```

### Group Management APIs
```bash
GET    http://localhost:8080/auth/groups
GET    http://localhost:8080/auth/groups/{id}
GET    http://localhost:8080/auth/groups/search?keyword=admin
POST   http://localhost:8080/auth/groups
PUT    http://localhost:8080/auth/groups/{id}
DELETE http://localhost:8080/auth/groups/{id}
POST   http://localhost:8080/auth/groups/{id}/authorities
DELETE http://localhost:8080/auth/groups/{id}/authorities/{authorityId}
GET    http://localhost:8080/auth/groups/{id}/authorities
POST   http://localhost:8080/auth/groups/{id}/users
DELETE http://localhost:8080/auth/groups/{id}/users/{userId}
GET    http://localhost:8080/auth/groups/{id}/users
```

### Parameter Management APIs
```bash
GET    http://localhost:8080/auth/parameters
GET    http://localhost:8080/auth/parameters/{id}
GET    http://localhost:8080/auth/parameters/key/{key}
GET    http://localhost:8080/auth/parameters/value/{key}
GET    http://localhost:8080/auth/parameters/search?keyword=config
GET    http://localhost:8080/auth/parameters/active
POST   http://localhost:8080/auth/parameters
PUT    http://localhost:8080/auth/parameters/{id}
DELETE http://localhost:8080/auth/parameters/{id}
```

### Order Management APIs (Port 8082)
```bash
GET    http://localhost:8080/orders
GET    http://localhost:8080/orders/{id}
POST   http://localhost:8080/orders
```

### Payment APIs (Port 8083)
```bash
GET    http://localhost:8080/payments
GET    http://localhost:8080/payments/{id}
GET    http://localhost:8080/payments/health
```

## 🔧 Technical Details

### Gateway Routing Logic
```
Client Request: /auth/login
       ↓
Gateway matches: /auth/**
       ↓
StripPrefix=1: /login
       ↓
Forward to: http://localhost:8084/login
       ↓
Auth Service processes: @RequestMapping("/auth") → /auth/login
```

### Service Configuration
| Service  | Port  | Context Path | Base URL      |
|----------|------|--------------|----------------|
| Gateway  | 8080 | -            | http://localhost:8080 |
| Auth     | 8084 | `/`          | http://localhost:8084 |
| CMS      | 8081 | `/`          | http://localhost:8081 |
| Order    | 8082 | `/`          | http://localhost:8082 |
| Payment  | 8083 | `/`          | http://localhost:8083 |

## 🚀 Benefits

### 1. **Simplicity**
- URLs ngắn hơn: `/auth/login` thay vì `/api/auth/v1/auth/login`
- Dễ đọc, dễ nhớ
- Phản ánh rõ cấu trúc microservices

### 2. **Clarity**
- Từ đầu URL là tên service: `auth`, `cms`, `orders`, `payments`
- Client biết ngay đang gọi service nào
- Dễ debug và trace

### 3. **Gateway-Centric**
- Tất cả traffic đi qua Gateway
- Single entry point: `localhost:8080`
- Centralized auth, CORS, logging

### 4. **No Version Lock-in**
- Không lock vào `/v1/` trong URL
- Dễ upgrade hoặc deprecate APIs
- Gateway có thể handle versioning nếu cần

## 📝 Usage Examples

### curl Examples
```bash
# Login
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456aA@"}'

# Register
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newuser",
    "password": "Password123!",
    "confirmPassword": "Password123!",
    "fullname": "New User",
    "mobile": "0123456789"
  }'

# Get Users (with JWT token)
curl -X GET http://localhost:8080/auth/users \
  -H "Authorization: Bearer <your_jwt_token>"

# Create Order
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your_jwt_token>" \
  -d '{"customerId": "123", "items": [...]}'

# Get Order
curl -X GET http://localhost:8080/orders/123 \
  -H "Authorization: Bearer <your_jwt_token>"
```

### JavaScript/Fetch Examples
```javascript
// Login
const response = await fetch('http://localhost:8080/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    username: 'admin',
    password: '123456aA@'
  })
});

// Get Users
const users = await fetch('http://localhost:8080/auth/users', {
  headers: {
    'Authorization': `Bearer ${token}`
  }
});

// Create Order
const order = await fetch('http://localhost:8080/orders', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`
  },
  body: JSON.stringify({
    customerId: '123',
    items: [...]
  })
});
```

## ⚠️ Migration Notes

### Breaking Changes
❌ **Old URLs no longer work:**
- `/api/auth/v1/auth/login` → **404 Not Found**
- `/api/auth/v1/users` → **404 Not Found**
- `/api/cms/v1/reports` → **404 Not Found**
- `/api/orders/v1/orders` → **404 Not Found**

✅ **New URLs:**
- `/auth/login`
- `/auth/users`
- `/cms/reports`
- `/orders`

### Client Updates Required
All client applications (mobile apps, web apps, third-party integrations) **PHẢI** cập nhật URLs.

### Migration Checklist
- [ ] Update frontend API calls
- [ ] Update mobile app API calls
- [ ] Update API documentation
- [ ] Update third-party integrations
- [ ] Update automated tests
- [ ] Update API gateway configuration
- [ ] Update monitoring/tracing systems

## 🎓 Best Practices

### URL Design Principles
1. **Nouns over verbs**: `/users` thay vì `/getUsers`
2. **Plural for collections**: `/users`, `/orders`
3. **Lowercase**: `/auth/login` thay vì `/Auth/Login`
4. **Hyphens for multi-word**: `/sales-report` thay vì `/salesReport`
5. **No file extensions**: `/users` thay vì `/users.json`

### HTTP Methods
- `GET` - Retrieve resources
- `POST` - Create new resources
- `PUT` - Update existing resources
- `DELETE` - Remove resources
- `PATCH` - Partial updates

### Status Codes
- `200` - Success
- `201` - Created
- `400` - Bad Request
- `401` - Unauthorized
- `403` - Forbidden
- `404` - Not Found
- `500` - Internal Server Error

## 📚 Additional Resources

- **API Gateway**: [Spring Cloud Gateway Docs](https://spring.io/projects/spring-cloud-gateway)
- **Microservices**: [Spring Boot Microservices Guide](https://spring.io/guides/microservices/)
- **REST API Design**: [REST API Best Practices](https://restfulapi.net/)

---

**Last Updated**: 2025-05-25
**Version**: 2.0.0
**Status**: ✅ Completed and Compiled Successfully
