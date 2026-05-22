# Verification Steps - End-to-End Order-Payment Saga Testing

## Prerequisites

### 1. Start All Services
```bash
# Terminal 1: API Gateway (Port 8080)
cd api-gateway
mvn spring-boot:run

# Terminal 2: Order Service (Port 8082)
cd order-service
mvn spring-boot:run

# Terminal 3: Payment Service (Port 8083)
cd payment-service
mvn spring-boot:run

# Terminal 4: Kafka (if not running)
# Kafka should be running on localhost:9092
kafka-server-start.bat ...\config\server.properties
```

### 2. Verify Services are Running
```bash
# Check API Gateway
curl http://localhost:8080/actuator/health

# Check Order Service
curl http://localhost:8082/actuator/health

# Check Payment Service
curl http://localhost:8083/actuator/health
```

---

## Test Scenario 1: Successful Payment Flow (80% success rate)

### Step 1: User Login - Get JWT Token
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "password123"
  }'
```

**Expected Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": "user-uuid-12345",
  "username": "john_doe",
  "fullname": "John Doe",
  "roles": ["USER"]
}
```

**Save the JWT token for next steps:**
```bash
JWT_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### Step 2: Create Order via API Gateway
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "customerName": "John Doe",
    "customerEmail": "john.doe@example.com",
    "customerPhone": "+1234567890",
    "shippingAddress": "123 Main St, New York, NY 10001",
    "paymentMethod": "CREDIT_CARD",
    "items": [
      {
        "productId": "prod-001",
        "productName": "Laptop Gaming",
        "productSku": "LAPTOP-001",
        "quantity": 1,
        "unitPrice": 1500.00,
        "currency": "USD"
      },
      {
        "productId": "prod-002",
        "productName": "Wireless Mouse",
        "productSku": "MOUSE-001",
        "quantity": 2,
        "unitPrice": 50.00,
        "currency": "USD"
      }
    ]
  }'
```

**Expected Immediate Response (100-200ms):**
```json
{
  "status": 200,
  "message": "Order created successfully",
  "data": {
    "orderId": "order-uuid-67890",
    "orderNumber": "ORD-20260522123456-ABC12345",
    "status": "PENDING",
    "paymentStatus": "PENDING",
    "totalAmount": 1600.00,
    "finalAmount": 1600.00,
    "currency": "USD",
    "createdAt": "2026-05-22T12:34:56"
  }
}
```

**Note:** Order được tạo ngay lập tức với status `PENDING`.

### Step 3: Monitor Order Status (Polling)
```bash
# Check order status immediately
ORDER_ID="order-uuid-67890"

curl http://localhost:8080/api/orders/$ORDER_ID \
  -H "Authorization: Bearer $JWT_TOKEN"
```

**Expected Response (Immediately after creation):**
```json
{
  "orderId": "order-uuid-67890",
  "orderNumber": "ORD-20260522123456-ABC12345",
  "status": "PENDING",
  "paymentStatus": "PENDING",
  "transactionId": null,
  "paymentDate": null,
  "paymentFailureReason": null
}
```

### Step 4: Wait 2-3 seconds and Check Again
```bash
# Wait for payment processing (simulation takes 2 seconds)
sleep 3

# Check order status again
curl http://localhost:8080/api/orders/$ORDER_ID \
  -H "Authorization: Bearer $JWT_TOKEN"
```

**Expected Response (80% success rate):**
```json
{
  "orderId": "order-uuid-67890",
  "orderNumber": "ORD-20260522123456-ABC12345",
  "status": "CONFIRMED",
  "paymentStatus": "PAID",
  "transactionId": "TXN-20260522123456-XYZ12345",
  "paymentDate": "2026-05-22T12:35:00",
  "paymentFailureReason": null
}
```

**Behind the Scenes:**
1. Order Service saves Order with status `PENDING`
2. Order Service publishes `OrderCreatedEvent` → Kafka
3. Payment Service consumes `OrderCreatedEvent`
4. Payment Service creates Payment record (`PROCESSING`)
5. Payment Service processes payment (80% success rate)
6. Payment Service publishes `PaymentProcessedEvent` (success=true)
7. Order Service consumes `PaymentProcessedEvent`
8. Order Service updates Order status → `PAID`
9. Order Service saves Transaction ID

---

## Test Scenario 2: Failed Payment Flow (20% failure rate)

### Step 1: Create Another Order
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "customerName": "Jane Smith",
    "customerEmail": "jane.smith@example.com",
    "customerPhone": "+9876543210",
    "shippingAddress": "456 Oak Ave, Los Angeles, CA 90001",
    "paymentMethod": "VNPAY",
    "items": [
      {
        "productId": "prod-003",
        "productName": "Smartphone",
        "productSku": "PHONE-001",
        "quantity": 1,
        "unitPrice": 800.00,
        "currency": "USD"
      }
    ]
  }'
```

**Expected Response:**
```json
{
  "status": 200,
  "message": "Order created successfully",
  "data": {
    "orderId": "order-uuid-failed-test",
    "orderNumber": "ORD-20260522124001-DEF67890",
    "status": "PENDING",
    "paymentStatus": "PENDING",
    "finalAmount": 800.00
  }
}
```

### Step 2: Wait and Check Status
```bash
ORDER_ID_2="order-uuid-failed-test"

sleep 3

curl http://localhost:8080/api/orders/$ORDER_ID_2 \
  -H "Authorization: Bearer $JWT_TOKEN"
```

**Expected Response (20% failure rate):**
```json
{
  "orderId": "order-uuid-failed-test",
  "orderNumber": "ORD-20260522124001-DEF67890",
  "status": "PENDING",
  "paymentStatus": "FAILED",
  "transactionId": null,
  "paymentDate": null,
  "paymentFailureReason": "Transaction declined by bank - Error Code: TRANSACTION_DECLINED - Retryable"
}
```

**Behind the Scenes (Failed Flow):**
1. Order Service saves Order with status `PENDING`
2. Order Service publishes `OrderCreatedEvent` → Kafka
3. Payment Service consumes `OrderCreatedEvent`
4. Payment Service creates Payment record (`PROCESSING`)
5. Payment Service processes payment (20% failure)
6. Payment Service publishes `PaymentProcessedEvent` (success=false, reason="Transaction declined by bank")
7. Order Service consumes `PaymentProcessedEvent`
8. Order Service updates Order status → `FAILED`
9. Order Service saves failure reason

---

## Test Scenario 3: Query All Orders for User

```bash
curl "http://localhost:8080/api/orders?userId=$USER_ID" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

**Expected Response:**
```json
{
  "status": 200,
  "message": "Orders retrieved successfully",
  "data": {
    "orders": [
      {
        "orderId": "order-uuid-67890",
        "orderNumber": "ORD-20260522123456-ABC12345",
        "status": "CONFIRMED",
        "paymentStatus": "PAID",
        "finalAmount": 1600.00,
        "transactionId": "TXN-20260522123456-XYZ12345"
      },
      {
        "orderId": "order-uuid-failed-test",
        "orderNumber": "ORD-20260522124001-DEF67890",
        "status": "PENDING",
        "paymentStatus": "FAILED",
        "finalAmount": 800.00,
        "paymentFailureReason": "Transaction declined by bank"
      }
    ],
    "total": 2
  }
}
```

---

## Test Scenario 4: Database Verification

### Check Orders Table Directly
```bash
# Connect to MySQL
mysql -u root -p payment_service

# View all orders
SELECT order_id, order_number, status, payment_status, transaction_id, payment_date, payment_failure_reason
FROM orders
ORDER BY created_at DESC
LIMIT 5;
```

**Expected Output:**
```sql
+--------------------------------------+---------------------------+--------+---------------+-------------------+---------------------+---------------------------+
| order_id                             | order_number               | status | payment_status | transaction_id    | payment_date        | payment_failure_reason     |
+--------------------------------------+---------------------------+--------+---------------+-------------------+---------------------+---------------------------+
| order-uuid-67890                    | ORD-20260522...-ABC12345 |      2 |             1 | TXN-20260522...XYZ | 2026-05-22 12:35:00 | NULL                      |
| order-uuid-failed-test               | ORD-20260522...-DEF67890 |      0 |             2 | NULL              | NULL                | Transaction declined...    |
+--------------------------------------+---------------------------+--------+---------------+-------------------+---------------------+---------------------------+
```

### Check Payment Service Database
```bash
mysql -u root -p payment_service_db

SELECT payment_id, order_id, payment_number, payment_status, amount, transaction_id, failure_reason
FROM payments
ORDER BY created_at DESC
LIMIT 5;
```

**Expected Output:**
```sql
+--------------------------------------+--------------------------------+---------------------------+---------------+--------+--------------------------+----------------------------+
| payment_id                           | order_id                       | payment_number             | payment_status | amount  | transaction_id           | failure_reason             |
+--------------------------------------+--------------------------------+---------------------------+---------------+--------+--------------------------+----------------------------+
| payment-uuid-success                 | order-uuid-67890              | PAY-20260522...-ABC12345 | PAID          | 1600.00| TXN-20260522...XYZ | NULL                       |
| payment-uuid-failed                 | order-uuid-failed-test       | PAY-20260522...-DEF67890 | FAILED        | 800.00 | NULL                     | Transaction declined by bank |
+--------------------------------------+--------------------------------+---------------------------+---------------+--------+--------------------------+----------------------------+
```

---

## Test Scenario 5: Check Kafka Messages (Optional)

### Consume from order-events topic
```bash
kafka-console-consumer.bat \
  --bootstrap-server localhost:9092 \
  --topic order-events \
  --from-beginning \
  --property print.key=true \
  --property print.value=true \
  --property key.separator=" | "
```

### Consume from payment-events topic
```bash
kafka-console-consumer.bat \
  --bootstrap-server localhost:9092 \
  --topic payment-events \
  --from-beginning \
  --property print.key=true \
  --property print.value=true \
  --property key.separator=" | "
```

**Expected order-events messages:**
```json
key: "order-uuid-67890"
value: {
  "eventId": "event-uuid-001",
  "eventType": "ORDER_CREATED",
  "aggregateId": "order-uuid-67890",
  "orderId": "order-uuid-67890",
  "customerId": "user-uuid-12345",
  "finalAmount": 1600.00,
  "paymentMethod": "CREDIT_CARD"
}
```

**Expected payment-events messages:**
```json
key: "order-uuid-67890"
value: {
  "eventId": "event-uuid-002",
  "eventType": "PAYMENT_SUCCESS",
  "aggregateId": "payment-uuid-success",
  "orderId": "order-uuid-67890",
  "paymentId": "payment-uuid-success",
  "transactionId": "TXN-20260522...XYZ",
  "paidAmount": 1600.00,
  "success": true
}
```

---

## Timing Verification

### Expected Timeline
```
T+0ms:    User calls POST /api/orders
T+100ms:  Order created (PENDING) + response returned to user
T+150ms:  OrderCreatedEvent published to Kafka
T+200ms:  Payment Service consumes OrderCreatedEvent
T+250ms:  Payment record created (PROCESSING)
T+250ms:  Payment processing starts (simulation)
T+2250ms: Payment processing completes (2 seconds simulation)
T+2300ms: PaymentProcessedEvent published to Kafka
T+2350ms: Order Service consumes PaymentProcessedEvent
T+2400ms: Order status updated (PAID or FAILED)
T+2450ms: OrderPaidEvent/OrderPaymentFailedEvent published
```

**Total time: ~2.5 seconds** (vs 6+ seconds for synchronous payment)

---

## Success Criteria

✅ **Order created immediately** (100-200ms response time)
✅ **No blocking on main thread** - user gets response immediately
✅ **Payment processed asynchronously** (2-3 seconds)
✅ **Order status auto-updated** to PAID (80%) or FAILED (20%)
✅ **Transaction ID saved** when successful
✅ **Failure reason saved** when failed
✅ **Idempotency maintained** - duplicate events don't cause double updates
✅ **End-to-end tracing** - traceId flows through all services

---

## Troubleshooting

### Order stays in PENDING forever
```bash
# Check Kafka topics
kafka-topics.bat --bootstrap-server localhost:9092 --list

# Check consumer lag
kafka-consumer-groups.bat --bootstrap-server localhost:9092 \
  --group payment-service-group --describe

# Check logs
# Payment Service logs
tail -f payment-service/logs/app.log

# Order Service logs  
tail -f order-service/logs/app.log
```

### Payment always succeeds or always fails
```bash
# Check payment simulation settings
curl http://localhost:8083/actuator/configprops | jq '.contexts.payment-service.beans.paymentSimulation'
```

**Adjust success rate in application.yml:**
```yaml
payment:
  simulation:
    success-rate: 0.5  # 50% success for testing
```

### Kafka connection errors
```bash
# Verify Kafka is running
kafka-topics.bat --bootstrap-server localhost:9092 --list

# Check network connectivity
telnet localhost 9092
```

---

## Performance Metrics

### Expected Throughput
- **Order creation**: 100-200ms (API Gateway + Order Service)
- **Payment processing**: 2-3 seconds (async, non-blocking)
- **Status update**: <100ms (Order Service consumes event)
- **Total end-to-end**: ~2.5 seconds (vs 6+ seconds sync)

### Concurrency Testing
```bash
# Create 10 orders concurrently
for i in {1..10}; do
  curl -X POST http://localhost:8080/api/orders \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $JWT_TOKEN" \
    -d "{
      \"customerName\": \"Test User $i\",
      \"customerEmail\": \"test$i@example.com\",
      \"customerPhone\": \"+123456789\"",
      \"shippingAddress\": \"Address $i\",
      \"paymentMethod\": \"CREDIT_CARD\",
      \"items\": [{
        \"productId\": \"prod-00$i\",
        \"productName\": \"Product $i\",
        \"productSku\": \"SKU-00$i\",
        \"quantity\": 1,
        \"unitPrice\": 100.00,
        \"currency\": \"USD\"
      }]
    }" &
done
wait
echo "All orders created. Checking status..."
```

This completes the Choreography Saga implementation for Order-Payment flow!
