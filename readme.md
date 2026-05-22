# Microservices E-Commerce System - Event-Driven Architecture

## 📋 Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Services](#services)
- [Key Features](#key-features)
- [Getting Started](#getting-started)
- [Documentation](#documentation)
- [Development](#development)
- [Deployment](#deployment)
- [Contributing](#contributing)

---

## 🎯 Overview

Đây là hệ thống thương mại điện tử (E-Commerce) được xây dựng theo kiến trúc **Microservices** với **Event-Driven Architecture (EDA)** và **CQRS Pattern**. Hệ thống bao gồm 3 microservices chính giao tiếp bất đồng bộ qua **Apache Kafka**, đảm bảo performance và scalability cao.

### 🏗️ Architecture Pattern

```
┌─────────────┐      ┌──────────────┐      ┌──────────────┐
│   Client    │──────│ API Gateway  │──────│   Services   │
│  (Frontend) │      │  (Port 8080)  │      │              │
└─────────────┘      └──────────────┘      └──────────────┘
                            │
                    ┌───────┴───────┐
                    │  Kafka Cluster │
                    │  (Port 9092)  │
                    └───────┬───────┘
                            │
          ┌─────────────────┼─────────────────┐
          │                 │                 │
    ┌─────▼─────┐   ┌─────▼──────┐   ┌───▼──────────┐
    │   Order   │   │  Payment   │   │   Other      │
    │  Service  │   │  Service   │   │   Services   │
    │ (8082)    │   │  (8083)    │   │              │
    └───────────┘   └────────────┘   └──────────────┘
```

### 🎯 Business Problem Solved

#### Trước đây (Synchronous Architecture):
```
User → Create Order → [6-7 giây] → Payment Gateway → Response
                                      ↓
                                 (Blocking Call)
                              User phải chờ đợi
```

**Vấn đề:**
- ❌ User phải chờ 6-7 giây để tạo đơn hàng
- ❌ Payment Gateway timeout → Order creation thất bại
- ❌ Không scale được độc lập mỗi service
- ❌ Tight coupling giữa services

#### Hiện tại (Event-Driven Architecture):
```
User → Create Order → [100-200ms] → Response ✅
                           ↓
                    Publish OrderCreatedEvent
                           ↓
                    Payment Service xử lý async (2-3s)
                           ↓
                    Publish PaymentProcessedEvent
                           ↓
                    Order Service cập nhật status tự động
```

**Lợi ích:**
- ✅ User nhận response ngay lập tức (UX tốt hơn)
- ✅ Services decoupled, có scale độc lập
- ✅ Payment processing không block user flow
- ✅ Dễ thêm services mới (Shipping, Notification, etc.)

---

## 🏢 Services

### 1. API Gateway Service
**Port:** `8080`

**Responsibilities:**
- Central entry point cho tất cả client requests
- JWT Authentication & Authorization
- Route requests đến appropriate services
- Rate limiting & security policies
- CORS configuration

**Key Features:**
- ✅ JWT validation tại Gateway (Single Source of Truth)
- ✅ Inject user headers (`X-User-Id`, `X-User-Name`, `X-User-Fullname`) vào downstream services
- ✅ Eliminates "Shared Secret Key Anti-pattern"
- ✅ Spring Cloud Gateway with WebFlux (non-blocking)

### 2. Order Service
**Port:** `8082`
**Database:** `order_service_db`

**Responsibilities:**
- Order CRUD operations
- Order lifecycle management (PENDING → CONFIRMED → PAID → SHIPPED → DELIVERED)
- Event publishing (OrderCreatedEvent)
- Event consumption (PaymentProcessedEvent)
- Order status updates based on payment result

**Key Features:**
- ✅ CQRS Pattern với GraphQL Queries
- ✅ Event-driven payment processing
- ✅ Idempotency checks
- ✅ Transactional event publishing
- ✅ ThreadLocal user context (from Gateway headers)

### 3. Payment Service
**Port:** `8083`
**Database:** `payment_service_db`

**Responsibilities:**
- Payment processing (via Gateway Ngân hàng - simulated)
- Payment status tracking (PROCESSING → PAID/FAILED)
- Retry logic cho failed payments
- Event consumption (OrderCreatedEvent)
- Event publishing (PaymentProcessedEvent)

**Key Features:**
- ✅ 80% success rate simulation (configurable)
- ✅ Transaction ID generation (TXN-YYYYMMDDHHMMSS-XXXXXXXX)
- ✅ Multiple failure scenarios
- ✅ Retry mechanism (max 3 attempts)
- ✅ Exactly-once semantics với TransactionSynchronization

---

## 🚀 Key Features

### 1. Event-Driven Architecture (EDA)
Services giao tiếp qua **Apache Kafka** topics:
- `order-events` - Order events (OrderCreated, OrderPaid, OrderPaymentFailed)
- `payment-events` - Payment events (PaymentProcessed)

### 2. Choreography Saga Pattern
Không có orchestrator trung tâm, mỗi service tự xử lý:
```
Order Service                    Payment Service
     |                                  |
     |---OrderCreatedEvent----------->|
     |                                  |
     |                                  |--PaymentProcessedEvent
     |<---------------------------------|
     |
 Update Order Status
```

### 3. CQRS Pattern
- **Write (Command):** REST endpoints (via API Gateway)
- **Read (Query):** GraphQL endpoints (direct access)
- Separate data models for commands vs queries

### 4. Transactional Event Publishing
Events chỉ được gửi **SAU KHI** DB commit thành công:
```java
TransactionSynchronizationManager.registerSynchronization(
    new TransactionSynchronization() {
        @Override
        public void afterCommit() {
            publishEvent(); // Only sent if DB commit succeeded
        }
    }
);
```

### 5. Idempotency & Exactly-Once Semantics
- Duplicate events không gây double updates
- Payment status check trước khi update
- Kafka partition key bằng `orderId` để đảm bảo ordering per order

### 6. Distributed Tracing
- `traceId` flows through all services
- Easy debugging across microservices
- Spring Cloud Sleuth + Zipkin compatible

---

## 🚦 Getting Started

### Prerequisites

- **Java 21+**
- **Maven 3.8+**
- **Apache Kafka 2.8+** (running on `localhost:9092`)
- **MySQL 8.0+** (3 databases: `api_gateway_db`, `order_service_db`, `payment_service_db`)
- **Git**

### Clone Repository

```bash
git clone https://github.com/your-org/microservice-ecommerce.git
cd microservice-ecommerce
```

### Start Kafka

```bash
# Start Zookeeper
bin/zookeeper-server-start.bat config/zookeeper.properties

# Start Kafka
bin/kafka-server-start.bat config/server.properties

# Create topics
bin/kafka-topics.bat --create --topic order-events --bootstrap-server localhost:9092
bin/kafka-topics.bat --create --topic payment-events --bootstrap-server localhost:9092
```

### Start Services

```bash
# Terminal 1: API Gateway
cd api-gateway
mvn spring-boot:run

# Terminal 2: Order Service
cd order-service
mvn spring-boot:run

# Terminal 3: Payment Service
cd payment-service
mvn spring-boot:run
```

### Verify Services

```bash
# Check health endpoints
curl http://localhost:8080/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
```

---

## 📚 Documentation

- **[Architecture Overview](architecture.md)** - Công nghệ & Tech stack
- **[System Diagrams](diagrams.md)** - Flow diagrams & sequence diagrams
- **[Configuration Guide](config.md)** - Cấu hình ứng dụng
- **[Project Rules](.github/project_rules.md)** - Coding standards & best practices
- **[Verification Steps](VERIFICATION_STEPS.md)** - End-to-end testing guide

---

## 💻 Development

### Build All Services

```bash
# Build entire project
mvn clean install

# Skip tests during build
mvn clean install -DskipTests
```

### Run Tests

```bash
# Test all services
mvn test

# Test specific service
cd order-service
mvn test
```

### Code Structure

```
microservice-ecommerce/
├── api-gateway/                 # API Gateway Service
│   ├── src/main/java/
│   │   └── com/gateway/
│   │       ├── filter/          # JWT Filter
│   │       ├── config/          # Gateway Config
│   │       └── ApiGatewayApplication.java
│   └── pom.xml
│
├── order-service/               # Order Service
│   ├── src/main/java/
│   │   └── com/order/
│   │       ├── api/             # GraphQL Resolvers, REST Controllers
│   │       ├── application/     # Commands, Queries, Services
│   │       ├── domain/          # Entities, Repositories, Enums
│   │       └── infrastructure/  # Kafka, Config, Security
│   └── pom.xml
│
├── payment-service/             # Payment Service
│   ├── src/main/java/
│   │   └── com/payment/
│   │       ├── application/     # Services, Commands
│   │       ├── domain/          # Entities, Repositories
│   │       └── infrastructure/  # Kafka, Consumer, Producer
│   └── pom.xml
│
└── docs/                        # Documentation
    ├── README.md
    ├── architecture.md
    ├── diagrams.md
    └── config.md
```

---

## 🚢 Deployment

### Environment Variables

| Service | Port | Database | Kafka Topics |
|---------|------|----------|--------------|
| API Gateway | 8080 | api_gateway_db | - |
| Order Service | 8082 | order_service_db | order-events (produce), payment-events (consume) |
| Payment Service | 8083 | payment_service_db | order-events (consume), payment-events (produce) |

### Production Deployment

```bash
# Build Docker images
mvn clean package
docker-compose build

# Deploy to Kubernetes
kubectl apply -f k8s/

# Verify deployment
kubectl get pods
kubectl get services
```

---

## 🤝 Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open Pull Request

Xem [Project Rules](.github/project_rules.md) để hiểu coding standards.

---

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👥 Team

- **Backend Team** - Microservices Architecture
- **DevOps Team** - Infrastructure & Deployment
- **QA Team** - Testing & Quality Assurance

---

## 📞 Support

For questions or support, please contact:
- Email: dev-team@example.com
- Slack: #ecommerce-microservices
- Jira: https://jira.example.com/projects/ECOM

---

**Built with ❤️ using Spring Boot 3.5.5, Java 21, and Apache Kafka**
