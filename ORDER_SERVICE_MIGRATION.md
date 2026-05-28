# Order Service Database Schema Migration Guide

## Overview
Đã cập nhật order-service theo cấu trúc database mới cho ORDERS và order_items tables.

## ✅ Completed Changes

### Database Schema Changes

**ORDERS Table:**
```sql
CREATE TABLE IF NOT EXISTS ORDERS (
    ID VARCHAR(36) PRIMARY KEY,
    ORDER_NUMBER VARCHAR(50) NOT NULL UNIQUE,
    USER_ID VARCHAR(36) NOT NULL,
    CUST_NAME VARCHAR(200),
    CUST_EMAIL VARCHAR(100),
    CUST_PHONE VARCHAR(20),
    STATUS INT NOT NULL DEFAULT 0,
    AMOUNT INT(19) NOT NULL DEFAULT 0,
    DISCOUNT_AMOUNT INT(19) DEFAULT 0,
    SHIP_FEE INT(19) DEFAULT 0,
    SHIP_ADDR VARCHAR(500),
    NOTES VARCHAR(1000),
    SHIPPED_DATE DATETIME,
    DELIVERED_DATE DATETIME,
    CANCELLED_DATE DATETIME,
    CREATED_DATE DATETIME NOT NULL,
    UPDATED_DATE DATETIME NOT NULL,
    CREATED_BY VARCHAR(36),
    UPDATED_BY VARCHAR(36)
)
```

**order_items Table:**
```sql
CREATE TABLE IF NOT EXISTS order_items (
    ID VARCHAR(36) PRIMARY KEY,
    ORDER_ID VARCHAR(36) NOT NULL,
    PRODUCT_ID VARCHAR(36) NOT NULL,
    PRODUCT_NAME VARCHAR(255) NOT NULL,
    PRODUCT_SKU VARCHAR(100),
    PRODUCT_IMAGE VARCHAR(500),
    QUANTITY INT NOT NULL DEFAULT 1,
    UNIT_PRICE INT(19) NOT NULL DEFAULT 0,
    DISCOUNT_AMOUNT INT(19) DEFAULT 0,
    TAX_AMOUNT INT(19) DEFAULT 0,
    TOTAL_PRICE INT(19) NOT NULL DEFAULT 0,
    CREATED_DATE DATETIME NOT NULL,
    UPDATED_DATE DATETIME NOT NULL,
    CREATED_BY VARCHAR(36),
    UPDATED_BY VARCHAR(36),
    FOREIGN KEY (ORDER_ID) REFERENCES orders(ID) ON DELETE CASCADE
)
```

### Field Name Mappings

#### Order Entity Changes:
| Old Field | New Field | Notes |
|-----------|-----------|-------|
| customerName | custName | Customer name |
| customerEmail | custEmail | Customer email |
| customerPhone | custPhone | Customer phone |
| totalAmount | amount | Total order amount |
| shippingAmount | shipFee | Shipping fee |
| shippingAddress | shipAddr | Shipping address |
| billingAddress | **REMOVED** | Not in new schema |
| customerNotes | notes | Customer notes |
| adminNotes | **REMOVED** | Not in new schema |
| orderDate | **REMOVED** | Use createdDate instead |
| confirmedDate | **REMOVED** | Not in new schema |
| taxAmount | **REMOVED** | Tax calculated at item level only |
| finalAmount | **REMOVED** | Use amount instead |
| currency | **REMOVED** | Not in new schema |
| paymentStatus | **REMOVED** | Handled by payment service |
| paymentStatusName | **REMOVED** | Handled by payment service |
| paymentStatusDescription | **REMOVED** | Handled by payment service |
| paymentMethod | **REMOVED** | Handled by payment service |
| transactionId | **REMOVED** | Handled by payment service |
| paymentDate | **REMOVED** | Handled by payment service |
| paymentFailureReason | **REMOVED** | Handled by payment service |

#### OrderItem Entity Changes:
| Old Field | New Field | Notes |
|-----------|-----------|-------|
| **No Change** | currency | **REMOVED** - Not in new schema |

### Files Updated

#### 1. **Entities**
- [Order.java](d:\1.Project\microservice\order-service\src\main\java\com\order\domain\model\Order.java)
  - Updated field names to match database schema
  - Added @lombok.Builder.Default annotations
  - Removed payment-related fields
  - Removed currency, billingAddress, adminNotes

- [OrderItem.java](d:\1.Project\microservice\order-service\src\main\java\com\order\domain\model\OrderItem.java)
  - Added @lombok.Builder.Default annotations
  - Removed currency field

#### 2. **Repositories**
- [OrderRepository.java](d:\1.Project\microservice\order-service\src\main\java\com\order\domain\repository\OrderRepository.java)
  - Updated method names: `findByOrderDateDesc()` → `findByCreatedDateDesc()`
  - Updated query fields: `customerName` → `custName`, `customerEmail` → `custEmail`
  - Updated date range query: `orderDate` → `createdDate`
  - Updated method name: `findByCustomerEmail()` → `findByCustEmail()`

#### 3. **DTOs**
- [OrderResponse.java](d:\1.Project\microservice\order-service\src\main\java\com\order\application\dto\OrderResponse.java)
  - Updated field names to match new schema
  - Removed payment-related fields
  - Removed currency, billingAddress, adminNotes, taxAmount

- [OrderItemResponse.java](d:\1.Project\microservice\order-service\src\main\java\com\order\application\dto\OrderItemResponse.java)
  - Removed currency field

- [CreateOrderRequest.java](d:\1.Project\microservice\order-service\src\main\java\com\order\application\dto\CreateOrderRequest.java)
  - Updated field names: customerName → custName, etc.
  - Removed: billingAddress, taxAmount, currency, paymentMethod
  - Updated validation methods

#### 4. **Commands**
- [CreateOrderCommand.java](d:\1.Project\microservice\order-service\src\main\java\com\order\application\command\CreateOrderCommand.java)
  - Updated entity mapping to use new field names
  - Removed payment-related processing
  - Removed currency handling
  - Updated calculation logic

## 🔄 API Changes

### Request/Response Examples

**Create Order Request:**
```json
{
  "custName": "John Doe",
  "custEmail": "john@example.com",
  "custPhone": "0123456789",
  "items": [
    {
      "productId": "prod-123",
      "productName": "Product Name",
      "productSku": "SKU-123",
      "quantity": 2,
      "unitPrice": 100.00,
      "discountAmount": 10.00,
      "taxAmount": 5.00
    }
  ],
  "shipAddr": "123 Main St, City, Country",
  "discountAmount": 20.00,
  "shipFee": 15.00,
  "notes": "Please deliver between 9AM-5PM"
}
```

**Order Response:**
```json
{
  "id": "order-uuid",
  "orderNumber": "ORD-20260526143022-ABC12345",
  "userId": "user-uuid",
  "custName": "John Doe",
  "custEmail": "john@example.com",
  "custPhone": "0123456789",
  "status": 0,
  "statusName": "Pending",
  "amount": 190.00,
  "discountAmount": 20.00,
  "shipFee": 15.00,
  "shipAddr": "123 Main St, City, Country",
  "notes": "Please deliver between 9AM-5PM",
  "shippedDate": null,
  "deliveredDate": null,
  "cancelledDate": null,
  "items": [...],
  "createdBy": "user-uuid",
  "updatedBy": "user-uuid",
  "createdDate": "2026-05-26T14:30:22",
  "updatedDate": "2026-05-26T14:30:22"
}
```

## 📋 Migration Checklist

- [x] Update Order entity fields
- [x] Update OrderItem entity fields
- [x] Update repository methods
- [x] Update DTOs
- [x] Update CreateOrderCommand
- [x] Update CreateOrderRequest validation
- [x] Remove payment processing from order service
- [x] Update calculation logic

## ⚠️ Breaking Changes

### Removed Functionality:
1. **Payment processing** - Moved to payment-service (event-driven architecture)
2. **Currency support** - All amounts in default currency
3. **Billing address** - Only shipping address stored
4. **Admin notes** - Only customer notes stored
5. **Order date** - Use createdDate instead
6. **Confirmed date** - Not tracked in new schema

### API Impact:
- Create order request no longer requires `paymentMethod`
- Response no longer includes payment status information
- Response field names have changed (see mapping table above)

## 🚀 Usage

### Create Order via REST API:
```bash
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <jwt-token>" \
  -d '{
    "custName": "John Doe",
    "custEmail": "john@example.com",
    "custPhone": "0123456789",
    "items": [{
      "productId": "prod-123",
      "productName": "Product Name",
      "quantity": 2,
      "unitPrice": 100.00
    }],
    "shipAddr": "123 Main St",
    "shipFee": 15.00
  }'
```

### Get Order by ID:
```bash
curl -X GET http://localhost:8080/orders/{orderId} \
  -H "Authorization: Bearer <jwt-token>"
```

## 📝 Notes

- Payment processing is now handled asynchronously through Kafka events
- Order statuses: 0=Pending, 1=Confirmed, 2=Processing, 3=Shipped, 4=Delivered, 5=Cancelled, 6=Refunded
- All monetary values are stored as INT(19) - amounts in smallest currency unit (cents, vnd, etc.)
- Order total is calculated as: `itemsTotal + shipFee - discountAmount`

---

**Last Updated**: 2026-05-26  
**Version**: 2.0.0  
**Status**: ✅ Completed
