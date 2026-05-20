# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**qlbh-kolia-service** - Kolia Quản lý bán hàng (Sales Management System)

A Spring Boot 3.5.5 application using Java 21, implementing Clean Architecture with CQRS pattern for a sales management system.

- **Framework**: Spring Boot 3.5.5, Hibernate 7.1.1, JPA 3.2.0
- **Database**: PostgreSQL
- **API**: GraphQL (queries) + REST (commands)
- **Architecture**: Clean Architecture with DDD
- **Language**: Java 21

## Build & Run Commands

```bash
# Build the project
mvn clean install

# Run tests
mvn test

# Run the application
mvn spring-boot:run

# Package as JAR
mvn clean package
```

The application runs on port **8086** by default (configurable in `application.yml`).

## Architecture

### Clean Architecture Layers

The codebase follows strict Clean Architecture principles with these layers:

```
src/main/java/vn/osp/qlbh/
├── api/               # Interface adapters (Controllers, GraphQL Resolvers)
├── application/       # Application use cases (Commands, Queries, DTOs, Services)
├── domain/            # Domain entities, repositories interfaces, enums, projections
└── infrastructure/    # External concerns (JPA entities, repository implementations, config)
```

### CQRS Pattern

The application uses CQRS (Command Query Responsibility Segregation):

- **Commands** (`application.commands.<entity>/*`): Write operations (Create, Update, Delete)
- **Queries** (`application.queries.<entity>/*`): Read operations (Get, List, Search)

**Command Structure:**
```java
// Command class with validation annotations
@Getter @Setter
public class CreateXxxCommand extends CreateCommand<UUID> {
    @NotNull @Size(max = 200)
    private String name;
}

// Handler class with @EventListener
@Component
class CreateXxxCommandHandler extends BaseCommandCreateHandler<...> {
    @EventListener
    @Transactional
    public void handle(CreateXxxCommand command) {
        super.handle(command);
    }
}
```

**Query Structure:**
```java
public class GetXxxQuery extends PaginateQuery<Xxx> { ... }

@Component
class GetXxxQueryHandler extends BasePaginateQueryHandler<...> {
    @EventListener
    public void handle(GetXxxQuery query) {
        super.handle(query);
    }
}
```

### Key Dependencies

- **vn.osp.common** (v0.0.106): Internal framework providing base classes:
  - `GenericRepository<T, K>`: Base repository interface
  - `GenericRepositoryImpl<T, K>`: Base repository implementation
  - `BaseCommandCreateHandler`, `BaseCommandUpdateHandler`, `BaseCommandDeleteHandler`
  - `BasePaginateQueryHandler`, `BaseGetByIdQueryHandler`
  - `FutureHelper.getWithTimeout()`: For non-blocking CompletableFuture handling

## Coding Standards

### Naming Conventions

- **Business/Domain variables**: Use Vietnamese business terms (e.g., `invBatchList`, `loHang`, `congNo`)
- **Technical components**: Use English (e.g., `service`, `controller`, `repository`, `id`, `status`)
- **Booleans**: Prefix with `is` or `has` (e.g., `isVisible`, `hasPermission`)
- **Lists**: Use `List<T>` with descriptive names (e.g., `invBatchList`, not `invBatches`)

### Data Types

| Purpose | Type | Example |
|---------|------|---------|
| Timestamps with timezone | `OffsetDateTime` | `createdAt`, `modifiedAt` |
| Dates without time | `LocalDate` | `expiredAt` |
| Local date/time | `LocalDateTime` | - |
| IDs | `UUID` | `userId` |
| Money/Amounts | `BigDecimal` | `amount`, `price` |
| Counts/Indices | `Integer`/`Long` | `quantity`, `order` |
| Statuses/Types | `Enum` | `status`, `type` |
| Lists | `List<T>` | `itemList` |
| Flags | `Boolean` | `isDeleted` |

### Key Rules

1. **No DTO extending `BaseDomainEntity`**: DTOs must not extend domain entity base classes
2. **Use `FutureHelper.getWithTimeout()`**: Never use `.join()` or `.get()` on CompletableFuture in controllers/resolvers
3. **FilterOperator enum**: Use `FilterOperator` for query filters, not raw operators
4. **GenericRepository**: Leverage `GenericRepository` methods before writing custom queries
5. **@Override annotation**: Required when overriding hook methods (`beforeHandle`, `afterHandle`, `validate`)
6. **Validation**: Command fields must have Jakarta validation annotations (`@NotNull`, `@Size`, etc.)
7. **No blocking calls**: Avoid `.join()`/`.get()` on CompletableFuture
8. **Javadoc**: All classes and public methods require Javadoc with `@param`, `@return`, `@throws`

### Key/Cache Naming Format

```
<project>_<service>_<env>_<key>
```

Example: `kolia_koliaservice_dev_createccv`, `kolia_notification_stg_sendmail_{userId}`

## Project Structure Details

### Controllers (`api.controllers`)

- REST endpoints for commands (POST/PUT/DELETE)
- Extend `BaseRestApi`
- Use `doPost()`, `doPut()`, `doDelete()` helper methods
- Never access repositories directly - use CQRS commands

### GraphQL Resolvers (`api.graphql`)

- GraphQL query endpoints
- Send CQRS queries
- Schema files in `src/main/resources/graphql/queries/*.graphqls`

### Repository Pattern

**Interface** (`domain.repositories`):
```java
public interface XxxRepository extends GenericRepository<Xxx, UUID> {}
```

**Implementation** (`infrastructure.repositories`):
```java
@Repository
public class XxxRepositoryImpl extends GenericRepositoryImpl<Xxx, UUID>
        implements XxxRepository {
    protected XxxRepositoryImpl(EntityManager entityManager) {
        super(entityManager);
    }
}
```

### Domain Entities

- Located in `domain.entities.<context>/`
- Extend `BaseDomainEntityUuid` (for UUID entities)
- Use Lombok: `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@FieldNameConstants`
- Implement interfaces like `HasCreatedBy`, `HasModifiedBy` for audit fields

### GraphQL Schemas

- Location: `src/main/resources/graphql/`
- Common definitions: `graphql/common/*.graphqls` (pagination, sort, filter)
- Query definitions: `graphql/queries/*.graphqls`
- Main schema: `graphql/schema.graphqls`

## Database Migrations

- **Liquibase** for schema management
- Changelog: `src/main/resources/db/changelog/db.changelog-master.xml`
- Configuration: `liquibase.properties`

## Configuration

- **Main config**: `src/main/resources/application.yml`
- **Environment-specific**: `application-dev.yml`, etc.
- **Server port**: 8086
- **Timezone**: UTC (set in `Application.main()`)
- **Async timeout**: 30 seconds
- **CORS**: Configured for all origins by default

## Testing

Test location: `src/test/java/`

Run tests with: `mvn test`

## Common Development Patterns

### Creating a New Entity

1. Domain entity in `domain.entities/`
2. Repository interface in `domain.repositories/`
3. Repository implementation in `infrastructure.repositories/`
4. Create command/handler in `application.commands.<entity>/`
5. Update command/handler (if needed)
6. Query handlers in `application.queries.<entity>/`
7. Controller in `api.controllers/` (for REST)
8. GraphQL resolver in `api.graphql/` (for queries)
9. GraphQL schema in `resources/graphql/queries/<entity>.graphqls`

### Adding Query Filters

Use `FilterHelper` and `FilterOperator` enum:
```java
FilterHelper.equal("status", status)
FilterHelper.like("name", name, FilterOperator.CONTAINS)
```

### Handling Async Results

Always use `FutureHelper.getWithTimeout()`:
```java
return FutureHelper.getWithTimeout(query.getResult());
```

## Vietnamese Language Support

This project uses Vietnamese for:
- All comments and documentation
- Business domain variable names
- Error messages
- Log messages

Technical terms remain in English (e.g., "service", "controller", "repository").
