package com.cms.domain.repository;

import com.cms.domain.model.GroupUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupUserRepository extends JpaRepository<GroupUser, String> {

    /**
     * Tìm kiếm tất cả GroupUser theo ID người dùng.
     *
     * @param userId ID người dùng
     * @return danh sách GroupUser
     */
    @Query("SELECT gu FROM GroupUser gu WHERE gu.user.id = :userId")
    List<GroupUser> findByUserId(@Param("userId") String userId);

    /**
     * Tìm kiếm tất cả GroupUser theo ID nhóm.
     *
     * @param groupId ID nhóm
     * @return danh sách GroupUser
     */
    @Query("SELECT gu FROM GroupUser gu WHERE gu.group.id = :groupId")
    List<GroupUser> findByGroupId(@Param("groupId") String groupId);

    /**
     * Tìm kiếm tất cả nhóm mà người dùng thuộc về.
     *
     * @param userId ID người dùng
     * @return danh sách nhóm
     */
    @Query("SELECT gu.group FROM GroupUser gu WHERE gu.user.id = :userId")
    List<com.cms.domain.model.Group> findGroupsByUserId(@Param("userId") String userId);

    /**
     * Tìm kiếm tất cả người dùng thuộc một nhóm.
     *
     * @param groupId ID nhóm
     * @return danh sách người dùng
     */
    @Query("SELECT gu.user FROM GroupUser gu WHERE gu.group.id = :groupId")
    List<com.cms.domain.model.User> findUsersByGroupId(@Param("groupId") String groupId);

    /**
     * Kiểm tra người dùng có thuộc nhóm hay không.
     *
     * @param userId ID người dùng
     * @param groupId ID nhóm
     * @return true nếu người dùng thuộc nhóm
     */
    @Query("SELECT COUNT(gu) > 0 FROM GroupUser gu WHERE gu.user.id = :userId AND gu.group.id = :groupId")
    boolean existsByUserIdAndGroupId(@Param("userId") String userId, @Param("groupId") String groupId);

    /**
     * Xóa tất cả GroupUser theo ID người dùng.
     *
     * @param userId ID người dùng
     */
    @Query("DELETE FROM GroupUser gu WHERE gu.user.id = :userId")
    void deleteByUserId(@Param("userId") String userId);

    /**
     * Xóa tất cả GroupUser theo ID nhóm.
     *
     * @param groupId ID nhóm
     */
    @Query("DELETE FROM GroupUser gu WHERE gu.group.id = :groupId")
    void deleteByGroupId(@Param("groupId") String groupId);
}
