# Project Rules - CMS Microservice

> **Last updated:** 2026-05-19
> **Version:** 1.0.0
> **Project:** CMS Service - User & Authority Management System

---

## 1. Tổng quan Dự án

### 1.1. Thông tin hệ thống
- **Tên dự án:** CMS Microservice
- **Mã dự án:** cms
- **Module:** cms-service
- **Mô-đun chính:** User Management, Authority Management, Group Management, Parameter Management

### 1.2. Công nghệ & Version
| Công nghệ | Version |
|-----------|---------|
| Java | 21 |
| Spring Boot | 3.5.5 |
| Spring Data JPA | 3.2.0 |
| Hibernate | 7.1.1 |
| Lombok | 1.18.34 |
| MapStruct | 1.6.3 |
| JWT (jjwt) | 0.12.6 |
| SpringDoc OpenAPI | 2.7.0 |

### 1.3. Nguyên tắc chung
- **Ngôn ngữ tài liệu:** Tiếng Việt
- **Ngôn ngữ code:** Tiếng Anh cho tất cả identifier, class, method, variable
- **Coding Style:** Google Java Style Guide
- **Design Patterns:** Clean Architecture, CQRS (Command Query Responsibility Segregation), DDD
- **Design Principles:** SOLID, DRY, KISS, YAGNI

---

## 2. Cấu trúc Package & Thư mục

### 2.1. Cấu trúc tổng thể
```
cms-service/src/main/java/com/cms/
├── application/
│   ├── command/          → CQRS Command classes (write operations)
│   ├── query/            → CQRS Query classes (read operations)
│   └── dto/              → Request/Response DTOs
├── domain/
│   ├── model/            → JPA Entities (Domain Models)
│   ├── repository/       → Repository Interfaces
│   └── common/           → Common exceptions, utilities
├── infrastructure/
│   └── config/           → Spring configurations (Security, JWT, JPA, Swagger...)
└── interfaces/
    └── rest/             → REST Controllers
```

### 2.2. Chi tiết từng package

#### 2.2.1. `application/command/`
- Chứa các class thực thi **write operations** (Create, Update, Delete, Lock, Unlock...)
- Implement interface `ICommand`
- Annotation: `@Component`, `@RequiredArgsConstructor`
- Method: `execute()` với `@Transactional`
- **CHỈ inject dependencies cố định** qua Constructor

#### 2.2.2. `application/query/`
- Chứa các class thực thi **read operations** (GetAll, GetById, Search...)
- Implement interface `IQuery`
- Annotation: `@Component`, `@RequiredArgsConstructor`
- Method: `execute()`
- **CHỈ inject dependencies cố định** qua Constructor

#### 2.2.3. `application/dto/`
- **Request DTOs:** `{Entity}Request` (Create, Update, Assign, Change...)
- **Response DTOs:** `{Entity}Response`
- **Common DTO:** `Response<T>` (wrapper cho API response)
- Sử dụng Lombok `@Builder`, `@Data`
- Validation annotations: `@NotNull`, `@Size`, `@Valid`...

#### 2.2.4. `domain/model/`
- JPA Entities
- Extend `BaseEntity`
- Annotation: `@Entity`, `@Table(name = "table_name")`
- Field annotation: `@Column(name = "column_name")`
- Enum field: `@Enumerated(EnumType.STRING)`
- Sử dụng `@SuperBuilder`, `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`
- Business logic methods đặt trong Entity

#### 2.2.5. `domain/repository/`
- Repository Interfaces
- Extend `JpaRepository<Entity, ID>`
- Annotation: `@Repository`
- Custom query methods naming convention
- `@Query` cho complex queries

#### 2.2.6. `domain/common/`
- Custom Exceptions:
  - `BaseException` (base class)
  - `BusinessException`
  - `ResourceNotFoundException`
  - `DuplicateResourceException`
  - `AuthenticationException`

#### 2.2.7. `infrastructure/config/`
- Security configuration
- JWT authentication filter & service
- JPA/Auditing configuration
- OpenAPI/Swagger configuration
- Application properties configuration

#### 2.2.8. `interfaces/rest/`
- REST Controllers
- Annotation: `@RestController`, `@RequestMapping`, `@Tag`
- OpenAPI annotations: `@Operation`, `@Parameter`
- Inject Commands/Queries via constructor
- **KHÔNG chứa business logic** - chỉ gọi Command/Query.execute()

---

## 3. Quy tắc CQRS (Command & Query)

### 3.1. Command Pattern

```java
@Component
@RequiredArgsConstructor
public class CreateUserCommand implements ICommand {

    // ✅ Static dependencies - CHỈ những này được inject qua Constructor
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    // private final MailService mailService;  ← Được phép

    // ❌ Runtime data - KHÔNG được inject qua Constructor
    // private final String userId;           ← SAI!
    // private final CreateUserRequest req;   ← SAI!

    @Transactional
    public UserResponse execute(CreateUserRequest request, String currentUserId) {
        // currentUserId truyền vào qua parameter, KHÔNG qua constructor
        User user = User.builder()
                .createdBy(currentUserId)  // Runtime data
                .build();
        userRepository.save(user);
        return mapToResponse(user);
    }
}
```

### 3.2. Query Pattern

```java
@Component
@RequiredArgsConstructor
public class GetUserByIdQuery implements IQuery {

    // ✅ Static dependencies
    private final UserRepository userRepository;

    public UserResponse execute(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return mapToResponse(user);
    }
}
```

### 3.3. Quy tắc execute()
- **Command:** `@Transactional` bắt buộc
- Tham số runtime (userId, request data) truyền qua **method parameter**, KHÔNG qua constructor
- Trả về DTO Response

---

## 4. 🚨 QUY TẮC CONSTRUCTOR INJECTION (QUAN TRỌNG)

> **Áp dụng cho:** TẤT CẢ class trong `com.cms.application.command` và `com.cms.application.query`

### 4.1. Nguyên tắc cốt lõi
Constructor **CHỈ** được phép chứa **static dependencies** - các object tồn tại toàn bộ vòng đời ứng dụng.

### 4.2. ✅ Được phép inject qua Constructor

| Loại | Ví dụ | Thời điểm tồn tại |
|-----|-------|------------------|
| Repository | `UserRepository`, `AuthorityRepository` | Singleton, toàn lifecycle |
| Service | `MailService`, `FileService` | Singleton, toàn lifecycle |
| Utility/Helper | `PasswordEncoder`, `ObjectMapper` | Singleton, toàn lifecycle |
| Mapper | `ModelMapper` | Singleton, toàn lifecycle |
| Config/Properties | `ConfigProperties` | Singleton, toàn lifecycle |
| External Client | `RestTemplate`, `WebClient` | Singleton, toàn lifecycle |

### 4.3. ❌ KHÔNG được phép inject qua Constructor

| Loại | Ví dụ | Lý do |
|-----|-------|-------|
| User runtime data | `userId`, `username`, `currentUser` | Thay đổi theo từng request |
| Request/Response DTO | `CreateUserRequest`, `UserResponse` | Chỉ tồn tại trong 1 request |
| HTTP Servlet objects | `HttpServletRequest`, `HttpServletResponse` | Request-scoped |
| Context data | `RequestContext`, `SecurityContext` | Request-scoped |
| Request parameters | `@PathVariable String id` | Request-scoped |
| Session data | `HttpSession` attributes | Session-scoped |
| Transaction context | `TransactionStatus` | Transaction-scoped |

### 4.4. Cách xử lý đúng

```java
// ❌ SAI - Inject runtime data qua constructor
@Component
@RequiredArgsConstructor
public class UpdateUserCommand implements ICommand {
    private final UserRepository userRepository;
    private final String currentUserId;  // SAI! Runtime data
}

// ✅ ĐÚNG - Truyền runtime data qua parameter
@Component
@RequiredArgsConstructor
public class UpdateUserCommand implements ICommand {
    private final UserRepository userRepository;  // Static dependency

    public UserResponse execute(String id, UpdateUserRequest request, String currentUserId) {
        // currentUserId truyền vào execute()
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        user.updateProfile(request.getFullname(), request.getMobile(), request.getAddress());
        user.setUpdatedBy(currentUserId);
        userRepository.save(user);
        return mapToResponse(user);
    }
}
```

### 4.5. Lý do quan trọng
1. **Thread Safety:** Runtime data khác nhau giữa các HTTP request
2. **Scope Mismatch:** Constructor gọi 1 lần tại startup, runtime data thay đổi mỗi request
3. **Testability:** Khó unit test khi inject runtime data
4. **DI Principle:** Violation of Dependency Injection - dependencies phải có thể resolve tại startup

---

## 5. Quy tắc Entity & Model

### 5.1. BaseEntity pattern
```java
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    @Id
    @Column(name = "id", length = 36)
    private String id;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "updated_date", nullable = false)
    private LocalDateTime updatedDate;

    @CreatedBy
    @Column(name = "created_by", length = 36)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by", length = 36)
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        if (id == null || id.isEmpty()) {
            id = UUID.randomUUID().toString();
        }
    }
}
```

### 5.2. Entity pattern
```java
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "adm_users")
public class User extends BaseEntity {
    @Column(name = "username", length = 20, nullable = false, unique = true)
    private String username;

    @Column(name = "password", nullable = false, length = 200)
    private String password;

    // Business logic methods
    public void lock() {
        this.status = UserStatus.LOCKED.getValue();
        setUpdatedDate(LocalDateTime.now());
    }
}
```

### 5.3. Quy tắc Enum
```java
public enum UserStatus {
    ACTIVE(1, "Active"),
    LOCKED(2, "Locked"),
    DELETED(3, "Deleted");

    private final Integer value;
    private final String description;

    // fromValue(), getValue(), business methods...
}
```

---

## 6. Quy tắc Repository

### 6.1. Repository Interface
```java
@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);

    @Query("SELECT u FROM User u WHERE u.status <> :status")
    List<User> findAllActive(@Param("status") Integer status);

    boolean existsByUsername(String username);
}
```

### 6.2. Quy tắc đặt tên method
| Prefix | Trả về | Ví dụ |
|--------|--------|-------|
| `findBy` | Optional/List | `findByUsername`, `findByStatus` |
| `existsBy` | boolean | `existsByUsername` |
| `countBy` | long/Long | `countByStatus` |
| `deleteBy` | void | `deleteByStatus` |

---

## 7. Quy tắc REST Controller

### 7.1. Controller pattern
```java
@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "User management APIs")
public class UserController {

    // Inject Commands/Queries via constructor
    private final GetAllUsersQuery getAllUsersQuery;
    private final GetUserByIdQuery getUserByIdQuery;
    private final CreateUserCommand createUserCommand;
    private final UpdateUserCommand updateUserCommand;

    @GetMapping
    @Operation(summary = "Get all users")
    public Response<List<UserResponse>> getAllUsers() {
        List<UserResponse> response = getAllUsersQuery.execute();
        return Response.success(response);
    }

    @PostMapping
    @Operation(summary = "Create user")
    public Response<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse response = createUserCommand.execute(request, getCurrentUserId());
        return Response.success("User created successfully", response);
    }
}
```

### 7.2. Quy tắc endpoint
- URL: `/v1/{resource}` hoặc `/v1/{resource}/{id}`
- HTTP Method: GET (read), POST (create), PUT (update), DELETE (delete)
- Response: `Response<T>` wrapper

---

## 8. Quy tắc đặt tên (Naming Conventions)

### 8.1. Entity
- Class: PascalCase, singular → `User`, `Authority`, `Group`
- Table: snake_case, có prefix → `adm_users`, `adm_authorities`

### 8.2. Repository
- Interface: `{Entity}Repository` → `UserRepository`
- Method: `findBy{Field}`, `existsBy{Field}` → `findByUsername`

### 8.3. Command/Query
- Command: `{Action}{Entity}Command` → `CreateUserCommand`, `LockUserCommand`
- Query: `{Action}{Entity}Query` → `GetAllUsersQuery`, `GetUserByIdQuery`

### 8.4. DTO
- Request: `{Action}{Entity}Request` → `CreateUserRequest`, `UpdateUserRequest`
- Response: `{Entity}Response` → `UserResponse`

### 8.5. Controller
- Class: `{Entity}Controller` → `UserController`, `AuthorityController`
- Method: `{action}{Entity}` → `getAllUsers`, `createUser`

### 8.6. Keys (Cache, Topic, Queue...)
Format: `cms_{service}_{env}_{action}_{param}`
```
cms_user_dev_lock_user_{userId}
cms_auth_prod_refresh_token_{tokenId}
```

---

## 9. Quy tắc Data Types

| Loại dữ liệu | Type | Ví dụ field |
|--------------|------|-------------|
| ID | String (UUID) | `id`, `userId`, `createdBy` |
| Username/Code | String | `username`, `authorityCode` |
| Password | String | `password` |
| Name/Title | String | `fullname`, `description` |
| Enum field | Integer | `status`, `type` |
| Date only | LocalDate | `birthDate` |
| DateTime | LocalDateTime | `createdDate`, `updatedDate` |
| Money/Amount | BigDecimal | `amount`, `price` |
| Count/Quantity | Integer/Long | `quantity`, `orderId` |
| Boolean | Boolean (is/has prefix) | `isActive`, `hasPermission` |
| List | List<T> | `authorities`, `groups` |

---

## 10. Yêu cầu Hệ thống

- Hiệu năng cao (High Performance)
- An toàn và bảo mật (Security)
- Khả năng mở rộng (Scalability)
- Khả năng bảo trì (Maintainability)

---

## 11. Coding Best Practices

### 11.1. Annotation
- **Override hooks:** Bắt buộc `@Override`
- **Validation:** `@NotNull`, `@Size`, `@Valid`
- **Transactional:** `@Transactional` trên Command.execute()

### 11.2. Exception handling
- KHÔNG try-catch quá mức (đã có GlobalExceptionHandler)
- Nên throw custom exceptions: `ResourceNotFoundException`, `DuplicateResourceException`

### 11.3. Documentation

#### 11.3.1. Javadoc Format (BẮT BUỘC)

Tất cả các phương thức public đều phải có Javadoc comment ở đầu hàm với định dạng sau:

```java
/**
   * Mô tả ngắn gọn mục đích của hàm (1 câu).
   *
   * @param paramName mô tả tham số (nếu có)
   * @return mô tả giá trị trả về (nếu có)
   * @throws ExceptionType mô tả trường hợp ngoại lệ (nếu có)
   */
```

**Quy tắc:**
- Dòng 1: Tóm tắt ngắn gọn, bắt đầu bằng động từ (không viết "Hàm này", "Phương thức này")
- Dòng 2: Dùng để tách phần mô tả và params
- `@param`: Mỗi param trên một dòng, mô tả rõ ý nghĩa
- `@return`: Mô tả giá trị trả về (nếu có)
- `@throws`: Mô tả các exception có thể throw (nếu có)
- Ngôn ngữ: Tiếng Việt cho mô tả, tiếng Anh cho tên parameter/return type

**Ví dụ đúng:**
```java
/**
   * Tạo mới người dùng trong hệ thống.
   *
   * @param request thông tin người dùng cần tạo
   * @param currentUserId ID của người đang thực hiện thao tác
   * @return UserResponse thông tin người dùng vừa được tạo
   * @throws DuplicateResourceException nếu username đã tồn tại
   */
public UserResponse execute(CreateUserRequest request, String currentUserId) {
    // ...
}
```

**Ví dụ sai:**
```java
// ❌ Thiếu Javadoc
public UserResponse execute(CreateUserRequest request, String currentUserId) {
    // ...
}

// ❌ Sai format - không có dấu * ở dòng thứ 2
/**
 * Tạo mới người dùng trong hệ thống.
 * @param request thông tin người dùng cần tạo
 */
public UserResponse execute(CreateUserRequest request) {
    // ...
}
```

#### 11.3.2. OpenAPI annotations
- OpenAPI annotations cho REST endpoints

### 11.4. Code quality
- Không magic numbers, magic strings
- Comment ngắn gọn, rõ ràng
- TODO phải rõ ràng: nội dung, lý do, deadline

---

## 12. Tham khảo

- [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- [Spring Boot Best Practices](https://springframework.guru/)
- [Clean Code - Robert C. Martin](https://www.oreilly.com/library/view/clean-code-a/9780136083238/)
- [Domain-Driven Design - Eric Evans](https://www.oreilly.com/library/view/domain-driven-design/0321125215/)
