# Hướng Dẫn API Quản Lý Quyền (Authorities)

Tài liệu này hướng dẫn cách sử dụng các API quản lý quyền hạn và phân quyền trong hệ thống.

## 📋 Table of Contents

- [Danh Sách Quyền](#danh-sách-quyền)
- [Chi Tiết Quyền](#chi-tiết-quyền)
- [Tạo Quyền Mới](#tạo-quyền-mới)
- [Cập Nhật Quyền](#cập-nhật-quyền)
- [Xóa Quyền](#xóa-quyền)

---

## 🔑 Base URL

```
http://localhost:8081/cms-service/v1/authorities
```

---

## ⚠️ Yêu Cầu Xác Thực

Tất cả endpoints yêu cầu JWT token:

```http
Authorization: Bearer <jwt-token>
```

---

## 1. Danh Sách Quyền

Lấy danh sách tất cả các quyền hạn trong hệ thống.

### Endpoint

```http
GET /v1/authorities
```

### Response Example

```json
{
  "code": 200,
  "message": "Success",
  "data": [
    {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "authority": "USER_READ",
      "fid": 1,
      "description": "Quyền xem danh sách người dùng",
      "orderId": 1,
      "authKey": "user.read",
      "createdBy": "admin",
      "updatedBy": null,
      "createdDate": "2026-05-19T10:00:00",
      "updatedDate": "2026-05-19T10:00:00"
    },
    {
      "id": "223e4567-e89b-12d3-a456-426614174001",
      "authority": "USER_WRITE",
      "fid": 1,
      "description": "Quyền tạo, sửa, xóa người dùng",
      "orderId": 2,
      "authKey": "user.write",
      "createdBy": "admin",
      "updatedBy": null,
      "createdDate": "2026-05-19T10:00:00",
      "updatedDate": "2026-05-19T10:00:00"
    },
    {
      "id": "323e4567-e89b-12d3-a456-426614174002",
      "authority": "PARAMETER_MANAGE",
      "fid": 2,
      "description": "Quyền quản lý tham số hệ thống",
      "orderId": 1,
      "authKey": "parameter.manage",
      "createdBy": "admin",
      "updatedBy": null,
      "createdDate": "2026-05-19T10:00:00",
      "updatedDate": "2026-05-19T10:00:00"
    }
  ]
}
```

### Các Trường Dữ Liệu

| Trường | Kiểu | Mô Tả |
|-------|------|---------|
| authority | String | Tên quyền hạn (duy nhất, ví dụ: USER_READ) |
| fid | Integer | ID chức năng cha (để nhóm quyền) |
| description | String | Mô tả quyền hạn |
| orderId | Integer | Thứ tự hiển thị |
| authKey | String | Key sử dụng trong code (để check quyền) |

---

## 2. Chi Tiết Quyền

Lấy thông tin chi tiết một quyền hạn.

### Endpoint

```http
GET /v1/authorities/{id}
```

### Response Example

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "authority": "USER_READ",
    "fid": 1,
    "description": "Quyền xem danh sách người dùng",
    "orderId": 1,
    "authKey": "user.read",
    "createdBy": "admin",
    "updatedBy": null,
    "createdDate": "2026-05-19T10:00:00",
    "updatedDate": "2026-05-19T10:00:00"
  }
}
```

---

## 3. Tạo Quyền Mới

Tạo một quyền hạn mới (chỉ ADMIN).

### Endpoint

```http
POST /v1/authorities
```

### Request Body

```json
{
  "authority": "GROUP_MANAGE",
  "fid": 3,
  "description": "Quyền quản lý nhóm người dùng",
  "orderId": 1,
  "authKey": "group.manage"
}
```

### Các Trường Dữ Liệu

| Trường | Kiểu | Bắt Buộc | Mô Tả |
|-------|------|-----------|---------|
| authority | String | ✅ | Tên quyền (duy nhất, tối đa 200 ký tự) |
| fid | Integer | ✅ | ID chức năng cha (để nhóm quyền liên quan) |
| description | String | ✅ | Mô tả quyền hạn (tối đa 500 ký tự) |
| orderId | Integer | ✅ | Thứ tự hiển thị |
| authKey | String | ❌ | Key dùng trong code (tối đa 100 ký tự) |

### Response Example

**Thành công (200):**
```json
{
  "code": 200,
  "message": "Authority created successfully",
  "data": {
    "id": "423e4567-e89b-12d3-a456-426614174004",
    "authority": "GROUP_MANAGE",
    "fid": 3,
    "description": "Quyền quản lý nhóm người dùng",
    "orderId": 1,
    "authKey": "group.manage",
    "createdBy": "admin",
    "updatedBy": null,
    "createdDate": "2026-05-19T18:00:00",
    "updatedDate": "2026-05-19T18:00:00"
  }
}
```

### Lỗi Thường Gặp

| Mã | Thông Báo | Nguyên Nhân |
|----|----------|------------|
| 400 | Authority already exists | Quyền hạn đã tồn tại |
| 400 | Validation failed | Dữ liệu không hợp lệ |

---

## 4. Cập Nhật Quyền

Cập nhật thông tin quyền hạn.

### Endpoint

```http
PUT /v1/authorities/{id}
```

### Request Body

```json
{
  "description": "Quyền quản lý nhóm người dùng (đã cập nhật)",
  "orderId": 2,
  "authKey": "group.manage.updated"
}
```

### Các Trường Dữ Liệu (Tất cả đều tùy chọn)

| Trường | Kiểu | Mô Tả |
|-------|------|---------|
| description | String | Mô tả mới |
| orderId | Integer | Thứ tự hiển thị mới |
| authKey | String | AuthKey mới |

### Response Example

```json
{
  "code": 200,
  "message": "Authority updated successfully",
  "data": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "authority": "GROUP_MANAGE",
    "fid": 3,
    "description": "Quyền quản lý nhóm người dùng (đã cập nhật)",
    "orderId": 2,
    "authKey": "group.manage.updated",
    "updatedBy": "admin",
    "createdDate": "2026-05-19T18:00:00",
    "updatedDate": "2026-05-19T18:30:00"
  }
}
```

---

## 5. Xóa Quyền

Xóa một quyền hạn khỏi hệ thống.

### Endpoint

```http
DELETE /v1/authorities/{id}
```

### Response Example

```json
{
  "code": 200,
  "message": "Authority deleted successfully",
  "data": null
}
```

### ⚠️ Cảnh Báo

**KHÔNG xóa quyền hạn đang được sử dụng bởi các nhóm hoặc người dùng!** Hãy đảm bảo quyền hạn không còn được gán cho bất kỳ nhóm nào trước khi xóa.

---

## 📝 Ví Dụ cURL

```bash
# Lấy danh sách quyền
curl -X GET http://localhost:8081/cms-service/v1/authorities \
  -H "Authorization: Bearer <token>"

# Lấy chi tiết quyền
curl -X GET http://localhost:8081/cms-service/v1/authorities/123e4567-e89b-12d3-a456-426614174000 \
  -H "Authorization: Bearer <token>"

# Tạo quyền mới
curl -X POST http://localhost:8081/cms-service/v1/authorities \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "authority": "REPORT_VIEW",
    "fid": 4,
    "description": "Quyền xem báo cáo",
    "orderId": 1,
    "authKey": "report.view"
  }'

# Cập nhật quyền
curl -X PUT http://localhost:8081/cms-service/v1/authorities/123e4567-e89b-12d3-a456-426614174000 \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Mô tả đã cập nhật",
    "orderId": 3
  }'

# Xóa quyền
curl -X DELETE http://localhost:8081/cms-service/v1/authorities/123e4567-e89b-12d3-a456-426614174000 \
  -H "Authorization: Bearer <token>"
```

---

## 🎯 Quyền Hệ Thống Phổ Biến

Dưới đây là danh sách các quyền thường dùng trong hệ thống:

### Quản Lý Người Dùng (fid=1)

| Authority | AuthKey | Mô Tả |
|----------|---------|---------|
| USER_READ | user.read | Xem danh sách, chi tiết user |
| USER_WRITE | user.write | Tạo, sửa, xóa user |
| USER_LOCK | user.lock | Khóa/mở khóa user |
| USER_DELETE | user.delete | Xóa user |

### Quản Lý Tham Số (fid=2)

| Authority | AuthKey | Mô Tả |
|----------|---------|---------|
| PARAMETER_READ | parameter.read | Xem tham số |
| PARAMETER_WRITE | parameter.write | Tạo, sửa, xóa tham số |

### Quản Lý Nhóm (fid=3)

| Authority | AuthKey | Mô Tả |
|----------|---------|---------|
| GROUP_READ | group.read | Xem nhóm |
| GROUP_WRITE | group.write | Tạo, sửa, xóa nhóm |
| GROUP_ASSIGN_USER | group.assign.user | Gán user vào nhóm |
| GROUP_ASSIGN_AUTHORITY | group.assign.authority | Gán quyền vào nhóm |

### Báo Cáo (fid=4)

| Authority | AuthKey | Mô Tả |
|----------|---------|---------|
| REPORT_VIEW | report.view | Xem báo cáo |
| REPORT_EXPORT | report.export | Xuất báo cáo |

---

## ⚠️ Lưu Ý Quan Trọng

1. **Authority vs AuthKey**:
   - `authority`: Tên đầy đủ (ví dụ: USER_READ) - lưu trong database
   - `authKey`: Key ngắn gọn dùng trong code (ví dụ: user.read) - dùng để check quyền

2. **Quyền được gán qua Nhóm (Group)**:
   - User không trực tiếp có quyền
   - User thuộc vào các Nhóm
   - Nhóm có các Quyền
   - User có tất cả Quyền của các Nhóm mà họ thuộc

3. **Không được thay đổi authority** sau khi tạo vì nó có thể đang được sử dụng

4. **FID (Function ID)** dùng để nhóm các quyền liên quan lại với nhau

5. **OrderId** dùng để sắp xếp thứ tự hiển thị trong UI
