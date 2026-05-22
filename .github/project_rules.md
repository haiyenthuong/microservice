# Project Rules - Microservices E-Commerce System

## Coding Standards

### Java Code Style

- Follow Clean Code principles
- Use meaningful names for variables, methods, and classes
- Keep methods short and focused on single responsibility
- Use DTOs for API contracts, not domain entities
- Apply SOLID principles consistently

### Package Structure

Each service follows this structure:
```
com.{service}.api/          - Controllers, GraphQL resolvers
com.{service}.application/  - Commands, queries, services
com.{service}.domain/       - Entities, repositories, enums
com.{service}.infrastructure/ - Kafka, config, security
```

### Naming Conventions

- **Classes**: PascalCase (e.g., OrderService)
- **Methods**: camelCase (e.g., createOrder)
- **Constants**: UPPER_SNAKE_CASE (e.g., MAX_RETRY_COUNT)
- **Private fields**: camelCase (e.g., orderRepository)
- **DTOs**: End with 'Dto' or 'Command' or 'Query'

### Annotations

Required annotations:
- @Override: When overriding methods
- @Transactional: For service methods modifying data
- @KafkaListener: For Kafka consumers
- @Component/@Service/@Repository: For Spring beans

### Error Handling

- Never swallow exceptions
- Use specific exception types
- Include meaningful error messages
- Log errors at appropriate levels
- Propagate exceptions to be handled by global exception handler

### Event Publishing

- Always use TransactionSynchronizationManager for afterCommit
- Events must be immutable (use @Builder and final fields)
- Include eventId, eventType, timestamp, traceId
- Use proper partition keys (orderId)

### Database

- Use repository pattern, don't use EntityManager directly in services
- Entities must have proper equals() and hashCode()
- Use @Transactional for write operations
- Never expose entities directly to API layer

### Kafka

- Always use manual acknowledgment (Acknowledgment.acknowledge())
- Handle duplicate events (idempotency check)
- Log event consumption and processing
- Use proper consumer group IDs
- Never block consumer threads

### Testing

- Unit tests for business logic
- Integration tests for API endpoints
- Testcontainers for database tests
- Mock external dependencies
- Test both success and failure scenarios

### Security

- Never log sensitive data (passwords, tokens)
- Validate all input parameters
- Use parameterized queries to prevent SQL injection
- Never trust client-side data
- Implement proper authorization checks

### Performance

- Use connection pooling (HikariCP)
- Batch database operations when possible
- Use pagination for list queries
- Cache frequently accessed data
- Monitor Kafka consumer lag

### Documentation

- Javadoc for all public APIs
- Update README when adding features
- Document configuration changes
- Include examples in API documentation
- Keep diagrams up to date

## Git Workflow

### Branch Strategy

- main: Production code
- develop: Development branch
- feature/{feature-name}: Feature branches
- bugfix/{bug-name}: Bug fix branches

### Commit Messages

Format: {type}({scope}): {description}

Types:
- feat: New feature
- fix: Bug fix
- docs: Documentation changes
- refactor: Code refactoring
- test: Test changes
- chore: Build/config changes

Examples:
- feat(order): Add order cancellation feature
- fix(payment): Handle payment gateway timeout
- docs(readme): Update setup instructions
- refactor(kafka): Simplify event publisher

### Pull Request Rules

- PR title must follow commit message format
- Include description of changes
- Reference related issues
- All tests must pass
- Code review required before merge
- Update documentation if needed

## Best Practices

### Event-Driven Architecture

- Design events to be immutable
- Include all necessary data in events (don't query other services)
- Use event versioning for backward compatibility
- Handle duplicate events gracefully
- Never block on event processing

### Service Boundaries

- Each service owns its data
- No direct database access between services
- Communicate via APIs or events only
- Keep APIs stable and versioned
- Implement proper error handling

### Monitoring

- Include traceId in all log messages
- Use structured logging (JSON format)
- Log important business events
- Monitor Kafka consumer lag
- Track key metrics (orders created, payment success rate)

### Deployment

- Use environment variables for configuration
- Never commit secrets to repository
- Test in staging before production
- Use blue-green deployment
- Have rollback plan ready
