# Hướng Dẫn API Quản Lý Tham Số Hệ Thống

Tài liệu này hướng dẫn cách sử dụng các API quản lý tham số hệ thống (system parameters).

## 📋 Table of Contents

- [Danh Sách Tham Số](#danh-sách-tham-số)
- [Chi Tiết Tham Số](#chi-tiết-tham-số)
- [Tạo Tham Số Mới](#tạo-tham-số-mới)
- [Cập Nhật Tham Số](#cập-nhật-tham-số)
- [Xóa Tham Số](#xóa-tham-số)

---

## 🔑 Base URL

```
http://localhost:8081/cms-service/v1/parameters
```

---

## ⚠️ Yêu Cầu Xác Thực

Tất cả endpoints yêu cầu JWT token:

```http
Authorization: Bearer <jwt-token>
```

---

## 1. Danh Sách Tham Số

Lấy danh sách tất cả tham số hệ thống.

### Endpoint

```http
GET /v1/parameters
```

### Response Example

```json
{
  "code": 200,
  "message": "Success",
  "data": [
    {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "paramKey": "system.max_upload_size",
      "paramValue": "10485760",
      "paramName": "Kích thước upload tối đa",
      "description": "Kích thước file tối đa cho phép upload (bytes)",
      "status": 1,
      "createdBy": "admin",
      "updatedBy": "admin",
      "createdDate": "2026-05-19T10:00:00",
      "updatedDate": "2026-05-19T10:00:00"
    },
    {
      "id": "223e4567-e89b-12d3-a456-426614174001",
      "paramKey": "system.session_timeout",
      "paramValue": "3600",
      "paramName": "Thời gian session hết hạn",
      "description": "Thời gian session timeout (giây)",
      "status": 1,
      "createdBy": "admin",
      "updatedBy": null,
      "createdDate": "2026-05-19T10:00:00",
      "updatedDate": "2026-05-19T10:00:00"
    }
  ]
}
```

### Trạng Thái Tham Số

| Trạng Thái | Mô Tả |
|----------|---------|
| 1 | Hoạt động |
| 0 | Không hoạt động |

---

## 2. Chi Tiết Tham Số

Lấy thông tin chi tiết một tham số theo ID.

### Endpoint

```http
GET /v1/parameters/{id}
```

### Path Parameters

| Tham Số | Kiểu | Mô Tả |
|---------|------|---------|
| id | String | UUID của tham số |

### Response Example

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "paramKey": "system.max_upload_size",
    "paramValue": "10485760",
    "paramName": "Kích thước upload tối đa",
    "description": "Kích thước file tối đa cho phép upload (bytes)",
    "status": 1,
    "createdBy": "admin",
    "updatedBy": "admin",
    "createdDate": "2026-05-19T10:00:00",
    "updatedDate": "2026-05-19T10:00:00"
  }
}
```

### Lỗi Thường Gặp

| Mã | Thông Báo | Nguyên Nhân |
|----|----------|------------|
| 404 | Parameter not found | Không tìm thấy tham số |

---

## 3. Tạo Tham Số Mới

Tạo một tham số hệ thống mới (chỉ ADMIN).

### Endpoint

```http
POST /v1/parameters
```

### Request Body

```json
{
  "paramKey": "system.max_login_attempts",
  "paramValue": "5",
  "paramName": "Số lần đăng nhập sai tối đa",
  "description": "Số lần đăng nhập sai trước khi khóa tài khoản",
  "status": 1
}
```

### Các Trường Dữ Liệu

| Trường | Kiểu | Bắt Buộc | Mô Tả |
|-------|------|-----------|---------|
| paramKey | String | ✅ | Key tham số (duy nhất, tối đa 100 ký tự) |
| paramValue | String | ✅ | Giá trị tham số (tối đa 2000 ký tự) |
| paramName | String | ✅ | Tên hiển thị (tối đa 200 ký tự) |
| description | String | ❌ | Mô tả (tối đa 500 ký tự) |
| status | Integer | ❌ | Trạng thái (1: hoạt động, 0: không hoạt động), mặc định: 1 |

### Response Example

**Thành công (200):**
```json
{
  "code": 200,
  "message": "Parameter created successfully",
  "data": {
    "id": "323e4567-e89b-12d3-a456-426614174003",
    "paramKey": "system.max_login_attempts",
    "paramValue": "5",
    "paramName": "Số lần đăng nhập sai tối đa",
    "description": "Số lần đăng nhập sai trước khi khóa tài khoản",
    "status": 1,
    "createdBy": "admin",
    "updatedBy": null,
    "createdDate": "2026-05-19T17:00:00",
    "updatedDate": "2026-05-19T17:00:00"
  }
}
```

### Lỗi Thường Gặp

| Mã | Thông Báo | Nguyên Nhân |
|----|----------|------------|
| 400 | Parameter key already exists | Key tham số đã tồn tại |
| 400 | Validation failed | Dữ liệu không hợp lệ |

---

## 4. Cập Nhật Tham Số

Cập nhật thông tin tham số.

### Endpoint

```http
PUT /v1/parameters/{id}
```

### Request Body

```json
{
  "paramValue": "10",
  "paramName": "Số lần đăng nhập sai tối đa (đã cập nhật)",
  "description": "Mô tả mới",
  "status": 1
}
```

### Các Trường Dữ Liệu (Tất cả đều tùy chọn)

| Trường | Kiểu | Mô Tả |
|-------|------|---------|
| paramValue | String | Giá trị tham số mới |
| paramName | String | Tên hiển thị mới |
| description | String | Mô tả mới |
| status | Integer | Trạng thái (1: hoạt động, 0: không hoạt động) |

### Response Example

```json
{
  "code": 200,
  "message": "Parameter updated successfully",
  "data": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "paramKey": "system.max_login_attempts",
    "paramValue": "10",
    "paramName": "Số lần đăng nhập sai tối đa (đã cập nhật)",
    "description": "Mô tả mới",
    "status": 1,
    "createdBy": "admin",
    "updatedBy": "admin",
    "createdDate": "2026-05-19T17:00:00",
    "updatedDate": "2026-05-19T17:30:00"
  }
}
```

### Lỗi Thường Gặp

| Mã | Thông Báo | Nguyên Nhân |
|----|----------|------------|
| 404 | Parameter not found | Không tìm thấy tham số |

---

## 5. Xóa Tham Số

Xóa một tham số hệ thống.

### Endpoint

```http
DELETE /v1/parameters/{id}
```

### Response Example

```json
{
  "code": 200,
  "message": "Parameter deleted successfully",
  "data": null
}
```

### Lỗi Thường Gặp

| Mã | Thông Báo | Nguyên Nhân |
|----|----------|------------|
| 404 | Parameter not found | Không tìm thấy tham số |

---

## 📝 Ví Dụ cURL

```bash
# Lấy danh sách tham số
curl -X GET http://localhost:8081/cms-service/v1/parameters \
  -H "Authorization: Bearer <token>"

# Lấy chi tiết tham số
curl -X GET http://localhost:8081/cms-service/v1/parameters/123e4567-e89b-12d3-a456-426614174000 \
  -H "Authorization: Bearer <token>"

# Tạo tham số mới
curl -X POST http://localhost:8081/cms-service/v1/parameters \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "paramKey": "system.backup_retention_days",
    "paramValue": "30",
    "paramName": "Số ngày giữ bản backup",
    "description": "Số ngày lưu trữ file backup trước khi xóa",
    "status": 1
  }'

# Cập nhật tham số
curl -X PUT http://localhost:8081/cms-service/v1/parameters/123e4567-e89b-12d3-a456-426614174000 \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "paramValue": "60",
    "paramName": "Số ngày giữ backup (cập nhật)"
  }'

# Xóa tham số
curl -X DELETE http://localhost:8081/cms-service/v1/parameters/123e4567-e89b-12d3-a456-426614174000 \
  -H "Authorization: Bearer <token>"
```

---

## 🎯 Use Cases Thông Dùng

### Tham Số Hệ Thống Phổ Biến

| Key | Giá Trị Mặc Định | Mô Tả |
|-----|------------------|---------|
| `system.max_upload_size` | 10485760 | Kích thước upload tối đa (10MB) |
| `system.session_timeout` | 3600 | Thời gian session timeout (1 giờ) |
| `system.max_login_attempts` | 5 | Số lần đăng nhập sai tối đa |
| `system.password_min_length` | 6 | Độ dài mật khẩu tối thiểu |
| `system.password_expire_days` | 90 | Số ngày mật khẩu hết hạn |
| `system.backup_enabled` | 1 | Bật/tắt backup tự động |
| `system.maintenance_mode` | 0 | Chế độ bảo trì (0: bình thường, 1: bảo trì) |

---

## ⚠️ Lưu Ý

1. **Quyền hạn**: Chỉ ADMIN mới có quyền quản lý tham số
2. **ParamKey**: Không được thay đổi sau khi tạo
3. **Cache**: Tham số nên được cache ở application level để tránh query database liên tục
4. **Validation**: Giá trị tham số nên được validate trước khi sử dụng trong business logic
5. **Không được xóa**: Các tham số hệ thống quan trọng không nên bị xóa
