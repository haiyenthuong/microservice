# Architecture Overview

## Technology Stack

### Core Technologies
- **Java 21** - LTS version with modern language features
- **Spring Boot 3.5.5** - Core framework
- **Maven 3.8+** - Build and dependency management
- **MySQL 8.0+** - Relational database for each service
- **Apache Kafka 2.8+** - Event streaming platform

### Service Ports
- API Gateway: 8080
- Order Service: 8082
- Payment Service: 8083
- Kafka: 9092

## Design Patterns

### Event-Driven Architecture (EDA)
Services communicate asynchronously via Kafka events. This provides:
- Loose coupling between services
- Independent scaling
- Fault isolation
- Async processing of long-running tasks

### Choreography Saga Pattern
No central orchestrator. Services coordinate via events:
- Order Service publishes OrderCreatedEvent
- Payment Service consumes and processes payment
- Payment Service publishes PaymentProcessedEvent
- Order Service consumes and updates order status

### CQRS Pattern
Separate models for read (GraphQL) and write (REST) operations:
- Commands (Write): REST endpoints
- Queries (Read): GraphQL endpoints
- Optimized data models for each operation

### Transactional Event Publishing
Events are sent only after database commit succeeds:
- Uses TransactionSynchronizationManager
- afterCommit() callback publishes events
- Guarantees no orphan events

### Idempotency Pattern
Safe handling of duplicate Kafka messages:
- Check business state before processing
- Skip if already processed
- Exactly-once semantics at business level

## Communication Protocols

### HTTP/REST API
- Protocol: HTTP/1.1
- Data Format: JSON
- Authentication: JWT Bearer Token
- Entry Point: API Gateway (port 8080)

### GraphQL API
- Protocol: HTTP POST
- Data Format: GraphQL Query Language
- Direct Access: Order Service (port 8082)
- Optimized for complex queries

### Kafka Event Streaming
- Protocol: TCP over port 9092
- Serialization: JSON
- Delivery: At-least-once with manual ack
- Topics: order-events, payment-events

## Data Management

### Database per Service
Each service has its own database:
- api_gateway_db
- order_service_db
- payment_service_db

### ACID Transactions
- Local transactions within service boundary
- Eventual consistency across services
- Saga pattern for distributed transactions

## Security

### Authentication
- JWT tokens validated at API Gateway
- BCrypt password hashing
- Refresh token support
- Token expiration: 1 hour (access), 7 days (refresh)

### Authorization
- Role-based access control (RBAC)
- Gateway-level enforcement
- User ownership validation
- Header injection for downstream services

## Scalability

### Horizontal Scaling
- Stateless services
- Load balancing ready
- Kafka partitioning for parallel processing
- Connection pooling

### Monitoring
- Structured logging with trace IDs
- Spring Boot Actuator metrics
- Distributed tracing support
- Health check endpoints
