# Hướng Dẫn API Quản Lý Nhóm (Groups)

Tài liệu này hướng dẫn cách sử dụng các API quản lý nhóm người dùng và phân quyền.

## 📋 Table of Contents

- [Danh Sách Nhóm](#danh-sách-nhóm)
- [Chi Tiết Nhóm](#chi-tiết-nhóm)
- [Tạo Nhóm Mới](#tạo-nhóm-mới)
- [Cập Nhật Nhóm](#cập-nhật-nhóm)
- [Xóa Nhóm](#xóa-nhóm)
- [Gán User Vào Nhóm](#gán-user-vào-nhóm)
- [Gán Quyền Vào Nhóm](#gán-quyền-vào-nhóm)

---

## 🔑 Base URL

```
http://localhost:8081/cms-service/v1/groups
```

---

## ⚠️ Yêu Cầu Xác Thực

Tất cả endpoints yêu cầu JWT token:

```http
Authorization: Bearer <jwt-token>
```

---

## 1. Danh Sách Nhóm

Lấy danh sách tất cả các nhóm.

### Endpoint

```http
GET /v1/groups
```

### Response Example

```json
{
  "code": 200,
  "message": "Success",
  "data": [
    {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "groupName": "ADMIN_USERS",
      "status": 1,
      "authority": "Quản trị viên hệ thống",
      "description": "Nhóm quản trị viên có toàn quyền",
      "type": 0,
      "createdBy": "admin",
      "updatedBy": "admin",
      "createdDate": "2026-05-19T10:00:00",
      "updatedDate": "2026-05-19T10:00:00",
      "authorityIds": ["auth-001", "auth-002", "auth-003"],
      "userIds": ["user-001", "user-002"]
    },
    {
      "id": "223e4567-e89b-12d3-a456-426614174001",
      "groupName": "CONTENT_MANAGERS",
      "status": 1,
      "authority": "Quản lý nội dung",
      "description": "Nhóm quản lý nội dung website",
      "type": 1,
      "createdBy": "admin",
      "updatedBy": null,
      "createdDate": "2026-05-19T10:00:00",
      "updatedDate": "2026-05-19T10:00:00",
      "authorityIds": ["auth-010", "auth-011"],
      "userIds": ["user-003", "user-004", "user-005"]
    }
  ]
}
```

### Các Trường Dữ Liệu

| Trường | Kiểu | Mô Tả |
|-------|------|---------|
| groupName | String | Tên nhóm (tối đa 200 ký tự) |
| status | Integer | Trạng thái (1: hoạt động, 0: không hoạt động) |
| authority | String | Chuỗi quyền (tối đa 500 ký tự) - lưu thương báo |
| description | String | Mô tả nhóm (tối đa 500 ký tự) |
| type | Integer | Loại nhóm (0: hệ thống, 1: tùy chỉnh) |
| authorityIds | List<String> | Danh sách IDs quyền hạn của nhóm |
| userIds | List<String> | Danh sách IDs user thuộc nhóm |

---

## 2. Chi Tiết Nhóm

Lấy thông tin chi tiết một nhóm.

### Endpoint

```http
GET /v1/groups/{id}
```

### Response Example

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "groupName": "ADMIN_USERS",
    "status": 1,
    "authority": "Quản trị viên hệ thống",
    "description": "Nhóm quản trị viên có toàn quyền",
    "type": 0,
    "createdBy": "admin",
    "updatedBy": "admin",
    "createdDate": "2026-05-19T10:00:00",
    "updatedDate": "2026-05-19T10:00:00",
    "authorityIds": ["auth-001", "auth-002"],
    "userIds": ["user-001", "user-002"]
  }
}
```

---

## 3. Tạo Nhóm Mới

Tạo một nhóm mới và gán quyền/users.

### Endpoint

```http
POST /v1/groups
```

### Request Body

```json
{
  "groupName": "CONTENT_EDITORS",
  "status": 1,
  "authority": "Biên tập viên nội dung",
  "description": "Nhóm biên tập viên có quyền sửa nội dung",
  "type": 1,
  "authorityIds": ["auth-010", "auth-011", "auth-012"],
  "userIds": ["user-003", "user-004"]
}
```

### Các Trường Dữ Liệu

| Trường | Kiểu | Bắt Buộc | Mô Tả |
|-------|------|-----------|---------|
| groupName | String | ✅ | Tên nhóm (tối đa 200 ký tự) |
| status | Integer | ✅ | Trạng thái (1: hoạt động, 0: không hoạt động) |
| authority | String | ❌ | Chuỗi quyền (tối đa 500 ký tự) |
| description | String | ❌ | Mô tả (tối đa 500 ký tự) |
| type | Integer | ❌ | Loại nhóm (0: hệ thống, 1: tùy chỉnh), mặc định: 1 |
| authorityIds | List<String> | ❌ | Danh sách IDs quyền hạn |
| userIds | List<String> | ❌ | Danh sách IDs user |

### Response Example

**Thành công (200):**
```json
{
  "code": 200,
  "message": "Group created successfully",
  "data": {
    "id": "323e4567-e89b-12d3-a456-426614174005",
    "groupName": "CONTENT_EDITORS",
    "status": 1,
    "authority": "Biên tập viên nội dung",
    "description": "Nhóm biên tập viên có quyền sửa nội dung",
    "type": 1,
    "createdBy": "admin",
    "updatedBy": null,
    "createdDate": "2026-05-19T19:00:00",
    "updatedDate": "2026-05-19T19:00:00",
    "authorityIds": ["auth-010", "auth-011", "auth-012"],
    "userIds": ["user-003", "user-004"]
  }
}
```

### Lỗi Thường Gặp

| Mã | Thông Báo | Nguyên Nhân |
|----|----------|------------|
| 400 | Group name already exists | Tên nhóm đã tồn tại |
| 400 | Validation failed | Dữ liệu không hợp lệ |
| 404 | Authority/User not found | Một hoặc nhiều authority/user không tồn tại |

---

## 4. Cập Nhật Nhóm

Cập nhật thông tin nhóm, quyền hạn và thành viên.

### Endpoint

```http
PUT /v1/groups/{id}
```

### Request Body

```json
{
  "groupName": "CONTENT_EDITORS_UPDATED",
  "status": 1,
  "description": "Mô tả đã cập nhật",
  "authorityIds": ["auth-010", "auth-011", "auth-012", "auth-013"],
  "userIds": ["user-003", "user-004", "user-005"]
}
```

### Response Example

```json
{
  "code": 200,
  "message": "Group updated successfully",
  "data": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "groupName": "CONTENT_EDITORS_UPDATED",
    "status": 1,
    "description": "Mô tả đã cập nhật",
    "type": 1,
    "updatedBy": "admin",
    "updatedDate": "2026-05-19T19:30:00",
    "authorityIds": ["auth-010", "auth-011", "auth-012", "auth-013"],
    "userIds": ["user-003", "user-004", "user-005"]
  }
}
```

### ⚠️ Lưu Ý

Khi cập nhật `authorityIds` hoặc `userIds`:
- **Toàn bộ thay thế** danh sách cũ
- Danh sách cũ sẽ bị xóa và thay bằng danh sách mới

---

## 5. Xóa Nhóm

Xóa một nhóm và tất cả các gán quyền liên quan.

### Endpoint

```http
DELETE /v1/groups/{id}
```

### Response Example

```json
{
  "code": 200,
  "message": "Group deleted successfully",
  "data": null
}
```

### ⚠️ Cảnh Báo

**KHÔNG xóa nhóm đang được sử dụng!** Hãy đảm bảo không có user nào thuộc nhóm trước khi xóa.

---

## 6. Gán User Vào Nhóm

Gán users vào một nhóm (thay thế toàn bộ).

### Endpoint

```http
POST /v1/groups/assign-users
```

### Request Body

```json
{
  "groupId": "123e4567-e89b-12d3-a456-426614174000",
  "userIds": ["user-006", "user-007", "user-008"]
}
```

### Các Trường Dữ Liệu

| Trường | Kiểu | Bắt Buộc | Mô Tả |
|-------|------|-----------|---------|
| groupId | String | ✅ | ID của nhóm |
| userIds | List<String> | ✅ | Danh sách IDs user cần gán |

### Response Example

```json
{
  "code": 200,
  "message": "Users assigned to group successfully",
  "data": ["user-006", "user-007", "user-008"]
}
```

### ⚠️ Lưu Ý

- **Thay thế toàn bộ**: Users cũ sẽ bị loại khỏi nhóm và thay bằng users mới
- Một user có thể thuộc nhiều nhóm khác nhau

---

## 7. Gán Quyền Vào Nhóm

Gán quyền hạn vào một nhóm (thay thế toàn bộ).

### Endpoint

```http
POST /v1/groups/assign-authorities
```

### Request Body

```json
{
  "groupId": "123e4567-e89b-12d3-a456-426614174000",
  "authorityIds": ["auth-020", "auth-021", "auth-022"]
}
```

### Các Trường Dữ Liệu

| Trường | Kiểu | Bắt Buộc | Mô Tả |
|-------|------|-----------|---------|
| groupId | String | ✅ | ID của nhóm |
| authorityIds | List<String> | ✅ | Danh sách IDs quyền cần gán |

### Response Example

```json
{
  "code": 200,
  "message": "Authorities assigned to group successfully",
  "data": ["auth-020", "auth-021", "auth-022"]
}
```

### ⚠️ Lưu Ý

- **Thay thế toàn bộ**: Quyền cũ sẽ bị loại khỏi nhóm và thay bằng quyền mới
- Quyền của nhóm = TỔNG BỘ các quyền được gán

---

## 📝 Ví Dụ cURL

```bash
# Lấy danh sách nhóm
curl -X GET http://localhost:8081/cms-service/v1/groups \
  -H "Authorization: Bearer <token>"

# Lấy chi tiết nhóm
curl -X GET http://localhost:8081/cms-service/v1/groups/123e4567-e89b-12d3-a456-426614174000 \
  -H "Authorization: Bearer <token>"

# Tạo nhóm mới
curl -X POST http://localhost:8081/cms-service/v1/groups \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "groupName": "SALES_TEAM",
    "status": 1,
    "authority": "Nhóm bán hàng",
    "description": "Nhóm nhân viên bán hàng",
    "type": 1,
    "authorityIds": ["auth-030", "auth-031"],
    "userIds": ["user-010", "user-011", "user-012"]
  }'

# Cập nhật nhóm
curl -X PUT http://localhost:8081/cms-service/v1/groups/123e4567-e89b-12d3-a456-426614174000 \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "groupName": "SALES_TEAM_UPDATED",
    "description": "Mô tả đã cập nhật",
    "authorityIds": ["auth-030", "auth-031", "auth-032"]
  }'

# Gán user vào nhóm
curl -X POST http://localhost:8081/cms-service/v1/groups/assign-users \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "groupId": "123e4567-e89b-12d3-a456-426614174000",
    "userIds": ["user-013", "user-014"]
  }'

# Gán quyền vào nhóm
curl -X POST http://localhost:8081/cms-service/v1/groups/assign-authorities \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "groupId": "123e4567-e89b-12d3-a456-426614174000",
    "authorityIds": ["auth-040", "auth-041"]
  }'

# Xóa nhóm
curl -X DELETE http://localhost:8081/cms-service/v1/groups/123e4567-e89b-12d3-a456-426614174000 \
  -H "Authorization: Bearer <token>"
```

---

## 🎯 Use Cases Thông Dùng

### 1. Tạo Nhóm Quản Trị Viên

```bash
curl -X POST http://localhost:8081/cms-service/v1/groups \
  -H "Authorization: Bearer <admin-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "groupName": "ADMINISTRATORS",
    "status": 1,
    "authority": "Quản trị viên hệ thống",
    "description": "Toàn quyền truy cập",
    "type": 0,
    "authorityIds": ["all"],
    "userIds": ["admin-001"]
  }'
```

### 2. Tạo Nhóm Khách Hàng

```bash
curl -X POST http://localhost:8081/cms-service/v1/groups \
  -H "Authorization: Bearer <admin-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "groupName": "CUSTOMER_SERVICE",
    "status": 1,
    "authority": "Chăm sóc khách hàng",
    "description": "Nhóm nhân viên CSKH",
    "type": 1,
    "authorityIds": ["customer.read", "customer.write", "ticket.read", "ticket.write"],
    "userIds": ["user-020", "user-021", "user-022"]
  }'
```

### 3. Cập Nhật Thành Viên Nhóm

```bash
curl -X POST http://localhost:8081/cms-service/v1/groups/assign-users \
  -H "Authorization: Bearer <admin-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "groupId": "group-customer-service",
    "userIds": ["user-020", "user-021", "user-023", "user-024"]
  }'
```

### 4. Thêm Quyền Cho Nhóm

```bash
curl -X POST http://localhost:8081/cms-service/v1/groups/assign-authorities \
  -H "Authorization: Bearer <admin-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "groupId": "group-customer-service",
    "authorityIds": ["customer.read", "customer.write", "ticket.read", "ticket.write", "report.read"]
  }'
```

---

## ⚠️ Lưu Ý Quan Trọng

1. **Một User có thể thuộc nhiều Nhóm khác nhau**
   - User sẽ có TẤT CẢ các quyền của TẤT CẢ các nhóm mà họ thuộc
   - Quyền cuối cùng = hợp của tất cả các nhóm

2. **Không thể xóa Group đang có User**
   - Phải loại bỏ tất cả users khỏi nhóm trước
   - Hoặc thay thế bằng users khác

3. **Cập nhật `authorityIds` và `userIds` là thay thế hoàn toàn**
   - Khác với "thêm vào", đây là "thay thế toàn bộ"
   - Danh sách cũ sẽ bị xóa hoàn toàn

4. **Type = 0 là nhóm hệ thống**
   - Không nên xóa hoặc sửa nhóm hệ thống
   - Các nhóm hệ thống thường có quyền quan trọng

5. **Authority field là lưu thương báo**
   - Dùng để hiển thị nhanh trong UI
   - Quyền thực tế được lấy từ `authorityIds`

6. **User có thể không thuộc nhóm nào**
   - User không có nhóm = không có quyền (ngoại quyền mặc định)
