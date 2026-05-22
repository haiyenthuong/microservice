# Order Service

## 📋 Overview

Order Service là một Event-Driven Microservice chịu trách nhiệm:
- Quản lý Orders (tạo, update, query)
- Event-Driven communication với Payment Service
- Saga pattern cho Order-Payment flow

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                       API Gateway (8080)                         │
│  - Centralized JWT validation                                    │
│  - Inject X-User-Id, X-User-Name, X-User-Fullname headers       │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             │ HTTP Headers
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Order Service (8082)                          │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  UserHeaderInterceptor                                  │   │
│  │  • Extract X-User-Id, X-User-Name, X-User-Fullname      │   │
│  │  • Store in ThreadLocal (UserContextHolder)             │   │
│  └─────────────────────────────────────────────────────────┘   │
│                              │                                   │
│                              ▼                                   │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  Controllers / Commands / Queries                       │   │
│  │  • Access user context via UserContextHolder            │   │
│  │  • No JWT validation (done at Gateway)                  │   │
│  └─────────────────────────────────────────────────────────┘   │
│                              │                                   │
│                              ▼                                   │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  OrderEventProducer                                     │   │
│  │  • Publish OrderCreatedEvent to Kafka                   │   │
│  └─────────────────────────────────────────────────────────┘   │
│                              │                                   │
└──────────────────────────────┼───────────────────────────────────┘
                               │
                               │ Kafka Events
                               ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Payment Service (8083)                        │
│  • Consume OrderCreatedEvent                                    │
│  • Process payment                                               │
│  • Publish PaymentSuccessEvent OR PaymentFailedEvent           │
└─────────────────────────────┬───────────────────────────────────┘
                              │
                              │ Kafka Events
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Order Service (8082)                          │
│  PaymentEventConsumer                                            │
│  • Consume PaymentSuccessEvent → Update Order: PAID             │
│  • Consume PaymentFailedEvent → Update Order: PAYMENT_FAILED   │
│  • Publish OrderPaidEvent OR OrderPaymentFailedEvent           │
└─────────────────────────────────────────────────────────────────┘
```

## 🚀 Quick Start

### Prerequisites

```bash
# Java
java -version  # OpenJDK 21+

# Maven
mvn -version

# Kafka (đã chạy)
kafka-topics.sh --bootstrap-server localhost:9092 --list

# API Gateway (đã chạy)
curl http://localhost:8080/actuator/health
```

### Build & Run

```bash
# Build
cd d:/1.Project/microservice/order-service
mvn clean install

# Run
mvn spring-boot:run

# Hoặc chạy JAR
java -jar target/order-service-1.0.0.jar
```

### Health Check

```bash
curl http://localhost:8082/order-service/actuator/health
```

## 📡 API Endpoints

### Create Order

```bash
POST /order-service/api/v1/orders
Headers:
  X-User-Id: <user-id-from-gateway>
  X-User-Name: <username-from-gateway>
  X-User-Fullname: <fullname-from-gateway>
  Content-Type: application/json

Body:
{
  "customerId": "123e4567-e89b-12d3-a456-426614174000",
  "items": [
    {
      "productId": "prod-001",
      "productName": "Laptop",
      "quantity": 1,
      "unitPrice": 1000000
    }
  ],
  "shippingAddress": "123 Main St, City, Country",
  "paymentMethod": "CREDIT_CARD"
}

Response:
{
  "orderId": "order-123",
  "status": "PENDING",
  "totalAmount": 1000000,
  "createdAt": "2024-05-20T10:00:00Z"
}
```

### Get Order

```bash
GET /order-service/api/v1/orders/{orderId}
Headers:
  X-User-Id: <user-id-from-gateway>
  X-User-Name: <username-from-gateway>

Response:
{
  "orderId": "order-123",
  "status": "PAID",
  "totalAmount": 1000000,
  "items": [...]
}
```

### List Orders

```bash
GET /order-service/api/v1/orders?page=0&size=20
Headers:
  X-User-Id: <user-id-from-gateway>
  X-User-Name: <username-from-gateway>
```

## 🔐 Security Model

### Gateway Pattern (New)

```
Client → API Gateway (JWT validation)
         ↓ Inject headers
    Order Service (read headers only)
```

### Access User Context

```java
// Trong bất kỳ Controller/Service/Command/Query:
@Component
public class CreateOrderCommandHandler {

    private final SecurityHelper securityHelper;

    public void handle(CreateOrderCommand command) {
        // Get current user info
        String userId = securityHelper.getCurrentUserId();
        String username = securityHelper.getCurrentUsername();

        // Use in business logic
        Order order = new Order();
        order.setCustomerId(userId);
        order.setCreatedBy(username);

        orderRepository.save(order);
    }
}
```

### Require Authentication

```java
// Trong Controller:
@PostMapping("/orders")
public ResponseEntity<Order> createOrder(@RequestBody CreateOrderRequest request) {
    // Require authentication
    securityHelper.requireAuthenticated();

    // Create order...
    return ResponseEntity.ok(order);
}

// Hoặc require specific user:
@PostMapping("/orders/{orderId}/cancel")
public ResponseEntity<Void> cancelOrder(
        @PathVariable String orderId
) {
    // Get order and check ownership
    Order order = orderRepository.findById(orderId);
    securityHelper.requireUser(order.getCustomerId());

    // Cancel order...
    return ResponseEntity.ok().build();
}
```

## 🔄 Order-Payment Saga

### Flow

```
1. Order Created
   POST /orders
   → Order status: PENDING
   → Publish: OrderCreatedEvent to Kafka

2. Payment Processing (Async)
   PaymentService receives OrderCreatedEvent
   → Process payment with provider

3a. Payment Success
    ← PaymentSuccessEvent from PaymentService
    → Order status: PAID
    → Publish: OrderPaidEvent to Kafka
    → Other services consume (Inventory, Notification)

3b. Payment Failed
    ← PaymentFailedEvent from PaymentService
    → Order status: PAYMENT_FAILED
    → Publish: OrderPaymentFailedEvent to Kafka
    → Schedule retry if retryable
```

### Events

#### OrderCreatedEvent
```json
{
  "eventId": "evt-123",
  "eventType": "ORDER_CREATED",
  "aggregateId": "order-123",
  "timestamp": "2024-05-20T10:00:00Z",
  "userId": "user-123",
  "traceId": "trace-abc",
  "customerId": "user-123",
  "items": [...],
  "totalAmount": 1000000,
  "currency": "USD",
  "shippingAddress": "...",
  "paymentInfo": {...}
}
```

#### PaymentSuccessEvent
```json
{
  "eventId": "evt-456",
  "eventType": "PAYMENT_SUCCESS",
  "aggregateId": "payment-456",
  "orderId": "order-123",
  "paymentId": "payment-456",
  "transactionId": "txn-789",
  "paidAmount": 1000000,
  "currency": "USD",
  "paymentMethod": "CREDIT_CARD",
  "provider": "stripe"
}
```

## 🧪 Testing

### Unit Tests

```bash
# Run tests
mvn test

# Run specific test class
mvn test -Dtest=SecurityHelperTest
```

### Integration Tests with Kafka

```bash
# Start Kafka (Docker)
docker-compose -f kafka-docker-compose.yml up -d

# Run integration tests
mvn verify -Pintegration-test
```

## 🔧 Configuration

### application.yml

```yaml
server:
  port: 8082

spring:
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
    consumer:
      group-id: order-service-group
      auto-offset-reset: earliest
      enable-auto-commit: false
```

### Environment Variables

```bash
# Kafka
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Database
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=order_db

# Service
export SERVER_PORT=8082
```

## 📊 Monitoring

### Actuator Endpoints

```bash
# Health
curl http://localhost:8082/order-service/actuator/health

# Metrics
curl http://localhost:8082/order-service/actuator/metrics

# Kafka Metrics
curl http://localhost:8082/order-service/actuator/kafka
```

### Prometheus Metrics

```bash
# Scrape metrics
curl http://localhost:8082/order-service/actuator/prometheus
```

## 🐛 Troubleshooting

### Order not created

**Check:**
1. Kafka đang chạy?
   ```bash
   kafka-topics.sh --bootstrap-server localhost:9092 --list
   ```

2. Headers có đúng không?
   ```bash
   curl -v http://localhost:8082/order-service/api/v1/orders \
     -H "X-User-Id: test-user-id"
   ```

3. Check logs:
   ```bash
   tail -f logs/order-service.log
   ```

### Payment event not consumed

**Check:**
1. Consumer group active?
   ```bash
   kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
     --describe --group order-service-group
   ```

2. Topic có messages không?
   ```bash
   kafka-console-consumer.sh --bootstrap-server localhost:9092 \
     --topic payment-events --from-beginning
   ```

## 📚 Related Documentation

- [API Gateway](../api-gateway/README.md)
- [Phase 2 Summary](./PHASE2_SUMMARY.md)
- [Architecture](../ARCHITECTURE.md)

---

**Version**: 1.0.0
**Last Updated**: 2025-05-20
**Status**: Event-Driven Refactor Complete
