# Order Service - Phase 3: Event-Driven Order Creation

## 🎯 What Changed in Phase 3?

Phase 3 chuyển đổi luồng **Tạo đơn hàng** từ kiến trúc Synchronous (chồng đợi) sang **Asynchronous Event-Driven** (bất đồng bộ).

### Key Changes

| Aspect | Before (Phase 2) | After (Phase 3) |
|--------|------------------|-----------------|
| **Payment Processing** | Synchronous (blocking) | Asynchronous (Kafka) |
| **Response Time** | 3-6 seconds | 100-200ms |
| **Payment Service Coupling** | Tight (direct HTTP) | Loose (Kafka events) |
| **User ID Source** | From JWT token | From Gateway headers |
| **Transaction Handling** | Single transaction | Transaction + Event Publishing |

---

## 🔄 New Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                        Client Application                        │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             │ POST /api/v1/orders
                             │ Headers: X-User-Id, X-User-Name
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                       API Gateway (8080)                         │
│  • Validate JWT                                                 │
│  • Inject: X-User-Id, X-User-Name, X-User-Fullname             │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             │ HTTP with Headers
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Order Service (8082)                          │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  UserHeaderInterceptor                                  │   │
│  │  • Extract X-User-Id, X-User-Name, X-User-Fullname      │   │
│  │  • Store in UserContextHolder (ThreadLocal)             │   │
│  └─────────────────────────────────────────────────────────┘   │
│                              │                                   │
│                              ▼                                   │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  CreateOrderCommand                                     │   │
│  │  • Validate request                                       │   │
│  │  • Get userId from SecurityHelper                         │   │
│  │  • Create Order entity (status: PENDING)                 │   │
│  │  • Calculate totals (items, discount, tax, shipping)     │   │
│  │  • Save order to database (@Transactional)               │   │
│  │  • Publish OrderCreatedEvent (after commit)              │   │
│  │  • Return OrderResponse IMMEDIATELY                       │   │
│  └─────────────────────────────────────────────────────────┘   │
│                              │                                   │
│                              └───────────────────────┐         │
│                                                      │         │
│                            Response (100-200ms)      │         │
│                                    │                   │         │
└────────────────────────────────────┼───────────────────┐         │
                                     │                   │         │
                                     ▼                   │         │
                              ┌─────────────┐           │         │
                              │   Client     │           │         │
                              │  Received    │           │         │
                              │  Order: PENDING│           │         │
                              └─────────────┘           │         │
                                                          │         │
                                                          │         │
┌─────────────────────────────────────────────────────────────────┤
│                    BACKGROUND (Async Processing)                  │
│                                                                  │
│  Transaction Commit ───────────────┐                            │
│                                     │                            │
│                                     ▼                            │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  OrderEventListener.handleOrderCreatedEvent()             │   │
│  │  • Publish OrderCreatedEvent to Kafka (async)            │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                     │                            │
│                                     ▼                            │
│                              ┌─────────┐                       │
│                              │  Kafka  │                       │
│                              │   Topic │                       │
│                              │  order- │                       │
│                              │  events │                       │
│                              └────┬────┘                       │
│                                   │                            │
│                                   ▼                            │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │              Payment Service (8083)                       │   │
│  │  • Consume OrderCreatedEvent                              │   │
│  │  • Process payment with provider                         │   │
│  │  • Publish PaymentSuccessEvent OR PaymentFailedEvent      │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                   │                            │
│                                   ▼                            │
│                              ┌─────────┐                       │
│                              │  Kafka  │                       │
│                              │   Topic │                       │
│                              │ payment-│                       │
│                              │  events │                       │
│                              └────┬────┘                       │
│                                   │                            │
│                                   ▼                            │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │        Order Service - PaymentEventConsumer              │   │
│  │  • Consume PaymentSuccessEvent / PaymentFailedEvent        │   │
│  │  • Update order status: PAID or PAYMENT_FAILED            │   │
│  │  • Publish OrderPaidEvent OR OrderPaymentFailedEvent       │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
```

---

## 📡 API Usage

### Create Order (Event-Driven)

```bash
POST /order-service/api/v1/orders
Headers:
  X-User-Id: user-123-uuid
  X-User-Name: john.doe
  X-User-Fullname: John Doe
  Content-Type: application/json

Body:
{
  "customerName": "John Doe",
  "customerEmail": "john@example.com",
  "customerPhone": "+1234567890",
  "shippingAddress": "123 Main St, New York, NY 10001",
  "billingAddress": "123 Main St, New York, NY 10001",
  "items": [
    {
      "productId": "prod-001",
      "productName": "Laptop Gaming Pro",
      "productSku": "LAPTOP-001",
      "productImage": "https://example.com/laptop.jpg",
      "quantity": 1,
      "unitPrice": 15000000,
      "discountAmount": 1000000,
      "taxAmount": 500000,
      "currency": "VND"
    }
  ],
  "discountAmount": 2000000,
  "taxAmount": 1000000,
  "shippingAmount": 500000,
  "currency": "VND",
  "paymentMethod": "CREDIT_CARD",
  "customerNotes": "Please deliver before 5 PM"
}
```

### Response (Immediate - 100-200ms)

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "orderNumber": "ORD-20240520103045-ABC12345",
  "userId": "user-123-uuid",
  "customerName": "John Doe",
  "customerEmail": "john@example.com",
  "customerPhone": "+1234567890",
  "status": 0,
  "statusName": "Pending",
  "statusDescription": "Đơn hàng chờ xử lý",
  "totalAmount": 15000000,
  "discountAmount": 3000000,
  "taxAmount": 1500000,
  "shippingAmount": 500000,
  "finalAmount": 14000000,
  "currency": "VND",
  "shippingAddress": "123 Main St, New York, NY 10001",
  "billingAddress": "123 Main St, New York, NY 10001",
  "customerNotes": "Please deliver before 5 PM",
  "orderDate": "2024-05-20T10:30:45",
  "paymentStatus": 0,
  "paymentStatusName": "Pending",
  "paymentStatusDescription": "Chờ thanh toán",
  "paymentMethod": "CREDIT_CARD",
  "transactionId": null,
  "paymentDate": null,
  "items": [
    {
      "id": "item-123-uuid",
      "productId": "prod-001",
      "productName": "Laptop Gaming Pro",
      "quantity": 1,
      "unitPrice": 15000000,
      "discountAmount": 1000000,
      "taxAmount": 500000,
      "totalPrice": 14500000,
      "currency": "VND"
    }
  ],
  "createdBy": "user-123-uuid",
  "createdDate": "2024-05-20T10:30:45"
}
```

**Note:** Response trả về **ngay lập tức** với status `PENDING`. Payment sẽ được xử lý background.

---

## 🔍 Order Status Lifecycle

```
1. PENDING (Initial)
   ↓
   [Payment Processing - Background via Kafka]
   ↓
   ┌─────────────┬──────────────┐
   │             │              │
Success       Failure       Timeout
   │             │              │
   ▼             ▼              ▼
PAID        PAYMENT_FAILED  PAYMENT_FAILED
(retryable)    (retryable)   (not retryable)
```

### Check Order Status (Later)

```bash
GET /order-service/api/v1/orders/{orderId}
Headers:
  X-User-Id: user-123-uuid
  X-User-Name: john.doe
```

**Possible Responses:**

#### 1. Still Pending
```json
{
  "id": "...",
  "status": 0,
  "statusName": "Pending",
  "paymentStatus": 0,
  "paymentStatusName": "Pending"
}
```

#### 2. Payment Success
```json
{
  "id": "...",
  "status": 2,
  "statusName": "Paid",
  "paymentStatus": 2,
  "paymentStatusName": "Paid",
  "transactionId": "txn-stripe-12345",
  "paymentDate": "2024-05-20T10:31:30"
}
```

#### 3. Payment Failed
```json
{
  "id": "...",
  "status": 5,
  "statusName": "Cancelled",
  "paymentStatus": 3,
  "paymentStatusName": "Failed",
  "paymentFailureReason": "Insufficient funds",
  "transactionId": null
}
```

---

## 🧪 Testing

### Unit Tests

```bash
# Run CreateOrderCommand tests
mvn test -Dtest=CreateOrderCommandTest

# Run all tests
mvn test
```

### Manual Testing with Kafka

#### 1. Start Kafka

```bash
# Using Docker Compose
cd d:/1.Project/microservice
docker-compose up -d kafka zookeeper

# Or local Kafka
bin/kafka-server-start.sh config/server.properties
```

#### 2. Verify Topics

```bash
# List topics
kafka-topics.sh --bootstrap-server localhost:9092 --list

# Expected output:
# order-events
# payment-events
```

#### 3. Create Order

```bash
curl -X POST http://localhost:8082/order-service/api/v1/orders \
  -H "Content-Type: application/json" \
  -H "X-User-Id: user-123" \
  -H "X-User-Name: john.doe" \
  -d '{
    "customerName": "John Doe",
    "shippingAddress": "123 Main St",
    "items": [{
      "productId": "prod-001",
      "productName": "Laptop",
      "quantity": 1,
      "unitPrice": 1000000
    }],
    "paymentMethod": "CREDIT_CARD"
  }'
```

#### 4. Check Kafka Topic

```bash
# Consume from order-events topic
kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic order-events \
  --from-beginning \
  --property print.key=true \
  --property print.value=true

# Expected output:
# Key: order-123-uuid
# Value: {"eventId":"evt-456","eventType":"ORDER_CREATED",...}
```

#### 5. Check Database

```sql
-- Check order status
SELECT 
    id, 
    order_number, 
    status, 
    payment_status, 
    final_amount,
    created_at
FROM orders 
WHERE id = 'order-123-uuid';

-- Expected: status=0 (PENDING), payment_status=0 (PENDING)
```

---

## 🔧 Configuration

### application.yml

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      acks: all
      enable-idempotence: true
      compression-type: snappy
    consumer:
      group-id: order-service-group
      auto-offset-reset: earliest
      enable-auto-commit: false
    listener:
      concurrency: 3
      ack-mode: manual_immediate
```

### Async Configuration

```java
@Configuration
@EnableAsync
public class AsyncConfiguration {
    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("order-async-");
        executor.initialize();
        return executor;
    }
}
```

---

## 📊 Monitoring

### Order Metrics

```bash
# Actuator metrics
curl http://localhost:8082/order-service/actuator/metrics/order.created

# Kafka consumer lag
curl http://localhost:8082/order-service/actuator/kafka/consumer/lag
```

### Logs

```
2024-05-20 10:30:45.123 INFO  Creating order for user: john.doe (user-123-uuid)
2024-05-20 10:30:45.456 INFO  Order created successfully: order-123 | Order Number: ORD-20240520103045-ABC12345 | Final Amount: 14000000 VND
2024-05-20 10:30:45.789 INFO  Transaction committed, publishing OrderCreatedEvent for order: order-123
2024-05-20 10:30:45.890 INFO  OrderCreatedEvent published to Kafka successfully: evt-456 | Order: order-123
```

---

## 🎓 Best Practices Applied

### 1. Transactional Event Publishing

```java
// ✅ DO: Publish after transaction commit
@Transactional
public void execute(CreateOrderRequest request) {
    Order order = saveOrder(request);
    
    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
        @Override
        public void afterCommit() {
            publishEvent(order);  // Only publish if commit
        }
    });
    
    return response;  // Return immediately
}

// ❌ DON'T: Publish before transaction commit
@Transactional
public void execute(CreateOrderRequest request) {
    Order order = saveOrder(request);
    publishEvent(order);  // May publish even if rollback!
    return response;
}
```

### 2. Idempotency

```java
// ✅ DO: Check for duplicate events
public void handlePaymentSuccess(PaymentSuccessEvent event) {
    Order order = orderRepository.findById(event.getOrderId());
    
    if (order.getPaymentStatus().isPaid()) {
        log.warn("Order already paid: {}", order.getId());
        return;  // Skip duplicate
    }
    
    order.markAsPaid(event.getTransactionId());
    orderRepository.save(order);
}

// ❌ DON'T: Process duplicate events
public void handlePaymentSuccess(PaymentSuccessEvent event) {
    Order order = orderRepository.findById(event.getOrderId());
    order.markAsPaid(event.getTransactionId());  // May process twice!
    orderRepository.save(order);
}
```

### 3. Async Processing

```java
// ✅ DO: Use @Async for event publishing
@TransactionalEventListener(phase = AFTER_COMMIT)
@Async
public void handleOrderCreatedEvent(OrderCreatedEvent event) {
    kafkaTemplate.send("order-events", event);
}

// ❌ DON'T: Block main thread
@TransactionalEventListener(phase = AFTER_COMMIT)
public void handleOrderCreatedEvent(OrderCreatedEvent event) {
    kafkaTemplate.send("order-events", event).get();  // Blocking!
}
```

---

## 🐛 Troubleshooting

### Event Not Published to Kafka

**Symptom:** Order created but no event in Kafka

**Check:**
1. Transaction committed?
   ```bash
   # Check database
   SELECT * FROM orders WHERE order_number = 'ORD-...';
   ```

2. Kafka running?
   ```bash
   kafka-topics.sh --bootstrap-server localhost:9092 --list
   ```

3. Check logs
   ```bash
   tail -f logs/order-service.log | grep "OrderCreatedEvent"
   ```

### Payment Event Not Consumed

**Symptom:** Payment sent but order status not updated

**Check:**
1. Consumer running?
   ```bash
   kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
     --describe --group order-service-group
   ```

2. Topic has messages?
   ```bash
   kafka-console-consumer.sh --bootstrap-server localhost:9092 \
     --topic payment-events --from-beginning
   ```

---

## 📚 Related Documentation

- [Phase 2 Summary](./PHASE2_SUMMARY.md) - Gateway Pattern
- [Phase 3 Summary](./PHASE3_SUMMARY.md) - Event-Driven Order Creation
- [Architecture](../ARCHITECTURE.md) - Overall Architecture
- [API Gateway](../api-gateway/README.md) - Gateway Configuration

---

**Version**: 2.0.0 (Event-Driven)
**Last Updated**: 2025-05-20
**Status**: Phase 3 Complete
**Next**: Phase 4 - Payment Event Consumer Implementation
