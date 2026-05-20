# Hướng dẫn Review Code cho AI Agent

## 1. Mục tiêu review code

- Đảm bảo code tuân thủ coding convention, coding guidelines, best practices của ngôn ngữ và framework sử dụng.
- Phát hiện lỗi logic, bảo mật, hiệu năng, maintainability, readability.
- Đánh giá mức độ nghiêm trọng của từng vấn đề.

## 2. Tiêu chuẩn áp dụng

### 2.1. Tiêu chuẩn chung

#### A. Quy tắc đặt tên & cấu trúc

- Đặt tên biến, class, file, method liên quan đến nghiệp vụ (business/domain) phải đảm bảo sát nghĩa nghiệp vụ thực tế, nhất quán trong toàn bộ hệ thống. Các thành phần mang tính kỹ thuật, cấu trúc lập trình (ví dụ: list, service, controller, repository, entity, request, response, id, status, type, date, ...) phải sử dụng tiếng Anh chuẩn.
  - Ví dụ đúng: invBatchList, InvBatchService, isNotify...
  - Ví dụ sai: loList, los, loHangs, loHangService, laLoHang...
- Đặt các file, class, module vào đúng vị trí, đúng vai trò theo Clean Architecture (ví dụ: domain, application, infrastructure, api, ...), tránh lẫn lộn giữa các tầng, đảm bảo tính tách biệt và dễ bảo trì.
- Khi override các hàm dạng hook (ví dụ: beforeHandle, afterHandle, validate, ...), bắt buộc phải thêm annotation `@Override` để đảm bảo rõ ràng, đúng chuẩn Java và tránh lỗi tiềm ẩn.

#### B. Quy tắc code style & best practices

- Clean Code.
- SOLID.
- DRY (Don't Repeat Yourself).
- KISS (Keep It Simple, Stupid).
- YAGNI (You Aren't Gonna Need It).
- Google Style Guide (Java, TypeScript, Markdown...).
- Coding convention, coding guidelines của dự án.
- Class, field, method đều phải có Javadoc mô tả rõ chức năng.
- Comment phải ngắn gọn, đúng vị trí, không thừa.
- Không được để các biến, các imports không sử dụng.
- Không sử dụng các magic string, magic number trong code.
- Cần mark TODO rõ ràng trong code cho những logic chưa làm hoặc sẽ cần thực hiện trong tương lai, để đảm bảo dễ kiểm soát và không bỏ sót các phần quan trọng. TODO phải ghi chú rõ nội dung, lý do và (nếu có) thời điểm hoặc điều kiện cần thực hiện.

#### C. Xử lý dữ liệu & truy vấn

- Sử dụng chính xác kiểu dữ liệu (data type) phù hợp với ý nghĩa nghiệp vụ và mục đích sử dụng của từng biến, từng field. Tránh dùng kiểu dữ liệu chung chung hoặc không tối ưu cho nghiệp vụ. Một số ví dụ thực tế:
  - Thời gian, ngày tháng:
    - `LocalDate` cho ngày không kèm giờ (ví dụ: `expiredAt`)
    - `LocalDateTime` cho ngày giờ không có timezone
    - `OffsetDateTime` cho ngày giờ có timezone (ví dụ: `createdAt`, `modifiedAt`)
  - Định danh:
    - `UUID` cho các trường id, mã định danh file, entity (ví dụ: `userId`)
  - Số lượng, tiền tệ:
    - `BigDecimal` cho các trường lưu giá trị tiền, số lượng lớn, cần độ chính xác cao (ví dụ: `amount`)
    - `Integer`, `Long` cho các trường đếm, số thứ tự, id dạng số (ví dụ: `quantity`)
  - `Enum`: cho các trường trạng thái, loại, phân loại (ví dụ: `status`)
  - Danh sách: `List<T>` cho các trường kiểu danh sách (ví dụ: `invBatchList`)
  - `Boolean`: cho các trường xác định đúng/sai, có/không (ví dụ: `isDraft`)
  - `String`: cho các trường tên, mô tả, ghi chú, địa chỉ (ví dụ: `description`, `address`)
  - Luôn ưu tiên chọn kiểu dữ liệu sát nghĩa nghiệp vụ, tránh dùng kiểu dữ liệu không rõ ràng hoặc lạm dụng kiểu `String` cho các trường có ý nghĩa đặc thù.
- Cân nhắc sử dụng ModelMapper để mapping dữ liệu giữa các lớp (DTO, Entity, ViewModel, ...) thay vì mapping thủ công để giảm lỗi và tăng maintainability.
- Cần tận dụng tối đa khả năng của GenericRepository trước khi cân nhắc sử dụng giải pháp khác để truy xuất database, nhằm đảm bảo tính nhất quán, tái sử dụng và giảm dư thừa code truy vấn.
- Khi xây dựng query filter làm đầu vào cho các hàm trong `GenericRepository`, bắt buộc phải sử dụng các operator từ enum `FilterOperator` để đảm bảo tính nhất quán, đúng chuẩn và tránh lỗi logic filter.
- Hạn chế sử dụng try-catch trong code vì đã có Global Error Handler xử lý tập trung. Chỉ sử dụng try-catch khi thực sự cần can thiệp vào logic xử lý lỗi đặc biệt hoặc cần custom luồng xử lý exception.
- Khi trả về kết quả từ `CompletableFuture` (ví dụ: query.getResult()), cần sử dụng hàm `FutureHelper.getWithTimeout` để tránh blocking thread, đảm bảo hiệu năng và tránh deadlock. Không sử dụng trực tiếp `.join()` hoặc `.get()` trên CompletableFuture trong các lớp `Controller`, `Resolver`, `Service`.
  - Ví dụ đúng: `return FutureHelper.getWithTimeout(query.getResult());`
  - Ví dụ sai: `return query.getResult().join();`, `return query.getResult().get();`

#### D. Quy tắc kế thừa, mở rộng

- Các DTO class không được extends từ `BaseDomainEntity`.

#### E. Quy tắc đặt tên cho Topic Key, Cache Key, Queue Key, Lock Key, ...:

- Đặt tên theo định dạng: `<mã dự án>_<mã dịch vụ>_<env>_<key>`.
- Tất cả thành phần đều viết thường, phân cách bằng dấu gạch dưới `_`, không dùng ký tự đặc biệt hoặc khoảng trắng.
- `mã dự án`: viết tắt hoặc tên hệ thống (ví dụ: nds, cms, ...)
- `mã dịch vụ`: viết tắt hoặc tên service/module (ví dụ: qlbh-kolia-service, user, ...)
- `env`: môi trường (dev, stg, prod, ...)
- `key nghiệp vụ`: mô tả chức năng, nghiệp vụ, hành động (ví dụ: createccv, updateuser, lockfile, ...)
- Nếu có nhiều tham số động, nối thêm vào cuối theo thứ tự nghiệp vụ, phân cách bằng dấu `_` (ví dụ: kolia*koliaservice_dev_createccv*{id}, cms*user_prod_lockfile*{fileId})
- Đảm bảo tên key ngắn gọn, dễ nhận diện, nhất quán toàn hệ thống.
- Ví dụ:
  - `kolia_koliaservice_dev_createccv`
  - `cms_user_prod_lockfile_{fileId}`
  - `kolia_notification_stg_sendmail_{userId}`

### 2.2. Tiêu chuẩn đặc thù

- Field kiểu `boolean` đặt tên bắt đầu bằng `is` hoặc `has`.
- Field kiểu danh sách phải là `List<T>` và khởi tạo mặc định nếu cần.
- Sử dụng Lombok (`@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`) nếu phù hợp.
- Javadoc cho class và function phải đồng nhất về ý nghĩa, đầy đủ các thẻ `@param`, `@return`, `@throws` nếu có. Không được để thiếu hoặc sai ý nghĩa giữa các thành phần.
- Đặt tên biến, class, file, method liên quan đến nghiệp vụ (business/domain) phải đảm bảo sát nghĩa nghiệp vụ thực tế, nhất quán trong toàn bộ hệ thống. Các thành phần mang tính kỹ thuật, cấu trúc lập trình (ví dụ: list, service, controller, repository, entity, request, response, id, status, type, date, ...) phải sử dụng tiếng Anh chuẩn.
  - Ví dụ đúng: invBatchList, invBatchService, isDraft...
  - Ví dụ sai: loHangList, loHangs, laDraft,...
- Cân nhắc sử dụng ModelMapper để mapping dữ liệu giữa các lớp (DTO, Entity, ViewModel, ...) thay vì mapping thủ công để giảm lỗi và tăng maintainability.
- Cần tận dụng tối đa khả năng của GenericRepository trước khi cân nhắc sử dụng giải pháp khác để truy xuất database, nhằm đảm bảo tính nhất quán, tái sử dụng và giảm dư thừa code truy vấn.
- Hạn chế sử dụng try-catch trong code vì đã có Global Error Handler xử lý tập trung. Chỉ sử dụng try-catch khi thực sự cần can thiệp vào logic xử lý lỗi đặc biệt hoặc cần custom luồng xử lý exception.
- Sử dụng chính xác kiểu dữ liệu (data type) phù hợp với ý nghĩa nghiệp vụ và mục đích sử dụng của từng biến, từng field. Tránh dùng kiểu dữ liệu chung chung hoặc không tối ưu cho nghiệp vụ. Một số ví dụ thực tế:
  - Thời gian, ngày tháng:
    - `LocalDate` cho ngày không kèm giờ (ví dụ: `ExpiredAt`)
    - `LocalDateTime` cho ngày giờ không có timezone
    - `OffsetDateTime` cho ngày giờ có timezone (ví dụ: `createdAt`, `modifiedAt`)
  - Định danh:
    - `UUID` cho các trường id, mã định danh file, entity (ví dụ: `userId`)
  - Số lượng, tiền tệ:
    - `BigDecimal` cho các trường lưu giá trị tiền, số lượng lớn, cần độ chính xác cao (ví dụ: `amount`)
    - `Integer`, `Long` cho các trường đếm, số thứ tự, id dạng số (ví dụ: `quantity`)
  - `Enum`: cho các trường trạng thái, loại, phân loại (ví dụ: `status`)
  - Danh sách: `List<T>` cho các trường kiểu danh sách (ví dụ: `invBatchList`)
  - `Boolean`: cho các trường xác định đúng/sai, có/không (ví dụ: `isDraft`)
  - `String`: cho các trường tên, mô tả, ghi chú, địa chỉ (ví dụ: `description`, `address`)
  - Luôn ưu tiên chọn kiểu dữ liệu sát nghĩa nghiệp vụ, tránh dùng kiểu dữ liệu không rõ ràng hoặc lạm dụng kiểu `String` cho các trường có ý nghĩa đặc thù.
- Các DTO class không được extends từ `BaseDomainEntity`.
- Khi xây dựng query filter làm đầu vào cho các hàm trong `GenericRepository`, bắt buộc phải sử dụng các operator từ enum `FilterOperator` để đảm bảo tính nhất quán, đúng chuẩn và tránh lỗi logic filter.
- Tiêu chuẩn đối với entities:
  - Tên class, tên file phải đúng chuẩn PascalCase, trùng với thực thể.
  - Entity phải có annotation `@Entity` và `@Table`, tên bảng phải nằm trong `""`.
  - Tên bảng, tên cột phải map đúng với database thực tế.
  - Các field phải có annotation `@Column`, tên cột nằm trong `""`.
  - Field kiểu enum phải có `@Enumerated(EnumType.STRING)`.
  - Các liên kết phải dùng đúng annotation: `@OneToMany`, `@ManyToOne`, `@OneToOne`, `@JoinColumn`.
  - Các field để lưu trữ thông tin file cần dùng type là `UUID` (bởi vì chúng ta định danh một file dựa vào `Id-UUID`).
- Repository Interface:
  - đặt trong package `domain.repositories`.
  - Kế thừa từ `GenericRepository<TEntity, TKey>`.
  - Có Javadoc mô tả chức năng.
- Repository Implementation:
  - Đặt trong package infrastructure.repositories.
  - Kế thừa từ `GenericRepositoryImpl<TEntity, TKey>`.
  - Có annotation @Repository.
  - Hàm khởi tạo sử dụng `protected`.
- CQRS command / query:
  - Tên class, tên file phải đúng chuẩn PascalCase, trùng với chức năng (ví dụ: CreateExampleEntityCommand, GetExampleEntityByIdQuery).
  - Đặt đúng package:
    - Command: application.commands.[entity]/
    - Query: application.queries.[entity]/
  - Các trường (field) trong class command phải được gắn annotation validate dữ liệu phù hợp (ví dụ: `@NotNull`, `@Size`, ...), đảm bảo dữ liệu đầu vào hợp lệ trước khi xử lý.
  - Trong mỗi Command handler hoặc Query handler, bắt buộc phải định nghĩa hàm `handle` và gắn annotation `@EventListener` cho hàm này. Hàm này là điểm vào chính để CQRS framework tự động ánh xạ (map) command/query với handler tương ứng.
  - Hàm `handle` của Command handler phải gắn annotation `@Transactional`.
  - Command handler phải sử dụng đúng và đầy đủ các hàm dạng hook (ví dụ: `beforeHandle`, `afterHandle`, ...) được cung cấp bởi các class cha như `BaseCommandCreateHandler`, `BaseCommandUpdateHandler`, `BaseCommandDeleteHandler` để đảm bảo mở rộng đúng chuẩn và kiểm soát luồng xử lý.
  - Command/Query và handler class của chúng phải nằm chung trong một file để đảm bảo tính liên kết, dễ bảo trì và kiểm soát luồng xử lý.
  - Khi trả về kết quả từ `CompletableFuture` (ví dụ: query.getResult()), cần sử dụng hàm `FutureHelper.getWithTimeout` để tránh blocking thread, đảm bảo hiệu năng và tránh deadlock. Không sử dụng trực tiếp `.join()` hoặc `.get()` trên CompletableFuture trong các lớp `Controller`, `Resolver`, `Service`.
    - Ví dụ đúng: `return FutureHelper.getWithTimeout(query.getResult());`
    - Ví dụ sai: `return query.getResult().join();`, `return query.getResult().get();`
  - Khi override các hàm dạng hook (ví dụ: `beforeHandle`, `afterHandle`, `validate`, ...), bắt buộc phải thêm annotation `@Override` để đảm bảo rõ ràng, đúng chuẩn Java và tránh lỗi tiềm ẩn.
- Controller:
  - Tên class, tên file phải đúng chuẩn PascalCase, kết thúc bằng `Controller`.
  - Đặt đúng package: `api.controllers`.
  - Class phải có annotation `@RestController` hoặc `@Controller`.
  - Định nghĩa rõ ràng các endpoint với annotation như `@RequestMapping`, `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`.
  - Endpoint phải rõ nghĩa, tuân thủ chuẩn RESTful.
  - Controller chỉ xử lý request/response, không chứa logic nghiệp vụ.
  - Send CQRS command để xử lý nghiệp vụ, không truy cập repository trực tiếp.
- Graphql Resolver:
  - Tên class, tên file phải đúng chuẩn PascalCase, kết thúc bằng `Resolver`.
  - Đặt đúng package: `api.graphql`.
  - Send CQRS query để xử lý nghiệp vụ, không truy cập repository trực tiếp.
- GraphQL Schema:
  - Tên file schema phải rõ ràng, phân loại theo domain hoặc entity, tên file phải đúng chuẩn snake_case (chỉ chữ thường, phân cách bằng dấu gạch dưới).
  - Đặt đúng thư mục:
    - Định nghĩa chung: `graphql/common/`
    - Định nghĩa query: `graphql/queries/`
    - Schema tổng: `graphql/schema.graphqls`
- Liquibase:
  - Các column thường xuyên cần query (lọc, join, sort) phải được đánh index trong file changelog Liquibase để tối ưu hiệu năng truy vấn.

## 3. Quy trình review code

1. Xác định file, dòng code cần review (ghi rõ file, dòng bắt đầu, dòng kết thúc).
2. Đọc kỹ đoạn code, xác định các vấn đề vì phạm tiêu chuẩn đã nêu. Chỉ nếu ra các vấn đề vi phạm tiêu chuẩn, còn đã đúng rồi thì không cần nêu ra.
3. Ghi chú chi tiết từng vấn đề, nêu rõ lý do và đề xuất hướng xử lý.
4. Đánh giá mức độ nghiêm trọng cho từng vấn đề:
   - Rất nghiêm trọng
   - Cao
   - Trung bình
   - Thấp
5. Tổng hợp kết quả review, trình bày ngắn gọn, rõ ràng.

## 4. Định dạng tài liệu review

- Lưu dưới dạng Markdown trong thư mục `/docs/code_review/react_<tên file>.md`.
- Mỗi review phải có:
  - Thông tin file, dòng code
  - Danh sách vấn đề, mức độ nghiêm trọng, đề xuất xử lý
  - Tổng kết chung

## 5. Tham khảo

- [Google Style Guide](https://google.github.io/styleguide/)
- Tài liệu coding convention của dự án
