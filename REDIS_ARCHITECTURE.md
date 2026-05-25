# 🎯 KIẾN TRÚC REDIS - Microservices E-Commerce System

## 📋 Table of Contents

- [Tổng Quan](#tổng-quan)
- [1. Redis Deployment Strategy](#1-redis-deployment-strategy)
- [2. API Gateway - Redis Use Cases](#2-api-gateway---redis-use-cases)
- [3. Order Service - Redis Use Cases](#3-order-service---redis-use-cases)
- [4. Payment Service - Redis Use Cases](#4-payment-service---redis-use-cases)
- [5. Common Patterns](#5-common-patterns)
- [6. Cache Invalidation Strategies](#6-cache-invalidation-strategies)
- [7. Production Configuration](#7-production-configuration)
- [8. Monitoring & Troubleshooting](#8-monitoring--troubleshooting)
- [9. Implementation Examples](#9-implementation-examples)

---

## TỔNG QUAN

### **Tại sao Redis?**

Redis phù hợp cho các use cases:
- ✅ **Caching**: Hot data access nhanh hơn DB
- ✅ **Rate Limiting**: Counter-based limits
- ✅ **Session Store**: User session, payment state
- ✅ **Distributed Lock**: Coordination across services
- ✅ **Idempotency**: Duplicate request detection
- ✅ **Real-time Analytics**: Live metrics, leaderboards

### **Redis trong kiến trúc hiện tại**

```
┌─────────────────────────────────────────────────────────────────┐
│                         Current Architecture                     │
└─────────────────────────────────────────────────────────────────┘

[API Gateway] → [Order Service] → Kafka → [Payment Service]
                  ↓                   ↓
              MySQL (Orders)      MySQL (Payments)

⚠️ VẤN ĐỀ:
  - Mỗi request truy vấn MySQL (slow)
  - Không có rate limiting
  - Không có distributed cache
  - Không có idempotency keys
```

```
┌─────────────────────────────────────────────────────────────────┐
│                     Proposed Architecture                        │
└─────────────────────────────────────────────────────────────────┘

[API Gateway] → [Order Service] → Kafka → [Payment Service]
     ↓                ↓                   ↓
  Redis #1         Redis #2            Redis #3
  (Gateway)       (Order)            (Payment)
     ↓                ↓                   ↓
  Shared Cache   Product Cache     Payment Cache
  Rate Limit     User Cache        Fraud Detection

⚠️ IMPROVEMENTS:
  ✅ Faster reads (cache hit)
  ✅ Rate limiting (DDoS protection)
  ✅ Idempotency keys (duplicate prevention)
  ✅ Distributed locks (coordination)
```

---

## 1. REDIS DEPLOYMENT STRATEGY

### **Option 1: Local Redis per Service** ⭐ RECOMMENDED

```
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│  API Gateway    │  │  Order Service  │  │ Payment Service │
│                 │  │                 │  │                 │
│  ┌───────────┐  │  │  ┌───────────┐  │  │  ┌───────────┐  │
│  │ Redis     │  │  │  │ Redis     │  │  │  │ Redis     │  │
│  │ (Local)   │  │  │  │ (Local)   │  │  │  │ (Local)   │  │
│  └───────────┘  │  │  └───────────┘  │  │  └───────────┘  │
└─────────────────┘  └─────────────────┘  └─────────────────┘
```

**Ưu điểm** ✅:
- **Low latency**: Local cache (<1ms)
- **No network overhead**: In-memory access
- **Service isolation**: Each service owns its cache
- **Simpler scaling**: Scale service + cache together

**Nhược điểm** ❌:
- **Data duplication**: Same data in multiple Redis instances
- **Higher memory usage**: 3 Redis instances instead of 1
- **Cache coherence**: No shared state between services

**When to use**:
- ✅ Services have different caching needs
- ✅ Low latency is critical
- ✅ Services scale independently

### **Option 2: Shared Redis Cluster**

```
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│  API Gateway    │  │  Order Service  │  │ Payment Service │
└────────┬────────┘  └────────┬────────┘  └────────┬────────┘
         │                  │                  │
         └──────────────────┼──────────────────┘
                            ↓
                 ┌─────────────────────┐
                 │  Redis Cluster      │
                 │  ┌───┬───┬───┬───┐  │
                 │  │ R1│ R2│ R3│ R4│  │
                 │  └───┴───┴───┴───┘  │
                 │  (Shared Cache)     │
                 └─────────────────────┘
```

**Ưu điểm** ✅:
- **Centralized management**: Easier ops
- **Data deduplication**: Same data cached once
- **Lower memory usage**: Single cache layer
- **Cache coherence**: Shared state

**Nhược điểm** ❌:
- **Network latency**: 1-5ms per call
- **SPOF**: Redis cluster downtime affects all services
- **Complex scaling**: Need cluster scaling

**When to use**:
- ✅ Services share same cache data
- ✅ Memory efficiency is important
- ✅ Ops simplicity preferred

### **Option 3: Hybrid Approach** ⭐⭐ PRODUCTION-READY

```
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│  API Gateway    │  │  Order Service  │  │ Payment Service │
│                 │  │                 │  │                 │
│  ┌───────────┐  │  │  ┌───────────┐  │  │  ┌───────────┐  │
│  │ Redis     │  │  │  │ Redis     │  │  │  │ Redis     │  │
│  │ (Local)   │  │  │  │ (Local)   │  │  │  │ (Local)   │  │
│  └───────────┘  │  │  └───────────┘  │  │  └───────────┘  │
└─────────────────┘  └─────────────────┘  └─────────────────┘
         │                  │                  │
         └──────────────────┼──────────────────┘
                            ↓
                 ┌─────────────────────┐
                 │  Redis Cluster      │
                 │  (Shared Cache)     │
                 │  - Distributed Lock │
                 │  - Global Rate Limit│
                 │  - Session Store    │
                 └─────────────────────┘
```

**Ưu điểm** ✅:
- **Best of both worlds**: Local + Shared
- **Local cache**: Hot data, low latency
- **Shared cache**: Coordination, global state

**Nhược điểm** ❌:
- **Complexity**: Two-tier caching
- **Cost**: More Redis instances

**When to use**:
- ✅ Production systems
- ✅ Need both low latency AND coordination
- ✅ Budget allows

### **RECOMMENDATION** cho hệ thống này:

```
PHASE 1 (Development):
  - Local Redis per service (docker-compose)
  - Easy setup, no network overhead

PHASE 2 (Staging):
  - Shared Redis Cluster
  - Test cache coherence

PHASE 3 (Production):
  - Hybrid approach
  - Local Redis (hot data) + Shared Redis (coordination)
```

---

## 2. API GATEWAY - REDIS USE CASES

### **Use Case 1: Rate Limiting** 🔴 CRITICAL

**Problem**: DDoS attacks, runaway clients

**Solution**: Redis-based rate limiting

```java
// RateLimiterFilter.java
@Component
public class RateLimiterFilter implements GlobalFilter {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String RATE_LIMIT_PREFIX = "rate_limit:";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String clientId = getClientId(exchange);
        String key = RATE_LIMIT_PREFIX + clientId;

        // SLIDING WINDOW LOG algorithm
        Long now = System.currentTimeMillis();
        Long windowStart = now - 60000; // 1 minute window

        // Remove old entries
        redisTemplate.opsForZSet().removeRangeByScore(key, 0, windowStart);

        // Count current requests
        Long count = redisTemplate.opsForZSet().count(key, windowStart, now);

        if (count >= 100) { // 100 requests per minute
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            return exchange.getResponse().setComplete();
        }

        // Add current request
        redisTemplate.opsForZSet().add(key, UUID.randomUUID().toString(), now);
        redisTemplate.expire(key, 1, TimeUnit.MINUTES);

        return chain.filter(exchange);
    }

    private String getClientId(ServerWebExchange exchange) {
        // Use IP or X-User-Id
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
        if (userId != null) {
            return "user:" + userId;
        }
        return "ip:" + exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
    }
}
```

**Redis Data Structure**:
```
Key: rate_limit:user:123
Type: Sorted Set (ZSET)
Score: Timestamp (milliseconds)
Member: UUID (unique request ID)
TTL: 60 seconds

Example:
ZADD rate_limit:user:123 1716392000000 "req-1"
ZADD rate_limit:user:123 1716392001000 "req-2"
ZADD rate_limit:user:123 1716392002000 "req-3"

ZCOUNT rate_limit:user:123 1716391940000 1716392000000
→ Returns: 3 (requests in last minute)
```

**Configuration**:
```yaml
# application.yml
gateway:
  rate-limit:
    enabled: true
    # Per-user limits
    user:
      requests-per-minute: 100
      requests-per-hour: 1000
    # Per-IP limits (for anonymous)
    ip:
      requests-per-minute: 20
      requests-per-hour: 100
```

### **Use Case 2: JWT Token Cache** ⚡ PERFORMANCE

**Problem**: JWT validation is expensive (HMAC verify)

**Solution**: Cache validated tokens

```java
// CachedJwtService.java
@Service
public class CachedJwtService {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RedisTemplate<String, Claims> redisTemplate;

    private static final String TOKEN_CACHE_PREFIX = "token:";
    private static final Duration TOKEN_CACHE_TTL = Duration.ofMinutes(5); // Short TTL

    public Claims validateToken(String token) {
        // Check cache first
        String cacheKey = TOKEN_CACHE_PREFIX + token.hashCode();
        Claims cachedClaims = redisTemplate.opsForValue().get(cacheKey);

        if (cachedClaims != null) {
            log.debug("Token cache hit");
            return cachedClaims;
        }

        // Cache miss - validate JWT
        log.debug("Token cache miss, validating JWT");
        Claims claims = jwtService.extractAllClaims(token);

        // Check if token is expired
        if (claims.getExpiration().before(new Date())) {
            throw new JwtException("Token expired");
        }

        // Cache the result
        redisTemplate.opsForValue().set(cacheKey, claims, TOKEN_CACHE_TTL);

        return claims;
    }

    public void invalidateToken(String token) {
        String cacheKey = TOKEN_CACHE_PREFIX + token.hashCode();
        redisTemplate.delete(cacheKey);
    }
}
```

**Redis Data Structure**:
```
Key: token:123456789
Type: String (JSON serialized Claims)
TTL: 300 seconds (5 minutes)

Example:
SET token:123456789 '{"sub":"john.doe","userId":"user-123",...}' EX 300
```

**Performance Gain**:
- Without cache: ~30ms per validation (HMAC verify)
- With cache: ~1ms per validation (Redis GET)
- **Improvement: 30x faster**

### **Use Case 3: Token Blacklist** 🔒 SECURITY

**Problem**: User logout, but token still valid until expiration

**Solution**: Blacklist revoked tokens

```java
// TokenBlacklistService.java
@Service
public class TokenBlacklistService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String BLACKLIST_PREFIX = "blacklist:";

    public void addToBlacklist(String token, long expirationTime) {
        long ttl = expirationTime - System.currentTimeMillis();
        if (ttl > 0) {
            String key = BLACKLIST_PREFIX + token.hashCode();
            redisTemplate.opsForValue().set(key, "revoked", ttl, TimeUnit.MILLISECONDS);
            log.info("Token added to blacklist: {}", key);
        }
    }

    public boolean isBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token.hashCode();
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    // When user logs out
    public void logout(String token) {
        Claims claims = jwtService.extractAllClaims(token);
        long expirationTime = claims.getExpiration().getTime();
        addToBlacklist(token, expirationTime);
    }
}
```

**Redis Data Structure**:
```
Key: blacklist:987654321
Type: String
Value: "revoked"
TTL: Remaining token lifetime (e.g., 55 minutes)

Example:
SET blacklist:987654321 "revoked" EX 3300
```

---

## 3. ORDER SERVICE - REDIS USE CASES

### **Use Case 1: Product Catalog Cache** 📦 HOT DATA

**Problem**: Products fetched from DB every time

**Solution**: Cache hot products

```java
// ProductCacheService.java
@Service
public class ProductCacheService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private RedisTemplate<String, ProductDto> redisTemplate;

    private static final String PRODUCT_PREFIX = "product:";

    // READ-THROUGH cache
    public ProductDto getProduct(String productId) {
        String cacheKey = PRODUCT_PREFIX + productId;

        // Try cache first
        ProductDto cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug("Product cache hit: {}", productId);
            return cached;
        }

        // Cache miss - fetch from DB
        log.debug("Product cache miss: {}", productId);
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new NotFoundException("Product not found"));

        ProductDto dto = ProductDto.from(product);

        // Cache for 1 hour
        redisTemplate.opsForValue().set(cacheKey, dto, Duration.ofHours(1));
z
        return dto;
    }

    // WRITE-BEHIND cache (invalidate on update)
    @CacheEvict(value = "products", key = "#productId")
    public void updateProduct(String productId, UpdateProductRequest request) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new NotFoundException("Product not found"));

        product.setName(request.getName());
        product.setPrice(request.getPrice());
        productRepository.save(product);

        // Cache automatically invalidated by @CacheEvict
    }

    // Cache warming on startup
    @PostConstruct
    public void warmUpCache() {
        log.info("Warming up product cache...");

        // Fetch top 1000 most popular products
        List<Product> popularProducts = productRepository.findTop1000ByOrderByPopularityDesc();

        popularProducts.forEach(product -> {
            String cacheKey = PRODUCT_PREFIX + product.getId();
            ProductDto dto = ProductDto.from(product);
            redisTemplate.opsForValue().set(cacheKey, dto, Duration.ofHours(1));
        });

        log.info("Product cache warmed: {} products", popularProducts.size());
    }
}
```

**Redis Data Structure**:
```
Key: product:prod-001
Type: Hash (for field-level updates) or String (JSON)
TTL: 3600 seconds (1 hour)

Example (String - simpler):
SET product:prod-001 '{"id":"prod-001","name":"Laptop","price":1500}' EX 3600

Example (Hash - for field updates):
HSET product:prod-001 id "prod-001"
HSET product:prod-001 name "Laptop"
HSET product:prod-001 price "1500"
EXPIRE product:prod-001 3600

# Later, update just price:
HSET product:prod-001 price "1400"
```

**Configuration**:
```yaml
# application.yml
spring:
  cache:
    type: redis
    redis:
      time-to-live: 3600000  # 1 hour
      cache-null-values: false
      key-prefix: "order-service::"
  redis:
    host: localhost
    port: 6379
```

### **Use Case 2: User's Recent Orders Cache** 📋 READ HEAVY

**Problem**: Users frequently view their recent orders

**Solution**: Cache recent orders per user

```java
// UserOrderCacheService.java
@Service
public class UserOrderCacheService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RedisTemplate<String, List<OrderSummaryDto>> redisTemplate;

    private static final String USER_ORDERS_PREFIX = "user_orders:";
    private static final int CACHE_SIZE = 20; // Cache last 20 orders

    public List<OrderSummaryDto> getUserRecentOrders(String userId) {
        String cacheKey = USER_ORDERS_PREFIX + userId;

        // Try cache
        List<OrderSummaryDto> cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug("User orders cache hit: {}", userId);
            return cached;
        }

        // Cache miss - fetch from DB
        log.debug("User orders cache miss: {}", userId);
        List<Order> orders = orderRepository.findTop20ByUserIdOrderByCreatedDateDesc(userId);

        List<OrderSummaryDto> dtos = orders.stream()
            .map(OrderSummaryDto::from)
            .collect(Collectors.toList());

        // Cache for 10 minutes
        redisTemplate.opsForValue().set(cacheKey, dtos, Duration.ofMinutes(10));

        return dtos;
    }

    // Invalidate when new order created
    public void invalidateUserOrders(String userId) {
        String cacheKey = USER_ORDERS_PREFIX + userId;
        redisTemplate.delete(cacheKey);
        log.info("Invalidated user orders cache: {}", userId);
    }
}
```

**Redis Data Structure**:
```
Key: user_orders:user-123
Type: List (or String with JSON array)
TTL: 600 seconds (10 minutes)

Example:
LPUSH user_orders:user-123 '{"orderId":"order-1","totalAmount":100}'
LPUSH user_orders:user-123 '{"orderId":"order-2","totalAmount":200}'
EXPIRE user_orders:user-123 600

# Get all orders
LRANGE user_orders:user-123 0 -1
```

### **Use Case 3: Idempotency Keys** 🔄 DUPLICATE PREVENTION

**Problem**: Double-click "Submit Order" button → duplicate orders

**Solution**: Idempotency key cache

```java
// IdempotencyService.java
@Service
public class IdempotencyService {

    @Autowired
    private RedisTemplate<String, OrderResponse> redisTemplate;

    private static final String IDEMPOTENCY_PREFIX = "idempotency:";
    private static final Duration IDEMPOTENCY_TTL = Duration.ofHours(24);

    public OrderResponse executeOrCreate(String idempotencyKey, Supplier<OrderResponse> supplier) {
        String cacheKey = IDEMPOTENCY_PREFIX + idempotencyKey;

        // Check if already processed
        OrderResponse cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.warn("Duplicate request detected, returning cached response: {}", idempotencyKey);
            return cached;
        }

        // Execute business logic
        OrderResponse response = supplier.get();

        // Cache result
        redisTemplate.opsForValue().set(cacheKey, response, IDEMPOTENCY_TTL);

        return response;
    }

    public boolean isProcessed(String idempotencyKey) {
        String cacheKey = IDEMPOTENCY_PREFIX + idempotencyKey;
        return Boolean.TRUE.equals(redisTemplate.hasKey(cacheKey));
    }
}

// Usage in CreateOrderCommand
@Component
public class CreateOrderCommand {

    @Autowired
    private IdempotencyService idempotencyService;

    public OrderResponse execute(CreateOrderRequest request, String idempotencyKey) {
        return idempotencyService.executeOrCreate(idempotencyKey, () -> {
            // Business logic here
            Order order = createOrder(request);
            return OrderResponse.from(order);
        });
    }
}
```

**Redis Data Structure**:
```
Key: idempotency:req-uuid-12345
Type: String (JSON serialized response)
TTL: 86400 seconds (24 hours)

Example:
SET idempotency:req-uuid-12345 '{"orderId":"order-456","status":"PENDING"}' EX 86400
```

### **Use Case 4: Distributed Lock** 🔒 COORDINATION

**Problem**: Concurrent updates to same order

**Solution**: Redis distributed lock

```java
// DistributedLockService.java
@Service
public class DistributedLockService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String LOCK_PREFIX = "lock:";

    public <T> T executeWithLock(String lockKey, Duration timeout, Supplier<T> action) {
        String key = LOCK_PREFIX + lockKey;
        String value = UUID.randomUUID().toString();

        // Try to acquire lock
        Boolean acquired = redisTemplate.opsForValue()
            .setIfAbsent(key, value, timeout);

        if (!acquired) {
            throw new LockAcquisitionException("Could not acquire lock: " + key);
        }

        try {
            // Execute action with lock held
            return action.get();
        } finally {
            // Release lock (only if we own it)
            releaseLock(key, value);
        }
    }

    private void releaseLock(String key, String value) {
        // Lua script to ensure atomicity
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                       "  return redis.call('del', KEYS[1]) " +
                       "else " +
                       "  return 0 " +
                       "end";

        redisTemplate.execute(
            new DefaultRedisScript<>(script, Long.class),
            Collections.singletonList(key),
            value
        );
    }
}

// Usage
@Service
public class OrderService {

    @Autowired
    private DistributedLockService lockService;

    public void updateOrder(String orderId, UpdateOrderRequest request) {
        lockService.executeWithLock(
            "order:" + orderId,
            Duration.ofSeconds(10),
            () -> {
                // Critical section - only one thread can execute
                Order order = orderRepository.findById(orderId).orElseThrow();
                order.setStatus(request.getStatus());
                orderRepository.save(order);
                return null;
            }
        );
    }
}
```

**Redis Data Structure**:
```
Key: lock:order:order-123
Type: String
Value: UUID (lock owner identifier)
TTL: 10 seconds (auto-release if crash)

Example:
SET lock:order:order-123 "uuid-abc-123" NX EX 10

# Release (Lua script ensures only owner can release)
if GET lock:order:order-123 == "uuid-abc-123" then
  DEL lock:order:order-123
end
```

---

## 4. PAYMENT SERVICE - REDIS USE CASES

### **Use Case 1: Payment Status Cache** 💳 HOT DATA

**Problem**: Frequent payment status checks

**Solution**: Cache payment status

```java
// PaymentCacheService.java
@Service
public class PaymentCacheService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private RedisTemplate<String, PaymentStatusDto> redisTemplate;

    private static final String PAYMENT_STATUS_PREFIX = "payment_status:";

    public PaymentStatusDto getPaymentStatus(String paymentId) {
        String cacheKey = PAYMENT_STATUS_PREFIX + paymentId;

        // Try cache
        PaymentStatusDto cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // Cache miss
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new NotFoundException("Payment not found"));

        PaymentStatusDto dto = PaymentStatusDto.from(payment);

        // Cache for 5 minutes (short TTL because status changes frequently)
        redisTemplate.opsForValue().set(cacheKey, dto, Duration.ofMinutes(5));

        return dto;
    }

    // Invalidate when payment status changes
    public void invalidatePaymentStatus(String paymentId) {
        String cacheKey = PAYMENT_STATUS_PREFIX + paymentId;
        redisTemplate.delete(cacheKey);
    }
}
```

### **Use Case 2: Fraud Detection Cache** 🚨 SECURITY

**Problem**: Detect suspicious payment activities

**Solution**: Track suspicious patterns

```java
// FraudDetectionService.java
@Service
public class FraudDetectionService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String FRAUD_PREFIX = "fraud:";

    public boolean isSuspicious(PaymentRequest request) {
        String cardHash = hashCard(request.getCardNumber());
        String userId = request.getUserId();

        // Check 1: Multiple payments with same card in short time
        String cardKey = FRAUD_PREFIX + "card:" + cardHash;
        Long cardCount = redisTemplate.opsForValue().increment(cardKey);
        redisTemplate.expire(cardKey, Duration.ofMinutes(5));

        if (cardCount > 3) {
            log.warn("Suspicious: Multiple payments with same card: {}", cardHash);
            return true;
        }

        // Check 2: Multiple payments from same user in short time
        String userKey = FRAUD_PREFIX + "user:" + userId;
        Long userCount = redisTemplate.opsForValue().increment(userKey);
        redisTemplate.expire(userKey, Duration.ofMinutes(5));

        if (userCount > 5) {
            log.warn("Suspicious: Multiple payments from user: {}", userId);
            return true;
        }

        // Check 3: High amount (check blacklist)
        String amountKey = FRAUD_PREFIX + "amount:" + request.getAmount();
        if (Boolean.TRUE.equals(redisTemplate.hasKey(amountKey))) {
            log.warn("Suspicious: Blacklisted amount: {}", request.getAmount());
            return true;
        }

        return false;
    }

    public void blacklistAmount(BigDecimal amount) {
        String key = FRAUD_PREFIX + "amount:" + amount;
        redisTemplate.opsForValue().set(key, "blacklisted", Duration.ofHours(1));
    }
}
```

**Redis Data Structure**:
```
Key: fraud:card:a1b2c3d4
Type: String (counter)
TTL: 300 seconds (5 minutes)

Example:
INCR fraud:card:a1b2c3d4
EXPIRE fraud:card:a1b2c3d4 300

Key: fraud:user:user-123
Type: String (counter)
TTL: 300 seconds

Key: fraud:amount:10000
Type: String (blacklist flag)
TTL: 3600 seconds (1 hour)
```

### **Use Case 3: Payment Gateway Rate Limit** 🚦 GATEWAY PROTECTION

**Problem**: Payment gateway has rate limits

**Solution**: Track gateway usage

```java
// GatewayRateLimitService.java
@Service
public class GatewayRateLimitService {

    @Autowired
    private RedisTemplate<String, Long> redisTemplate;

    private static final String GATEWAY_LIMIT_PREFIX = "gateway_limit:";

    public boolean canCallGateway(String gatewayType) {
        String key = GATEWAY_LIMIT_PREFIX + gatewayType;

        // Increment counter
        Long count = redisTemplate.opsForValue().increment(key);

        // Set expiration on first call
        if (count == 1) {
            redisTemplate.expire(key, Duration.ofSeconds(1));
        }

        // Check limit (e.g., 100 calls per second)
        if (count > 100) {
            log.warn("Gateway rate limit exceeded: {}", gatewayType);
            return false;
        }

        return true;
    }

    public long getRemainingQuota(String gatewayType) {
        String key = GATEWAY_LIMIT_PREFIX + gatewayType;
        Long count = redisTemplate.opsForValue().get(key);
        return count != null ? 100 - count : 100;
    }
}
```

### **Use Case 4: Payment Session Store** 💾 SESSION

**Problem**: Payment flow requires multi-step state

**Solution**: Store payment session in Redis

```java
// PaymentSessionService.java
@Service
public class PaymentSessionService {

    @Autowired
    private RedisTemplate<String, PaymentSession> redisTemplate;

    private static final String SESSION_PREFIX = "payment_session:";

    public PaymentSession createSession(String orderId, BigDecimal amount) {
        PaymentSession session = new PaymentSession();
        session.setId(UUID.randomUUID().toString());
        session.setOrderId(orderId);
        session.setAmount(amount);
        session.setStatus("INITIATED");
        session.setCreatedAt(Instant.now());

        String key = SESSION_PREFIX + session.getId();
        redisTemplate.opsForValue().set(key, session, Duration.ofMinutes(15));

        return session;
    }

    public PaymentSession getSession(String sessionId) {
        String key = SESSION_PREFIX + sessionId;
        return redisTemplate.opsForValue().get(key);
    }

    public void updateSession(String sessionId, String status, Map<String, Object> metadata) {
        PaymentSession session = getSession(sessionId);
        if (session != null) {
            session.setStatus(status);
            session.setMetadata(metadata);
            session.setUpdatedAt(Instant.now());

            String key = SESSION_PREFIX + sessionId;
            redisTemplate.opsForValue().set(key, session, Duration.ofMinutes(15));
        }
    }
}
```

---

## 5. COMMON PATTERNS

### **Pattern 1: Cache-Aside** (LAZY LOADING)

```
Application flow:
  1. Check Redis cache
  2. If cache hit → Return data
  3. If cache miss → Fetch from DB
  4. Write to Redis cache
  5. Return data

Pros:
  ✅ Simple to implement
  ✅ Only cache requested data

Cons:
  ❌ Cache stampede if many miss at once
  ❌ Stale data until TTL expires
```

**Implementation**:
```java
public Product getProduct(String id) {
    // 1. Check cache
    Product cached = redis.get("product:" + id);
    if (cached != null) {
        return cached;
    }

    // 2. Cache miss - fetch from DB
    Product product = db.findById(id);

    // 3. Write to cache
    redis.set("product:" + id, product, 3600);

    return product;
}
```

### **Pattern 2: Write-Through**

```
Application flow:
  1. Write to Redis cache
  2. Write to DB (synchronous)
  3. Return success

Pros:
  ✅ Cache always consistent with DB
  ✅ Data always available in cache

Cons:
  ❌ Higher write latency (2 writes)
  ❌ DB write failure handling needed
```

**Implementation**:
```java
@Transactional
public void updateProduct(Product product) {
    // 1. Update DB
    db.save(product);

    // 2. Update cache (in same transaction)
    redis.set("product:" + product.getId(), product, 3600);

    // 3. Return
}
```

### **Pattern 3: Write-Behind** (WRITE-BACK)

```
Application flow:
  1. Write to Redis cache (async)
  2. Return success immediately
  3. Background thread writes to DB

Pros:
  ✅ Very fast writes
  ✅ Batch DB writes possible

Cons:
  ❌ Data loss if Redis crashes before DB write
  ❌ Complex implementation
  ❌ Eventual consistency with DB
```

**Implementation**:
```java
public void updateProduct(Product product) {
    // 1. Update cache immediately
    redis.set("product:" + product.getId(), product, 3600);

    // 2. Queue for async DB write
    redis.lpush("write_queue", product.toJson());

    // 3. Return immediately (don't wait for DB)
}

// Background worker
@Scheduled(fixedRate = 1000)
public void flushWriteQueue() {
    List<String> items = redis.lrange("write_queue", 0, 100);
    redis.ltrim("write_queue", 100, -1);

    items.forEach(json -> {
        Product product = Product.fromJson(json);
        db.save(product);
    });
}
```

---

## 6. CACHE INVALIDATION STRATEGIES

### **Strategy 1: TTL-Based Expiration** ⭐ SIMPLEST

```
Set TTL when writing to cache
Redis auto-expires after TTL

Pros:
  ✅ No invalidation logic needed
  ✅ Auto cleanup

Cons:
  ❌ Stale data until expiration
  ❌ May serve outdated data
```

### **Strategy 2: Write-Through Invalidation** ⭐ RECOMMENDED

```
When updating DB:
  1. Update DB
  2. Delete/update cache key immediately

Pros:
  ✅ Cache stays fresh
  ✅ Simple to implement

Cons:
  ❌ Race conditions possible
  ❌ Need to track all cache keys
```

**Implementation**:
```java
@CacheEvict(value = "products", key = "#product.id")
public void updateProduct(Product product) {
    productRepository.save(product);
    // Cache automatically evicted by Spring Cache
}
```

### **Strategy 3: Cache Tagging** 🔧 ADVANCED

```
Associate multiple keys with a tag
Invalidate all keys with same tag at once

Use case:
  - Product cache tagged by category
  - When category updates, invalidate all products in category
```

**Implementation**:
```java
@Service
public class ProductCacheService {

    public void cacheProduct(Product product) {
        String productKey = "product:" + product.getId();
        String categoryTag = "category:" + product.getCategoryId();

        // Cache product
        redis.set(productKey, product, 3600);

        // Add product to category tag set
        redis.sadd(categoryTag, productKey);
    }

    public void invalidateCategory(String categoryId) {
        String categoryTag = "category:" + categoryId;

        // Get all product keys in category
        Set<String> productKeys = redis.smembers(categoryTag);

        // Delete all product keys
        redis.del(productKeys.toArray(new String[0]));

        // Delete tag set
        redis.del(categoryTag);
    }
}
```

---

## 7. PRODUCTION CONFIGURATION

### **Redis Configuration** (application.yml)

```yaml
spring:
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
    database: 0
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 50  # Max connections in pool
        max-idle: 20    # Max idle connections
        min-idle: 5     # Min idle connections
        max-wait: 1000ms  # Max wait for connection

  cache:
    type: redis
    redis:
      time-to-live: 3600000  # Default TTL: 1 hour
      cache-null-values: false  # Don't cache null values
      key-prefix: "${spring.application.name}::"  # Service-specific prefix
      use-key-prefix: true

# Custom cache settings
cache:
  products:
    ttl: 3600000  # 1 hour
  users:
    ttl: 1800000  # 30 minutes
  sessions:
    ttl: 900000   # 15 minutes
```

### **Redis Cluster Configuration** (Production)

```yaml
spring:
  redis:
    cluster:
      nodes:
        - redis-node-1:6379
        - redis-node-2:6379
        - redis-node-3:6379
        - redis-node-4:6379
        - redis-node-5:6379
        - redis-node-6:6379
      max-redirects: 3
    password: ${REDIS_PASSWORD}
    timeout: 3000ms
    lettuce:
      pool:
        max-active: 100
        max-idle: 50
        min-idle: 10
```

### **Docker Compose** (Development)

```yaml
version: '3.8'

services:
  redis:
    image: redis:7-alpine
    container_name: microservice-redis
    ports:
      - "6379:6379"
    command: redis-server --appendonly yes
    volumes:
      - redis-data:/data
    environment:
      - REDIS_MAXMEMORY=256mb
      - REDIS_MAXMEMORY_POLICY=allkeys-lru
    networks:
      - microservice-network

  redis-commander:
    image: rediscommander/redis-commander:latest
    container_name: redis-commander
    ports:
      - "8081:8081"
    environment:
      - REDIS_HOSTS=local:redis:6379
    networks:
      - microservice-network

volumes:
  redis-data:

networks:
  microservice-network:
    driver: bridge
```

---

## 8. MONITORING & TROUBLESHOOTING

### **Key Metrics to Monitor**

```java
@Component
public class RedisMetrics {

    @Autowired
    private RedisConnectionFactory connectionFactory;

    @Autowired
    private MeterRegistry meterRegistry;

    @Scheduled(fixedRate = 60000)  # Every minute
    public void reportMetrics() {
        RedisConnection connection = connectionFactory.getConnection();

        // Memory usage
        Properties info = connection.info("memory");
        long usedMemory = Long.parseLong(info.getProperty("used_memory"));
        long maxMemory = Long.parseLong(info.getProperty("maxmemory"));

        Gauge.builder("redis.memory.used", usedMemory)
            .register(meterRegistry);

        Gauge.builder("redis.memory.usage_ratio", (double) usedMemory / maxMemory)
            .register(meterRegistry);

        // Hit rate
        Properties stats = connection.info("stats");
        long keyspaceHits = Long.parseLong(stats.getProperty("keyspace_hits"));
        long keyspaceMisses = Long.parseLong(stats.getProperty("keyspace_misses"));
        long total = keyspaceHits + keyspaceMisses;

        Gauge.builder("redis.hit_ratio", (double) keyspaceHits / total)
            .register(meterRegistry);

        // Connected clients
        Properties clients = connection.info("clients");
        long connectedClients = Long.parseLong(clients.getProperty("connected_clients"));

        Gauge.builder("redis.clients.connected", connectedClients)
            .register(meterRegistry);

        connection.close();
    }
}
```

### **Common Issues & Solutions**

| Issue | Symptom | Solution |
|-------|---------|----------|
| **High Memory Usage** | Redis used_memory > maxmemory | Set maxmemory + eviction policy (allkeys-lru) |
| **Low Hit Ratio** | hit_ratio < 0.5 | Increase TTL, warm up cache, check cache pattern |
| **Connection Exhaustion** | Can't get connection from pool | Increase max-active, check for connection leaks |
| **Slow Commands** | Redis slow log shows slow ops | Avoid O(N) commands, use SCAN instead of KEYS |

### **Performance Tuning**

```bash
# Disable saving for cache-only Redis
redis-cli CONFIG SET save ""

# Set max memory
redis-cli CONFIG SET maxmemory 256mb

# Set eviction policy
redis-cli CONFIG SET maxmemory-policy allkeys-lru

# Enable lazy eviction
redis-cli CONFIG SET lazyfree-lazy-eviction yes

# Monitor slow commands
redis-cli SLOWLOG GET 10

# Monitor real-time commands
redis-cli MONITOR
```

---

## 9. IMPLEMENTATION EXAMPLES

### **Full Example: Product Cache with Spring Cache**

```java
// Configuration
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(1))
            .disableCachingNullValues()
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()));

        return RedisCacheManager.builder(factory)
            .cacheDefaults(config)
            .withInitialCacheConfigurations(
                Map.of(
                    "products", config.entryTtl(Duration.ofHours(2)),
                    "users", config.entryTtl(Duration.ofMinutes(30)),
                    "sessions", config.entryTtl(Duration.ofMinutes(15))
                )
            )
            .build();
    }
}

// Service
@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Cacheable(value = "products", key = "#id")
    public ProductDto getProduct(String id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Product not found"));
        return ProductDto.from(product);
    }

    @CachePut(value = "products", key = "#result.id")
    public ProductDto updateProduct(String id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Product not found"));

        product.setName(request.getName());
        product.setPrice(request.getPrice());
        Product saved = productRepository.save(product);

        return ProductDto.from(saved);
    }

    @CacheEvict(value = "products", key = "#id")
    public void deleteProduct(String id) {
        productRepository.deleteById(id);
    }
}
```

### **Full Example: Rate Limiting with Spring Boot Starter**

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis-reactive</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot3</artifactId>
</dependency>
```

```java
// Custom annotation
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {
    int requests() default 100;
    Duration window() default Duration.ofMinutes(1);
    String key() default "";  // SpEL expression
}

// Aspect
@Aspect
@Component
public class RateLimitAspect {

    @Autowired
    private ReactiveRedisTemplate<String, Long> redisTemplate;

    @Around("@annotation(rateLimit)")
    public Object rateLimit(ProceedingJoinPoint pjp, RateLimit rateLimit) throws Throwable {
        String key = "rate_limit:" + getKey(pjp, rateLimit);

        Long count = redisTemplate.opsForValue()
            .increment(key)
            .block();

        if (count == null || count == 1) {
            redisTemplate.expire(key, rateLimit.window()).block();
        }

        if (count > rateLimit.requests()) {
            throw new RateLimitExceededException("Rate limit exceeded");
        }

        return pjp.proceed();
    }

    private String getKey(ProceedingJoinPoint pjp, RateLimit rateLimit) {
        // Parse SpEL expression or use default
        return rateLimit.key().isEmpty() ?
            "default" :
            parseSpEL(rateLimit.key(), pjp);
    }
}

// Usage
@RestController
public class OrderController {

    @RateLimit(requests = 10, window = Duration.ofMinutes(1))
    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> createOrder(@RequestBody CreateOrderRequest request) {
        // Business logic
    }
}
```

---

## 📊 SUMMARY TABLE

| Service | Use Cases | Redis Data Types | TTL Recommendation |
|---------|-----------|------------------|-------------------|
| **API Gateway** | Rate Limiting, JWT Cache, Token Blacklist | String, ZSET | 1-60 minutes |
| **Order Service** | Product Cache, User Orders, Idempotency, Distributed Lock | String, Hash, List, SET | 15 min - 2 hours |
| **Payment Service** | Payment Status, Fraud Detection, Gateway Limit, Session | String, Hash | 1-15 minutes |

---

## 🚀 NEXT STEPS

1. **Implement Phase 1**: Local Redis per service (docker-compose)
2. **Add Core Use Cases**: Product cache, rate limiting, idempotency
3. **Monitor Metrics**: Hit ratio, memory usage, connection pool
4. **Tune Configuration**: TTL, maxmemory, eviction policy
5. **Scale to Production**: Redis cluster with high availability

---

**Document Version**: 1.0  
**Last Updated**: 2026-05-22  
**Author**: Enterprise Backend Architect  
**Target Audience**: Senior/Lead Backend Engineers
