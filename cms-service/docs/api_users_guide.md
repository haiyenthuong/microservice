# Hướng Dẫn API Quản Lý Người Dùng

Tài liệu này hướng dẫn cách sử dụng các API quản lý người dùng, bao gồm tạo, cập nhật, khóa/mở khóa và xóa tài khoản.

## 📋 Table of Contents

- [Danh Sách Người Dùng](#danh-sách-người-dùng)
- [Chi Tiết Người Dùng](#chi-tiết-người-dùng)
- [Tìm Kiếm Người Dùng](#tìm-kiếm-người-dùng)
- [Người Dùng Theo Loại](#người-dùng-theo-loại)
- [Tạo Người Dùng Mới](#tạo-người-dùng-mới)
- [Cập Nhật Người Dùng](#cập-nhật-người-dùng)
- [Khóa Tài Khoản](#khóa-tài-khoản)
- [Mở Khóa Tài Khoản](#mở-khóa-tài-khoản)
- [Xóa Tài Khoản](#xóa-tài-khoản)

---

## 🔑 Base URL

```
http://localhost:8081/cms-service/v1/users
```

---

## ⚠️ Yêu Cầu Xác Thực

Tất cả endpoints yêu cầu JWT token:

```http
Authorization: Bearer <jwt-token>
```

---

## 1. Danh Sách Người Dùng

Lấy danh sách tất cả người dùng.

### Endpoint

```http
GET /v1/users
```

### Response Example

```json
{
  "code": 200,
  "message": "Success",
  "data": [
    {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "username": "john_doe",
      "fullname": "John Doe",
      "status": "ACTIVE",
      "type": "CUSTOMER",
      "mobile": "0901234567",
      "address": "123 Đường ABC, Quận 1, TP.HCM",
      "createdDate": "2026-05-19T14:00:00",
      "updatedDate": "2026-05-19T14:00:00"
    },
    {
      "id": "223e4567-e89b-12d3-a456-426614174001",
      "username": "admin_user",
      "fullname": "Admin User",
      "status": "ACTIVE",
      "type": "ADMIN",
      "mobile": "0909876543",
      "address": "456 Đường XYZ, Quận 2, TP.HCM",
      "createdDate": "2026-05-19T15:00:00",
      "updatedDate": "2026-05-19T15:00:00"
    }
  ]
}
```

### Trạng Thái User

| Trạng Thái | Mô Tả |
|----------|---------|
| ACTIVE | Đang hoạt động |
| LOCKED | Đã bị khóa |
| DELETED | Đã bị xóa |

---

## 2. Chi Tiết Người Dùng

Lấy thông tin chi tiết một người dùng theo ID.

### Endpoint

```http
GET /v1/users/{id}
```

### Path Parameters

| Tham Số | Kiểu | Mô Tả |
|---------|------|---------|
| id | String | UUID của user |

### Response Example

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

### Lỗi Thường Gặp

| Mã | Thông Báo | Nguyên Nhân |
|----|----------|------------|
| 404 | User not found | Không tìm thấy user với ID |

---

## 3. Tìm Kiếm Người Dùng

Tìm kiếm user theo username hoặc fullname.

### Endpoint

```http
GET /v1/users/search?keyword={keyword}
```

### Query Parameters

| Tham Số | Kiểu | Mô Tả |
|---------|------|---------|
| keyword | String | Từ khóa tìm kiếm (username hoặc fullname) |

### Response Example

```json
{
  "code": 200,
  "message": "Success",
  "data": [
    {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "username": "john_doe",
      "fullname": "John Doe",
      "status": "ACTIVE",
      "type": "CUSTOMER"
    }
  ]
}
```

---

## 4. Người Dùng Theo Loại

Lấy danh sách user theo loại (ADMIN hoặc CUSTOMER).

### Endpoint

```http
GET /v1/users/type/{type}
```

### Path Parameters

| Tham Số | Kiểu | Mô Tả |
|---------|------|---------|
| type | Integer | Loại user (0: ADMIN, 1: CUSTOMER) |

### Response Example

```json
{
  "code": 200,
  "message": "Success",
  "data": [
    {
      "id": "223e4567-e89b-12d3-a456-426614174001",
      "username": "admin_user",
      "fullname": "Admin User",
      "status": "ACTIVE",
      "type": "ADMIN"
    }
  ]
}
```

---

## 5. Tạo Người Dùng Mới

Tạo một user mới (chỉ dành ADMIN).

### Endpoint

```http
POST /v1/users
```

### Request Body

```json
{
  "username": "new_user",
  "password": "SecurePass123!",
  "fullname": "New User",
  "type": 1,
  "mobile": "0901234567",
  "address": "789 Đường DEF, Quận 3, TP.HCM"
}
```

### Các Trường Dữ Liệu

| Trường | Kiểu | Bắt Buộc | Mô Tả |
|-------|------|-----------|---------|
| username | String | ✅ | Tên đăng nhập (duy nhất, 3-50 ký tự) |
| password | String | ✅ | Mật khẩu (tối thiểu 6 ký tự) |
| fullname | String | ✅ | Họ tên đầy đủ |
| type | Integer | ❌ | Loại user (0: ADMIN, 1: CUSTOMER), mặc định: 1 |
| mobile | String | ❌ | Số điện thoại |
| address | String | ❌ | Địa chỉ |

### Response Example

**Thành công (200):**
```json
{
  "code": 200,
  "message": "User created successfully",
  "data": {
    "id": "323e4567-e89b-12d3-a456-426614174002",
    "username": "new_user",
    "fullname": "New User",
    "status": "ACTIVE",
    "type": "CUSTOMER",
    "mobile": "0901234567",
    "address": "789 Đường DEF, Quận 3, TP.HCM",
    "createdDate": "2026-05-19T16:00:00",
    "updatedDate": "2026-05-19T16:00:00"
  }
}
```

### Lỗi Thường Gặp

| Mã | Thông Báo | Nguyên Nhân |
|----|----------|------------|
| 400 | Username already exists | Tên đăng nhập đã tồn tại |
| 400 | Validation failed | Dữ liệu không hợp lệ |

---

## 6. Cập Nhật Người Dùng

Cập nhật thông tin user.

### Endpoint

```http
PUT /v1/users/{id}
```

### Request Body

```json
{
  "fullname": "John Updated",
  "mobile": "0909999999",
  "address": "999 Đường Mới, Quận 5, TP.HCM"
}
```

### Các Trường Dữ Liệu (Tất cả đều tùy chọn)

| Trường | Kiểu | Mô Tả |
|-------|------|---------|
| fullname | String | Họ tên đầy đủ |
| mobile | String | Số điện thoại |
| address | String | Địa chỉ |

### Response Example

```json
{
  "code": 200,
  "message": "User updated successfully",
  "data": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "username": "john_doe",
    "fullname": "John Updated",
    "status": "ACTIVE",
    "type": "CUSTOMER",
    "mobile": "0909999999",
    "address": "999 Đường Mới, Quận 5, TP.HCM",
    "updatedDate": "2026-05-19T16:30:00"
  }
}
```

---

## 7. Khóa Tài Khoản

Khóa tài khoản người dùng (ngăn chặn đăng nhập).

### Endpoint

```http
POST /v1/users/{id}/lock
```

### Response Example

```json
{
  "code": 200,
  "message": "User locked successfully",
  "data": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "username": "john_doe",
    "fullname": "John Doe",
    "status": "LOCKED",
    "type": "CUSTOMER",
    "updatedDate": "2026-05-19T16:35:00"
  }
}
```

### Lỗi Thường Gặp

| Mã | Thông Báo | Nguyên Nhân |
|----|----------|------------|
| 400 | User is already locked | User đã bị khóa từ trước |
| 404 | User not found | Không tìm thấy user |

---

## 8. Mở Khóa Tài Khoản

Mở khóa tài khoản đã bị khóa.

### Endpoint

```http
POST /v1/users/{id}/unlock
```

### Response Example

```json
{
  "code": 200,
  "message": "User unlocked successfully",
  "data": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "username": "john_doe",
    "fullname": "John Doe",
    "status": "ACTIVE",
    "type": "CUSTOMER",
    "updatedDate": "2026-05-19T16:40:00"
  }
}
```

### Lỗi Thường Gặp

| Mã | Thông Báo | Nguyên Nhân |
|----|----------|------------|
| 400 | User is already active | User đang ở trạng thái hoạt động |
| 404 | User not found | Không tìm thấy user |

---

## 9. Xóa Tài Khoản

Xóa mềm tài khoản người dùng (soft delete).

### Endpoint

```http
DELETE /v1/users/{id}
```

### Response Example

```json
{
  "code": 200,
  "message": "User deleted successfully",
  "data": null
}
```

### Lỗi Thường Gặp

| Mã | Thông Báo | Nguyên Nhân |
|----|----------|------------|
| 400 | User is already deleted | User đã bị xóa từ trước |
| 404 | User not found | Không tìm thấy user |

---

## 📝 Ví Dụ cURL

```bash
# Lấy danh sách users
curl -X GET http://localhost:8081/cms-service/v1/users \
  -H "Authorization: Bearer <token>"

# Tìm kiếm user
curl -X GET "http://localhost:8081/cms-service/v1/users/search?keyword=john" \
  -H "Authorization: Bearer <token>"

# Tạo user mới
curl -X POST http://localhost:8081/cms-service/v1/users \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "new_user",
    "password": "SecurePass123!",
    "fullname": "New User",
    "type": 1
  }'

# Cập nhật user
curl -X PUT http://localhost:8081/cms-service/v1/users/123e4567-e89b-12d3-a456-426614174000 \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "fullname": "John Updated",
    "mobile": "0909999999"
  }'

# Khóa user
curl -X POST http://localhost:8081/cms-service/v1/users/123e4567-e89b-12d3-a456-426614174000/lock \
  -H "Authorization: Bearer <token>"

# Mở khóa user
curl -X POST http://localhost:8081/cms-service/v1/users/123e4567-e89b-12d3-a456-426614174000/unlock \
  -H "Authorization: Bearer <token>"

# Xóa user
curl -X DELETE http://localhost:8081/cms-service/v1/users/123e4567-e89b-12d3-a456-426614174000 \
  -H "Authorization: Bearer <token>"
```

---

## ⚠️ Lưu Ý

1. **Quyền hạn**: Chỉ ADMIN mới có quyền tạo, sửa, xóa user
2. **Xóa mềm**: User bị xóa vẫn còn trong database (soft delete), chỉ không thể đăng nhập
3. **Khóa user**: User bị khóa vẫn có thể xem được thông tin nhưng không thể đăng nhập
4. **Username**: Username sau khi tạo không thể thay đổi
