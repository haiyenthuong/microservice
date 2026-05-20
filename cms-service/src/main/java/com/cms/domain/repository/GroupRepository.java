package com.cms.domain.repository;

import com.cms.domain.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, String> {

    /**
     * Tìm kiếm nhóm theo tên nhóm.
     *
     * @param groupName tên nhóm cần tìm
     * @return Optional chứa Group nếu tìm thấy
     */
    Optional<Group> findByGroupName(String groupName);

    /**
     * Kiểm tra nhóm có tồn tại theo tên nhóm hay không.
     *
     * @param groupName tên nhóm cần kiểm tra
     * @return true nếu tên nhóm đã tồn tại
     */
    boolean existsByGroupName(String groupName);

    /**
     * Lấy danh sách tất cả nhóm có trạng thái khác với trạng thái cho trước.
     *
     * @param status trạng thái cần loại bỏ
     * @return danh sách nhóm hoạt động
     */
    @Query("SELECT g FROM Group g WHERE g.status <> :status ORDER BY g.groupName")
    List<Group> findAllActive(@Param("status") Integer status);

    /**
     * Tìm kiếm nhóm theo loại và trạng thái khác với trạng thái cho trước.
     *
     * @param type loại nhóm
     * @param status trạng thái cần loại bỏ
     * @return danh sách nhóm khớp điều kiện
     */
    @Query("SELECT g FROM Group g WHERE g.type = :type AND g.status <> :status")
    List<Group> findByTypeAndStatus(@Param("type") Integer type, @Param("status") Integer status);

    /**
     * Tìm kiếm tất cả nhóm mà người dùng thuộc về.
     *
     * @param userId ID người dùng
     * @param status trạng thái cần loại bỏ
     * @return danh sách nhóm của người dùng
     */
    @Query("SELECT DISTINCT g FROM Group g JOIN GroupUser gu ON g.id = gu.group.id WHERE gu.user.id = :userId AND g.status <> :status")
    List<Group> findByUserId(@Param("userId") String userId, @Param("status") Integer status);
}
