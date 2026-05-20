package com.order.domain.repository;

import com.order.domain.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface cho OrderItem entity.
 * Interface repository dùng để thao tác dữ liệu OrderItem.
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, String> {

    /**
     * Tìm tất cả items theo order ID.
     *
     * @param orderId ID đơn hàng
     * @return danh sách order items
     */
    List<OrderItem> findByOrderId(String orderId);

    /**
     * Tìm item theo order ID và product ID.
     *
     * @param orderId ID đơn hàng
     * @param productId ID sản phẩm
     * @return Optional chứa order item nếu tìm thấy
     */
    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.id = :orderId AND oi.productId = :productId")
    Optional<OrderItem> findByOrderIdAndProductId(@Param("orderId") String orderId, @Param("productId") String productId);

    /**
     * Tìm tất cả items theo product ID trên tất cả đơn hàng.
     *
     * @param productId ID sản phẩm
     * @return danh sách order items
     */
    @Query("SELECT oi FROM OrderItem oi WHERE oi.productId = :productId")
    List<OrderItem> findByProductId(@Param("productId") String productId);

    /**
     * Đếm items theo order ID.
     *
     * @param orderId ID đơn hàng
     * @return số lượng items
     */
    long countByOrderId(String orderId);

    /**
     * Tìm items theo tên sản phẩm chứa từ khóa.
     *
     * @param productName từ khóa tìm kiếm
     * @return danh sách order items
     */
    List<OrderItem> findByProductNameContainingIgnoreCase(String productName);
}
