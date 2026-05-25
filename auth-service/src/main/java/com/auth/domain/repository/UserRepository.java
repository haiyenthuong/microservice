package com.auth.domain.repository;

import com.auth.domain.enums.UserStatus;
import com.auth.domain.enums.UserType;
import com.auth.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho User entity
 *
 * Cung cấp các phương thức CRUD và query tùy chỉnh
 * cho User entity
 *
 * @author Auth Service
 * @version 1.0.0
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    /**
     * Tìm user bằng username
     *
     * @param username username cần tìm
     * @return User nếu tìm thấy, empty nếu không
     */
    Optional<User> findByUsername(String username);

    /**
     * Kiểm tra username đã tồn tại chưa
     *
     * @param username username cần kiểm tra
     * @return true nếu username đã tồn tại
     */
    boolean existsByUsername(String username);

    /**
     * Tìm user theo email
     *
     * @param email email cần tìm
     * @return List của users có email đó
     */
    List<User> findByEmail(String email);

    /**
     * Tìm user theo status
     *
     * @param status trạng thái user
     * @return List của users có status tương ứng
     */
    List<User> findByStatus(Integer status);

    /**
     * Tìm user theo type
     *
     * @param type loại user
     * @return List của users có type tương ứng
     */
    List<User> findByType(Integer type);

    /**
     * Tìm user đang hoạt động
     *
     * @return List của active users
     */
    @Query("SELECT u FROM User u WHERE u.status = 1")
    List<User> findActiveUsers();

    /**
     * Tìm user không bị xóa
     *
     * @return List của users chưa bị xóa
     */
    @Query("SELECT u FROM User u WHERE u.status != 3")
    List<User> findNonDeletedUsers();

    /**
     * Tìm user theo username hoặc fullname (search)
     *
     * @param keyword từ khóa tìm kiếm
     * @return List của users khớp với keyword
     */
    @Query("SELECT u FROM User u WHERE u.username LIKE %:keyword% OR u.fullname LIKE %:keyword%")
    List<User> searchByUsernameOrFullname(@Param("keyword") String keyword);

    /**
     * Tìm user theo type và status
     *
     * @param type loại user
     * @param status trạng thái user
     * @return List của users thỏa mãn
     */
    @Query("SELECT u FROM User u WHERE u.type = :type AND u.status = :status")
    List<User> findByTypeAndStatus(@Param("type") Integer type, @Param("status") Integer status);

    /**
     * Đếm số user theo status
     *
     * @param status trạng thái cần đếm
     * @return số lượng user
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.status = :status")
    long countByStatus(@Param("status") Integer status);

    /**
     * Tìm tất cả admins
     *
     * @return List của admin users
     */
    @Query("SELECT u FROM User u WHERE u.type = 0 AND u.status = 1")
    List<User> findActiveAdmins();

    /**
     * Tìm tất cả customers
     *
     * @return List của customer users
     */
    @Query("SELECT u FROM User u WHERE u.type = 1 AND u.status = 1")
    List<User> findActiveCustomers();

    /**
     * Tìm user theo mobile
     *
     * @param mobile số điện thoại
     * @return User nếu tìm thấy, empty nếu không
     */
    Optional<User> findByMobile(String mobile);

    /**
     * Kiểm tra mobile đã tồn tại chưa
     *
     * @param mobile số điện thoại cần kiểm tra
     * @return true nếu mobile đã tồn tại
     */
    boolean existsByMobile(String mobile);
}
