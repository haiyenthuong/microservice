# API Guidelines - Microservices E-Commerce System

## Table of Contents

- [Quick Start](#quick-start)
- [Authentication](#authentication)
- [API Endpoints](#api-endpoints)
- [Request/Response Examples](#requestresponse-examples)
- [Error Handling](#error-handling)
- [Event Flows](#event-flows)
- [Testing with cURL](#testing-with-curl)

---

## Quick Start

### Base URL
```
Development: http://localhost:8080
```

### Prerequisites
1. **Start Kafka** (port 9092)
2. **Start MySQL** with 3 databases:
   - `api_gateway_db`
   - `order_service_db`
   - `payment_service_db`
3. **Start Services**:
   - API Gateway (port 8080)
   - Order Service (port 8082)
   - Payment Service (port 8083)

---

## Authentication

### Get JWT Token

**Endpoint:** `POST /api/auth/login`

**Request:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "password123"
  }'
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqb2huX2RvZSIsInVzZXJJZCI6InVzZXItdXVpZC0xMjMiLCJmdWxsbmFtZSI6IkpvaG4gRG9lIiwicm9sZXMiOlsiVVTRVIiXSwiaWF0IjoxNzE2MzkyMDk2LCJleHAiOjE3MTYzOTU2OTZ9.signature",
  "userId": "user-uuid-12345",
  "username": "john_doe",
  "fullname": "John Doe",
  "roles": ["USER"]
}
```

**Error Response (401 Unauthorized):**
```json
{
  "timestamp": 1716392096123,
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid username or password",
  "path": "/api/auth/login"
}
```

### Use JWT Token

Include the token in subsequent requests:

```bash
curl http://localhost:8080/api/orders \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

## API Endpoints

### 1. Order APIs

#### 1.1 Create Order

**Endpoint:** `POST /api/orders`

**Authentication:** Required (JWT Bearer Token)

**Request:**
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "customerName": "John Doe",
    "customerEmail": "john.doe@example.com",
    "customerPhone": "+1234567890",
    "shippingAddress": "123 Main St, New York, NY 10001",
    "billingAddress": "123 Main St, New York, NY 10001",
    "paymentMethod": "CREDIT_CARD",
    "currency": "USD",
    "discountAmount": 0,
    "taxAmount": 0,
    "shippingAmount": 10,
    "items": [
      {
        "productId": "prod-001",
        "productName": "Laptop Gaming",
        "productSku": "LAPTOP-001",
        "quantity": 1,
        "unitPrice": 1500,
        "currency": "USD"
      },
      {
        "productId": "prod-002",
        "productName": "Wireless Mouse",
        "productSku": "MOUSE-001",
        "quantity": 2,
        "unitPrice": 50,
        "currency": "USD"
      }
    ]
  }'
```

**Response (200 OK):**
```json
{
  "status": 200,
  "message": "Order created successfully",
  "data": {
    "id": "order-uuid-12345",
    "orderNumber": "ORD-20260522123456-ABC12345",
    "userId": "user-uuid-67890",
    "customerName": "John Doe",
    "customerEmail": "john.doe@example.com",
    "customerPhone": "+1234567890",
    "shippingAddress": "123 Main St, New York, NY 10001",
    "billingAddress": "123 Main St, New York, NY 10001",
    "status": 0,
    "statusName": "PENDING",
    "statusDescription": "Order created, waiting for payment",
    "totalAmount": 1600,
    "discountAmount": 0,
    "taxAmount": 0,
    "shippingAmount": 10,
    "finalAmount": 1610,
    "currency": "USD",
    "orderDate": "2026-05-22T12:34:56Z",
    "paymentStatus": 0,
    "paymentStatusName": "PENDING",
    "paymentStatusDescription": "Payment pending",
    "paymentMethod": "CREDIT_CARD",
    "transactionId": null,
    "paymentDate": null,
    "items": [
      {
        "id": "item-uuid-001",
        "productId": "prod-001",
        "productName": "Laptop Gaming",
        "quantity": 1,
        "unitPrice": 1500,
        "totalPrice": 1500,
        "currency": "USD"
      },
      {
        "id": "item-uuid-002",
        "productId": "prod-002",
        "productName": "Wireless Mouse",
        "quantity": 2,
        "unitPrice": 50,
        "totalPrice": 100,
        "currency": "USD"
      }
    ]
  }
}
```

**Timeline:**
- `T+0ms`: Request received
- `T+100-200ms`: Order created with status `PENDING`
- `T+200ms`: Response returned to user
- `T+2-3s`: Payment processed asynchronously

#### 1.2 Get Order by ID

**Endpoint:** `GET /api/orders/{orderId}`

**Authentication:** Required

**Request:**
```bash
curl http://localhost:8080/api/orders/order-uuid-12345 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response (200 OK):**
```json
{
  "status": 200,
  "message": "Order retrieved successfully",
  "data": {
    "id": "order-uuid-12345",
    "orderNumber": "ORD-20260522123456-ABC12345",
    "status": 2,
    "statusName": "PAID",
    "paymentStatus": 1,
    "paymentStatusName": "PAID",
    "transactionId": "TXN-20260522123500-XYZ12345",
    "finalAmount": 1610,
    "currency": "USD"
  }
}
```

#### 1.3 List Orders

**Endpoint:** `GET /api/orders`

**Authentication:** Required

**Query Parameters:**
- `page` (optional, default: 0)
- `size` (optional, default: 10)
- `sort` (optional, default: createdDate,desc)

**Request:**
```bash
curl "http://localhost:8080/api/orders?page=0&size=10" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response (200 OK):**
```json
{
  "status": 200,
  "message": "Orders retrieved successfully",
  "data": {
    "orders": [
      {
        "id": "order-uuid-12345",
        "orderNumber": "ORD-20260522123456-ABC12345",
        "finalAmount": 1610,
        "status": 2,
        "paymentStatus": 1,
        "createdDate": "2026-05-22T12:34:56Z"
      }
    ],
    "pagination": {
      "page": 0,
      "size": 10,
      "totalElements": 25,
      "totalPages": 3,
      "first": true,
      "last": false
    }
  }
}
```

### 2. Payment APIs

#### 2.1 Get Payment by ID

**Endpoint:** `GET /api/payments/{paymentId}`

**Authentication:** Required

**Request:**
```bash
curl http://localhost:8080/api/payments/payment-uuid-12345 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response (200 OK):**
```json
{
  "status": 200,
  "message": "Payment retrieved successfully",
  "data": {
    "id": "payment-uuid-12345",
    "paymentNumber": "PAY-20260522123500-DEF67890",
    "orderId": "order-uuid-12345",
    "orderNumber": "ORD-20260522123456-ABC12345",
    "amount": 1610,
    "currency": "USD",
    "paymentStatus": "PAID",
    "transactionId": "TXN-20260522123500-XYZ12345",
    "paymentDate": "2026-05-22T12:35:05Z"
  }
}
```

---

## Request/Response Examples

### Complete Order Creation Flow

#### Step 1: Login and Get JWT Token

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "password123"
  }'
```

**Save the token:**
```bash
JWT_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### Step 2: Create Order

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
    "currency": "USD",
    "items": [
      {
        "productId": "prod-003",
        "productName": "Smartphone",
        "productSku": "PHONE-001",
        "quantity": 1,
        "unitPrice": 800,
        "currency": "USD"
      }
    ]
  }'
```

**Immediate Response (100-200ms):**
```json
{
  "status": 200,
  "message": "Order created successfully",
  "data": {
    "id": "order-uuid-failed-test",
    "orderNumber": "ORD-20260522124001-DEF67890",
    "status": 0,
    "statusName": "PENDING",
    "paymentStatus": 0,
    "paymentStatusName": "PENDING",
    "finalAmount": 800
  }
}
```

#### Step 3: Check Order Status (Polling)

```bash
# Check immediately
ORDER_ID="order-uuid-failed-test"
curl http://localhost:8080/api/orders/$ORDER_ID \
  -H "Authorization: Bearer $JWT_TOKEN"
```

**Response (Immediately):**
```json
{
  "status": "PENDING",
  "paymentStatus": "PENDING",
  "transactionId": null
}
```

```bash
# Wait 3 seconds and check again
sleep 3
curl http://localhost:8080/api/orders/$ORDER_ID \
  -H "Authorization: Bearer $JWT_TOKEN"
```

**Response (After payment processed - 80% success):**
```json
{
  "status": 2,
  "statusName": "PAID",
  "paymentStatus": 1,
  "paymentStatusName": "PAID",
  "transactionId": "TXN-20260522124010-XYZ99999",
  "paymentDate": "2026-05-22T12:40:10Z",
  "paymentFailureReason": null
}
```

**OR (20% failure):**
```json
{
  "status": 0,
  "statusName": "PENDING",
  "paymentStatus": 2,
  "paymentStatusName": "FAILED",
  "transactionId": null,
  "paymentDate": null,
  "paymentFailureReason": "Transaction declined by bank - Error Code: TRANSACTION_DECLINED - Retryable"
}
```

---

## Error Handling

### Error Response Format

All errors follow this format:

```json
{
  "timestamp": 1716392096123,
  "status": 400,
  "error": "Bad Request",
  "message": "Human-readable error message",
  "path": "/api/orders"
}
```

### Common Error Codes

| Status | Error | Description |
|--------|-------|-------------|
| 400 | Bad Request | Invalid request parameters |
| 401 | Unauthorized | Missing or invalid JWT token |
| 403 | Forbidden | User doesn't have permission |
| 404 | Not Found | Resource not found |
| 500 | Internal Server Error | Server error |

### Example Error Responses

**400 Bad Request - Missing Required Field:**
```json
{
  "timestamp": 1716392096123,
  "status": 400,
  "error": "Bad Request",
  "message": "Order items cannot be empty",
  "path": "/api/orders"
}
```

**401 Unauthorized - Missing Token:**
```json
{
  "timestamp": 1716392096123,
  "status": 401,
  "error": "Unauthorized",
  "message": "Authorization header is required",
  "path": "/api/orders"
}
```

**401 Unauthorized - Invalid Token:**
```json
{
  "timestamp": 1716392096123,
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid or expired JWT token",
  "path": "/api/orders"
}
```

**404 Not Found:**
```json
{
  "timestamp": 1716392096123,
  "status": 404,
  "error": "Not Found",
  "message": "Order not found with ID: order-uuid-invalid",
  "path": "/api/orders/order-uuid-invalid"
}
```

---

## Event Flows

### Order Creation & Payment Flow

```
┌─────────────────────────────────────────────────────────────────────────┐
│ 1. USER REQUEST                                                              │
│    POST /api/orders with JWT token                                        │
│    ↓                                                                         │
│ 2. API GATEWAY                                                               │
│    - Validate JWT token                                                     │
│    - Extract user info (userId, username)                                  │
│    - Inject headers: X-User-Id, X-User-Name, X-User-Fullname                │
│    - Route to Order Service                                                │
│    ↓                                                                         │
│ 3. ORDER SERVICE                                                             │
│    - Receive request with user headers                                     │
│    - Create Order entity (status: PENDING)                                │
│    - Save to database (in transaction)                                     │
│    - Publish OrderCreatedEvent to Kafka (after commit)                     │
│    - Return response immediately (100-200ms)                               │
│    ↓                                                                         │
│ 4. KAFKA                                                                     │
│    Topic: order-events                                                      │
│    Message: OrderCreatedEvent                                               │
│    ↓                                                                         │
│ 5. PAYMENT SERVICE (Background)                                             │
│    - Consume OrderCreatedEvent                                             │
│    - Create Payment entity (status: PROCESSING)                            │
│    - Process payment (2-3 seconds simulation)                              │
│    - 80% success → Publish PaymentSuccessEvent                             │
│    - 20% failure → Publish PaymentFailedEvent                              │
│    ↓                                                                         │
│ 6. KAFKA                                                                     │
│    Topic: payment-events                                                    │
│    Message: PaymentSuccessEvent OR PaymentFailedEvent                      │
│    ↓                                                                         │
│ 7. ORDER SERVICE (Consumer)                                                │
│    - Consume payment event                                                 │
│    - Update Order status:                                                  │
│      * PAID (if success)                                                   │
│      * FAILED (if failed)                                                 │
│    - Save to database                                                      │
│    - Publish result event (OrderPaidEvent or OrderPaymentFailedEvent)       │
│    ↓                                                                         │
│ 8. USER (Polling)                                                           │
│    GET /api/orders/{orderId}                                               │
│    - Check payment status                                                  │
│    - See final order status                                                │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## Testing with cURL

### Test Complete Flow

```bash
#!/bin/bash

# 1. Login and get token
echo "=== 1. Login ==="
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "password123"
  }')

echo "$LOGIN_RESPONSE" | jq .

# Extract token
JWT_TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.token')
echo "JWT Token: $JWT_TOKEN"
echo ""

# 2. Create Order
echo "=== 2. Create Order ==="
ORDER_RESPONSE=$(curl -s -X POST http://localhost:8080/api/orders \
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
        "unitPrice": 1500,
        "currency": "USD"
      }
    ]
  }')

echo "$ORDER_RESPONSE" | jq .

# Extract Order ID
ORDER_ID=$(echo "$ORDER_RESPONSE" | jq -r '.data.id')
echo "Order ID: $ORDER_ID"
echo ""

# 3. Check Order Status (Immediately)
echo "=== 3. Check Order Status (Immediately) ==="
curl -s http://localhost:8080/api/orders/$ORDER_ID \
  -H "Authorization: Bearer $JWT_TOKEN" | jq .
echo ""

# 4. Wait for payment processing
echo "=== 4. Waiting for payment processing... ==="
echo "Waiting 3 seconds..."
sleep 3

# 5. Check Order Status (After payment)
echo "=== 5. Check Order Status (After payment) ==="
curl -s http://localhost:8080/api/orders/$ORDER_ID \
  -H "Authorization: Bearer $JWT_TOKEN" | jq .
echo ""

echo "=== Test Complete ==="
```

### Test with Multiple Orders

```bash
#!/bin/bash

# Login
echo "Login..."
JWT_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "password123"
  }' | jq -r '.token')

echo "Token: $JWT_TOKEN"
echo ""

# Create 10 orders concurrently
echo "Creating 10 orders..."
for i in {1..10}; do
  curl -s -X POST http://localhost:8080/api/orders \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $JWT_TOKEN" \
    -d "{
      \"customerName\": \"Test User $i\",
      \"customerEmail\": \"test$i@example.com\",
      \"customerPhone\": \"+1234567890\",
      \"shippingAddress\": \"Address $i\",
      \"paymentMethod\": \"CREDIT_CARD\",
      \"items\": [{
        \"productId\": \"prod-00$i\",
        \"productName\": \"Product $i\",
        \"productSku\": \"SKU-00$i\",
        \"quantity\": 1,
        \"unitPrice\": 100,
        \"currency\": \"USD\"
      }]
    }" &
done

wait
echo "All orders created!"
echo ""

# Check all orders
echo "Checking all orders..."
curl -s "http://localhost:8080/api/orders?page=0&size=10" \
  -H "Authorization: Bearer $JWT_TOKEN" | jq .
```

---

## Payment Status Flow

### Order Status Values

| Value | Status Name | Description |
|-------|-------------|-------------|
| 0 | PENDING | Order created, waiting for payment |
| 1 | CONFIRMED | Order confirmed |
| 2 | PAID | Payment successful |
| 3 | SHIPPED | Order shipped |
| 4 | DELIVERED | Order delivered |
| 5 | CANCELLED | Order cancelled |

### Payment Status Values

| Value | Status Name | Description |
|-------|-------------|-------------|
| 0 | PENDING | Payment pending |
| 1 | PAID | Payment successful |
| 2 | FAILED | Payment failed |

### Status Transitions

```
Order Creation:
  ORDER status: PENDING (0)
  PAYMENT status: PENDING (0)
     ↓
Payment Processing (Async):
  ├─ Success (80%):
  │   ORDER status: PAID (2)
  │   PAYMENT status: PAID (1)
  │   transactionId populated
  │
  └─ Failure (20%):
      ORDER status: PENDING (0)
      PAYMENT status: FAILED (2)
      paymentFailureReason populated
```

---

## Tips & Best Practices

### 1. Always Include JWT Token

```bash
# ✅ Correct
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/orders

# ❌ Wrong - Missing token
curl http://localhost:8080/api/orders
```

### 2. Handle PENDING Status

When creating an order, it starts with `PENDING` status. Poll to check payment completion:

```bash
ORDER_ID="order-uuid-12345"

# Check every 1 second, max 10 times
for i in {1..10}; do
  STATUS=$(curl -s http://localhost:8080/api/orders/$ORDER_ID \
    -H "Authorization: Bearer $TOKEN" | jq -r '.paymentStatusName')
  
  echo "Check $i: Payment status = $STATUS"
  
  if [ "$STATUS" != "PENDING" ]; then
    echo "Payment completed: $STATUS"
    break
  fi
  
  sleep 1
done
```

### 3. Handle Pagination

```bash
# First page
curl "http://localhost:8080/api/orders?page=0&size=10" \
  -H "Authorization: Bearer $TOKEN"

# Second page
curl "http://localhost:8080/api/orders?page=1&size=10" \
  -H "Authorization: Bearer $TOKEN"
```

### 4. Monitor Payment Events

You can consume Kafka topics directly to monitor events:

```bash
# Watch order-events
kafka-console-consumer.bat \
  --bootstrap-server localhost:9092 \
  --topic order-events \
  --from-beginning

# Watch payment-events  
kafka-console-consumer.bat \
  --bootstrap-server localhost:9092 \
  --topic payment-events \
  --from-beginning
```

---

## Summary

This microservices e-commerce system uses:

1. **API Gateway Pattern**: Single entry point for all requests
2. **JWT Authentication**: Token-based auth at gateway level
3. **Event-Driven Architecture**: Async payment processing via Kafka
4. **Immediate Response**: Order created in 100-200ms
5. **Async Payment**: Payment processed in background (2-3 seconds)
6. **Status Polling**: Client polls to check payment completion

For more details:
- [README.md](readme.md) - Project overview
- [diagrams.md](diagrams.md) - System diagrams
- [ARCHITECTURE.md](ARCHITECTURE.md) - Technology stack
- [VERIFICATION_STEPS.md](VERIFICATION_STEPS.md) - Testing guide
