package com.auth.domain.repository;

import com.auth.domain.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho Group entity
 *
 * Cung cấp các phương thức CRUD và query tùy chỉnh
 * cho Group entity
 *
 * @author Auth Service
 * @version 1.0.0
 */
@Repository
public interface GroupRepository extends JpaRepository<Group, String> {

    /**
     * Tìm group bằng tên group
     *
     * @param groupName tên group cần tìm
     * @return Group nếu tìm thấy, empty nếu không
     */
    Optional<Group> findByGroupName(String groupName);

    /**
     * Kiểm tra group name đã tồn tại chưa
     *
     * @param groupName tên group cần kiểm tra
     * @return true nếu group name đã tồn tại
     */
    boolean existsByGroupName(String groupName);

    /**
     * Tìm group theo status
     *
     * @param status trạng thái group
     * @return List của groups có status tương ứng
     */
    List<Group> findByStatus(Integer status);

    /**
     * Tìm group đang hoạt động
     *
     * @return List của active groups
     */
    @Query("SELECT g FROM Group g WHERE g.status = 1")
    List<Group> findActiveGroups();

    /**
     * Tìm group theo type
     *
     * @param type loại group
     * @return List của groups có type tương ứng
     */
    List<Group> findByType(Integer type);

    /**
     * Tìm group theo keyword (search trong groupName, description)
     *
     * @param keyword từ khóa tìm kiếm
     * @return List của groups khớp với keyword
     */
    @Query("SELECT g FROM Group g WHERE g.groupName LIKE %:keyword% OR g.description LIKE %:keyword%")
    List<Group> searchGroups(@Param("keyword") String keyword);

    /**
     * Tìm group theo keyword (search trong groupName, description) - Alias method
     *
     * @param keyword từ khóa tìm kiếm
     * @return List của groups khớp với keyword
     */
    default List<Group> searchByGroupNameOrDescription(String keyword) {
        return searchGroups(keyword);
    }

    /**
     * Tìm group theo status và type
     *
     * @param status trạng thái group
     * @param type loại group
     * @return List của groups thỏa mãn
     */
    @Query("SELECT g FROM Group g WHERE g.status = :status AND g.type = :type")
    List<Group> findByStatusAndType(@Param("status") Integer status, @Param("type") Integer type);
}
