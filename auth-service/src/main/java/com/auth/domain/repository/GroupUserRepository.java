package com.auth.domain.repository;

import com.auth.domain.model.Group;
import com.auth.domain.model.GroupUser;
import com.auth.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho GroupUser entity (Many-to-Many: Group ↔ User)
 *
 * Cung cấp các phương thức CRUD và query tùy chỉnh
 * cho GroupUser entity
 *
 * @author Auth Service
 * @version 1.0.0
 */
@Repository
public interface GroupUserRepository extends JpaRepository<GroupUser, String> {

    /**
     * Tìm tất cả GroupUser theo user
     *
     * @param user user cần tìm
     * @return List của GroupUser của user đó
     */
    List<GroupUser> findByUser(User user);

    /**
     * Tìm tất cả GroupUser theo group
     *
     * @param group group cần tìm
     * @return List của GroupUser của group đó
     */
    List<GroupUser> findByGroup(Group group);

    /**
     * Tìm GroupUser theo user và group
     *
     * @param user user cần tìm
     * @param group group cần tìm
     * @return GroupUser nếu tìm thấy, empty nếu không
     */
    @Query("SELECT gu FROM GroupUser gu WHERE gu.user = :user AND gu.group = :group")
    Optional<GroupUser> findByUserAndGroup(@Param("user") User user, @Param("group") Group group);

    /**
     * Kiểm tra user đã thuộc group chưa
     *
     * @param user user cần kiểm tra
     * @param group group cần kiểm tra
     * @return true nếu user đã thuộc group đó
     */
    @Query("SELECT COUNT(gu) > 0 FROM GroupUser gu WHERE gu.user = :user AND gu.group = :group")
    boolean existsByUserAndGroup(@Param("user") User user, @Param("group") Group group);

    /**
     * Xóa user khỏi tất cả groups
     *
     * @param user user cần xóa
     */
    void deleteByUser(User user);

    /**
     * Xóa tất cả users khỏi một group
     *
     * @param group group cần xóa
     */
    void deleteByGroup(Group group);

    /**
     * Đếm số users trong một group
     *
     * @param group group cần đếm
     * @return số lượng users trong group
     */
    @Query("SELECT COUNT(gu) FROM GroupUser gu WHERE gu.group = :group")
    long countByGroup(@Param("group") Group group);

    /**
     * Đếm số groups của một user
     *
     * @param user user cần đếm
     * @return số lượng groups của user
     */
    @Query("SELECT COUNT(gu) FROM GroupUser gu WHERE gu.user = :user")
    long countByUser(@Param("user") User user);

    /**
     * Tìm tất cả users của một group
     *
     * @param group group cần tìm users
     * @return List của users trong group
     */
    @Query("SELECT DISTINCT gu.user FROM GroupUser gu WHERE gu.group = :group")
    List<User> findUsersByGroup(@Param("group") Group group);
}
