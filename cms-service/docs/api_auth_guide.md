# Hướng Dẫn API Xác Thực

Tài liệu này hướng dẫn cách sử dụng các API xác thực, bao gồm đăng nhập, đăng ký và thay đổi mật khẩu.

## 📋 Table of Contents

- [Đăng Ký Tài Khoản](#đăng-ký-tài-khoản)
- [Đăng Nhập](#đăng-nhập)
- [Thay Đổi Mật Khẩu](#thay-đổi-mật-khẩu)
- [Lấy Thông Tin User Hiện Tại](#lấy-thông-tin-user-hiện-tại)

---

## 🔑 Base URL

```
http://localhost:8081/cms-service/v1/auth
```

---

## 1. Đăng Ký Tài Khoản

Tạo tài khoản người dùng mới.

### Endpoint

```http
POST /v1/auth/register
```

### Request Body

```json
{
  "username": "john_doe",
  "password": "SecurePass123!",
  "fullname": "John Doe",
  "type": 1,
  "mobile": "0901234567",
  "address": "123 Đường ABC, Quận 1, TP.HCM"
}
```

### Các Trường Dữ Liệu

| Trường | Kiểu | Bắt Buộc | Mô Tả |
|-------|------|-----------|---------|
| username | String | ✅ | Tên đăng nhập (duy nhất) |
| password | String | ✅ | Mật khẩu (tối thiểu 6 ký tự) |
| fullname | String | ✅ | Họ tên đầy đủ |
| type | Integer | ❌ | Loại user (0: ADMIN, 1: CUSTOMER), mặc định: 1 |
| mobile | String | ❌ | Số điện thoại |
| address | String | ❌ | Địa chỉ |

### Response Example

**Thành công (201):**
```json
{
  "code": 200,
  "message": "Registration successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "username": "john_doe",
      "fullname": "John Doe",
      "status": "ACTIVE",
      "type": "CUSTOMER",
      "mobile": "0901234567",
      "address": "123 Đường ABC, Quận 1, TP.HCM",
      "createdDate": "2026-05-19T14:00:00",
      "updatedDate": "2026-05-19T14:00:00"
    }
  }
}
```

### Lỗi Thường Gặp

| Mã | Thông Báo | Nguyên Nhân |
|----|----------|------------|
| 400 | Username already exists | Tên đăng nhập đã được sử dụng |
| 400 | Validation failed | Dữ liệu không hợp lệ |

---

## 2. Đăng Nhập

Xác thực người dùng và nhận JWT token.

### Endpoint

```http
POST /v1/auth/login
```

### Request Body

```json
{
  "username": "john_doe",
  "password": "SecurePass123!"
}
```

### Response Example

**Thành công (200):**
```json
{
  "code": 200,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "username": "john_doe",
      "fullname": "John Doe",
      "status": "ACTIVE",
      "type": "CUSTOMER",
      "mobile": "0901234567",
      "address": "123 Đường ABC, Quận 1, TP.HCM"
    }
  }
}
```

### Sử Dụng Token

Sau khi có token, thêm vào header cho các request tiếp theo:

```bash
curl -X GET http://localhost:8081/cms-service/v1/users \
  -H "Authorization: Bearer <token>"
```

### Lỗi Thường Gặp

| Mã | Thông Báo | Nguyên Nhân |
|----|----------|------------|
| 401 | Invalid username or password | Sai tên đăng nhập hoặc mật khẩu |
| 401 | Account is locked | Tài khoản đã bị khóa |
| 401 | Account has been deleted | Tài khoản đã bị xóa |

---

## 3. Thay Đổi Mật Khẩu

Thay đổi mật khẩu cho user đang đăng nhập.

### Endpoint

```http
POST /v1/auth/change-password
```

### Headers

```http
Authorization: Bearer <jwt-token>
Content-Type: application/json
```

### Request Body

```json
{
  "currentPassword": "SecurePass123!",
  "newPassword": "NewSecurePass456!"
}
```

### Các Trường Dữ Liệu

| Trường | Kiểu | Bắt Buộc | Mô Tả |
|-------|------|-----------|---------|
| currentPassword | String | ✅ | Mật khẩu hiện tại |
| newPassword | String | ✅ | Mật khẩu mới (khác mật khẩu hiện tại) |

### Response Example

**Thành công (200):**
```json
{
  "code": 200,
  "message": "Password changed successfully",
  "data": null
}
```

### Lỗi Thường Gặp

| Mã | Thông Báo | Nguyên Nhân |
|----|----------|------------|
| 400 | Current password is incorrect | Mật khẩu hiện tại không đúng |
| 400 | New password must be different | Mật khẩu mới phải khác mật khẩu cũ |
| 401 | Unauthorized | Chưa đăng nhập hoặc token hết hạn |

---

## 4. Lấy Thông Tin User Hiện Tại

Lấy thông tin của user đang đăng nhập.

### Endpoint

```http
GET /v1/auth/me
```

### Headers

```http
Authorization: Bearer <jwt-token>
```

### Response Example

**Thành công (200):**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "username": "john_doe",
    "fullname": "John Doe",
    "status": "ACTIVE",
    "type": "CUSTOMER",
    "mobile": "0901234567",
    "address": "123 Đường ABC, Quận 1, TP.HCM",
    "createdDate": "2026-05-19T14:00:00",
    "updatedDate": "2026-05-19T14:00:00"
  }
}
```

---

## 🔒 Bảo Mật

1. **Luôn sử dụng HTTPS** trong môi trường sản xuất
2. **Lưu trữ JWT token an toàn** - không lưu trong localStorage nếu không cần thiết
3. **Token hết hạn** sau 1 giờ, cần refresh token để tiếp tục sử dụng
4. **Đăng xuất** bằng cách xóa token khỏi client (không có endpoint logout)

## 📝 Ví Dụ cURL

```bash
# Đăng ký
curl -X POST http://localhost:8081/cms-service/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "SecurePass123!",
    "fullname": "John Doe",
    "type": 1
  }'

# Đăng nhập
curl -X POST http://localhost:8081/cms-service/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "SecurePass123!"
  }'

# Thay đổi mật khẩu
curl -X POST http://localhost:8081/cms-service/v1/auth/change-password \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "currentPassword": "SecurePass123!",
    "newPassword": "NewSecurePass456!"
  }'

# Lấy thông tin user hiện tại
curl -X GET http://localhost:8081/cms-service/v1/auth/me \
  -H "Authorization: Bearer <token>"
```
