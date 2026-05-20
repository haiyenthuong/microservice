package com.cms.domain.repository;

import com.cms.domain.model.User;
import com.cms.domain.model.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    /**
     * Tìm kiếm người dùng theo username.
     *
     * @param username tên đăng nhập
     * @return Optional chứa User nếu tìm thấy
     */
    Optional<User> findByUsername(String username);

    /**
     * Tìm kiếm người dùng theo username và trạng thái khác với trạng thái cho trước.
     *
     * @param username tên đăng nhập
     * @param status trạng thái cần loại bỏ
     * @return Optional chứa User nếu tìm thấy
     */
    Optional<User> findByUsernameAndStatusNot(String username, Integer status);

    /**
     * Lấy danh sách tất cả người dùng có trạng thái khác với trạng thái cho trước.
     *
     * @param status trạng thái cần loại bỏ
     * @return danh sách người dùng
     */
    @Query("SELECT u FROM User u WHERE u.status <> :status")
    List<User> findAllActive(@Param("status") Integer status);

    /**
     * Tìm kiếm người dùng theo username hoặc fullname chứa từ khóa.
     *
     * @param keyword từ khóa tìm kiếm
     * @return danh sách người dùng khớp với từ khóa
     */
    @Query("SELECT u FROM User u WHERE u.username LIKE %:keyword% OR u.fullname LIKE %:keyword%")
    List<User> searchByUsernameOrFullname(@Param("keyword") String keyword);

    /**
     * Tìm kiếm người dùng theo trạng thái.
     *
     * @param status trạng thái cần tìm
     * @return danh sách người dùng có trạng thái tương ứng
     */
    @Query("SELECT u FROM User u WHERE u.status = :status")
    List<User> findByStatus(@Param("status") Integer status);

    // User Type queries
    /**
     * Tìm kiếm người dùng theo loại.
     *
     * @param type loại người dùng
     * @return danh sách người dùng có loại tương ứng
     */
    List<User> findByType(Integer type);

    /**
     * Tìm kiếm người dùng theo loại và trạng thái khác với trạng thái cho trước.
     *
     * @param type loại người dùng
     * @param status trạng thái cần loại bỏ
     * @return danh sách người dùng khớp điều kiện
     */
    @Query("SELECT u FROM User u WHERE u.type = :type AND u.status <> :status")
    List<User> findByTypeAndStatusNot(@Param("type") Integer type, @Param("status") Integer status);

    /**
     * Tìm kiếm người dùng theo loại và từ khóa trong username hoặc fullname.
     *
     * @param type loại người dùng
     * @param keyword từ khóa tìm kiếm
     * @return danh sách người dùng khớp điều kiện
     */
    @Query("SELECT u FROM User u WHERE u.type = :type AND (u.username LIKE %:keyword% OR u.fullname LIKE %:keyword%)")
    List<User> searchByTypeAndKeyword(@Param("type") Integer type, @Param("keyword") String keyword);

    /**
     * Kiểm tra người dùng có tồn tại theo username hay không.
     *
     * @param username tên đăng nhập cần kiểm tra
     * @return true nếu username đã tồn tại
     */
    boolean existsByUsername(String username);

    /**
     * Kiểm tra người dùng khác có tồn tại theo username hay không.
     *
     * @param username tên đăng nhập cần kiểm tra
     * @param id ID người dùng cần loại bỏ
     * @return true nếu username đã tồn tại ở người dùng khác
     */
    boolean existsByUsernameAndIdNot(String username, String id);

    /**
     * Kiểm tra người dùng hoạt động có tồn tại theo username hay không.
     *
     * @param username tên đăng nhập cần kiểm tra
     * @param status trạng thái không hoạt động
     * @return true nếu người dùng hoạt động đã tồn tại
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.username = :username AND u.status <> :status")
    boolean existsActiveUserByUsername(@Param("username") String username, @Param("status") Integer status);
}
