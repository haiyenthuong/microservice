---
type: "always_apply"
---

### **1. Nguyên tắc Chung**

* **Ngôn ngữ:**
    * Sử dụng **tiếng Việt** cho tất cả các phản hồi.
    * Giữ nguyên các thuật ngữ kỹ thuật **tiếng Anh** thuộc chuyên ngành Công nghệ thông tin và Phát triển phần mềm.
* **Phong cách:** Tạo phản hồi ngắn gọn, đơn giản và đi thẳng vào vấn đề.
* **Phạm vi:** Chỉ thực hiện và tạo tài liệu kiểm thử (testing) khi có yêu cầu cụ thể.
* **Lưu trữ:** Tất cả tài liệu phải được lưu dưới định dạng **Markdown** trong thư mục `/docs`.

### **2. Yêu cầu Dự án**

#### **2.1. Công nghệ và Kiến trúc**

* **Backend:** Java 21, Spring Boot 3.5.5, Hibernate 7.1.1, JPA 3.2.0

#### **2.2. Nguyên tắc Phát triển**

Áp dụng nghiêm ngặt các nguyên tắc sau cho TOÀN BỘ công nghệ và kiến trúc đã nêu:

* **Kiến trúc & Thiết kế:** Clean Architecture, Domain-Driven Design (DDD).
* **Lập trình:** Tuân thủ các nguyên tắc **Clean Code**, **SOLID**, **DRY**, **KISS**, và **YAGNI**.
* **Tiêu chuẩn:** Luôn tuân thủ `coding convention`, `coding guidelines`, và `best practices` của từng ngôn ngữ và
  framework.
    * **Nguồn tham chiếu ưu tiên:** Ưu tiên tham khảo và áp dụng theo **Google Style Guide** cho các ngôn ngữ được hỗ
      trợ (ví dụ: Java, TypeScript, Markdown).
* Sử dụng Javadoc cho tất cả các lớp, phương thức, và hằng số.
* Định dạng lại mã nguồn Java để tuân thủ theo Google Java Style

#### **2.3. Yêu cầu Hệ thống**

Hệ thống phải đảm bảo các tiêu chí sau:

* Hiệu năng cao.
* An toàn và bảo mật.
* Khả năng mở rộng (Scalability).
* Khả năng bảo trì (Maintainability).

### **3. Quy trình Xử lý**

#### **3.1. Quản lý Hướng dẫn (Guidelines)**

1. **Lưu trữ:** Tạo file hướng dẫn chi tiết trong thư mục `/docs/guidelines/`.
2. **Cập nhật tóm tắt:** Thêm một mục mới vào file `/docs/guidelines/summary.md` với các trường: `tên hướng dẫn`,
   `mô tả tóm tắt`, `tag`.

#### **3.2. Xử lý Lỗi (Issues)**

Khi nhận được yêu cầu sửa lỗi, hãy thực hiện theo quy trình sau:

1. **Tra cứu:** Tìm kiếm trong file `/docs/issues/summary.md` bằng `tag` để tìm các lỗi tương tự đã giải quyết.
2. **Tham khảo:** Nếu tìm thấy, sử dụng giải pháp chi tiết của lỗi đó làm tài liệu tham khảo.
3. **Lập tài liệu:**
    * Tạo một file Markdown mới trong `/docs/issues/` cho lỗi hiện tại.
    * Nội dung file phải bao gồm: **Phân tích lỗi**, **Giải pháp**, và **Kết quả**.
4. **Cập nhật tóm tắt:** Thêm một mục mới vào file `/docs/issues/summary.md` với các trường: `tên lỗi`, `mô tả tóm tắt`,
   `tag`.