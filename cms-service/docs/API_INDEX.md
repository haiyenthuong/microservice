# CMS Service - Tài Liệu API

Chào mừng đến với tài liệu API của CMS Service. Dịch vụ này cung cấp các REST API hoàn chỉnh để quản lý người dùng, xác thực, phân quyền và cấu hình hệ thống.

## 📚 Hướng Dẫn API

### Xác Thực & Quản Lý Người Dùng
- **[API Xác Thực](api_auth_guide.md)** - Đăng nhập, đăng ký, quản lý mật khẩu
- **[API Quản Lý Người Dùng](api_users_guide.md)** - Thêm, sửa, xóa, khóa/mở khóa người dùng

### Phân Quyền (RBAC)
- **[API Quản Lý Tham Số](api_parameters_guide.md)** - Cấu hình tham số hệ thống
- **[API Quản Lý Quyền](api_authorities_guide.md)** - Quản lý phân quyền và quyền hạn
- **[API Quản Lý Nhóm](api_groups_guide.md)** - Quản lý nhóm người dùng và gán quyền

## 🔐 Xác Thực

Tất cả API endpoints (trừ `/v1/auth/login` và `/v1/auth/register`) đều yêu cầu JWT token:

```bash
# Thêm JWT token vào Authorization header
Authorization: Bearer <jwt-token-cua-ban>
```

## 🌐 URL Cơ Sở

```
Phát triển: http://localhost:8081/cms-service
Sản xuất: https://api.yourdomain.com/cms-service
```

## 📖 Định Dạng Response

### Response Thành Công
```json
{
  "code": 200,
  "message": "Thao tác thành công",
  "data": { ... }
}
```

### Response Lỗi
```json
{
  "code": 400,
  "message": "Mô tả lỗi",
  "data": null
}
```

## 🔗 Swagger UI

Tài liệu API tương tác có tại: http://localhost:8081/swagger-ui.html

## 📝 Mã HTTP Status

| Mã | Mô Tả |
|----|-------|
| 200 | Thành công |
| 201 | Đã tạo |
| 400 | Yêu cầu không hợp lệ |
| 401 | Chưa xác thực |
| 403 | Không có quyền |
| 404 | Không tìm thấy |
| 500 | Lỗi máy chủ |

## 🚀 Bắt Đầu Nhanh

1. **Đăng ký người dùng mới:**
   ```bash
   POST /v1/auth/register
   ```

2. **Đăng nhập để lấy JWT token:**
   ```bash
   POST /v1/auth/login
   ```

3. **Sử dụng token cho các yêu cầu đã xác thực:**
   ```bash
   GET /v1/users
   Authorization: Bearer <token>
   ```
