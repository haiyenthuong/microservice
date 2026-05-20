package com.order.domain.repository;

import com.order.domain.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface cho Order entity.
 * Interface repository dùng để thao tác dữ liệu Order.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    /**
     * Tìm đơn hàng theo số đơn hàng.
     *
     * @param orderNumber số đơn hàng
     * @return Optional chứa đơn hàng nếu tìm thấy
     */
    Optional<Order> findByOrderNumber(String orderNumber);

    /**
     * Kiểm tra đơn hàng tồn tại theo số đơn hàng.
     *
     * @param orderNumber số đơn hàng
     * @return true nếu đơn hàng tồn tại, false nếu không
     */
    boolean existsByOrderNumber(String orderNumber);

    /**
     * Tìm tất cả đơn hàng theo user ID.
     *
     * @param userId ID người dùng
     * @return danh sách đơn hàng
     */
    List<Order> findByUserId(String userId);

    /**
     * Tìm đơn hàng theo user ID, sắp xếp theo ngày đặt giảm dần.
     *
     * @param userId ID người dùng
     * @return danh sách đơn hàng sắp xếp theo ngày đặt giảm dần
     */
    List<Order> findByUserIdOrderByOrderDateDesc(String userId);

    /**
     * Tìm tất cả đơn hàng theo trạng thái.
     *
     * @param status giá trị trạng thái
     * @return danh sách đơn hàng
     */
    List<Order> findByStatus(Integer status);

    /**
     * Tìm đơn hàng theo user ID và trạng thái.
     *
     * @param userId ID người dùng
     * @param status giá trị trạng thái
     * @return danh sách đơn hàng
     */
    @Query("SELECT o FROM Order o WHERE o.userId = :userId AND o.status = :status")
    List<Order> findByUserIdAndStatus(@Param("userId") String userId, @Param("status") Integer status);

    /**
     * Tìm kiếm đơn hàng theo từ khóa trong số đơn hàng hoặc tên khách hàng.
     *
     * @param keyword từ khóa tìm kiếm
     * @return danh sách đơn hàng khớp
     */
    @Query("SELECT o FROM Order o WHERE o.orderNumber LIKE %:keyword% OR o.customerName LIKE %:keyword%")
    List<Order> searchOrders(@Param("keyword") String keyword);

    /**
     * Tìm đơn hàng theo khoảng ngày.
     *
     * @param startDate ngày bắt đầu
     * @param endDate ngày kết thúc
     * @return danh sách đơn hàng
     */
    @Query("SELECT o FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate")
    List<Order> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Tìm đơn hàng theo email khách hàng.
     *
     * @param email email khách hàng
     * @return danh sách đơn hàng
     */
    List<Order> findByCustomerEmail(String email);

    /**
     * Đếm đơn hàng theo trạng thái.
     *
     * @param status giá trị trạng thái
     * @return số lượng đơn hàng
     */
    long countByStatus(Integer status);
}
