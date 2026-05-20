package com.cms.domain.repository;

import com.cms.domain.model.GroupAuthority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupAuthorityRepository extends JpaRepository<GroupAuthority, String> {

    /**
     * Tìm kiếm tất cả GroupAuthority theo ID nhóm.
     *
     * @param groupId ID nhóm
     * @return danh sách GroupAuthority
     */
    @Query("SELECT ga FROM GroupAuthority ga WHERE ga.group.id = :groupId")
    List<GroupAuthority> findByGroupId(@Param("groupId") String groupId);

    /**
     * Tìm kiếm tất cả GroupAuthority theo ID quyền.
     *
     * @param authorityId ID quyền
     * @return danh sách GroupAuthority
     */
    @Query("SELECT ga FROM GroupAuthority ga WHERE ga.authority.id = :authorityId")
    List<GroupAuthority> findByAuthorityId(@Param("authorityId") String authorityId);

    /**
     * Tìm kiếm tất cả quyền của một nhóm.
     *
     * @param groupId ID nhóm
     * @return danh sách quyền
     */
    @Query("SELECT ga.authority FROM GroupAuthority ga WHERE ga.group.id = :groupId")
    List<com.cms.domain.model.Authority> findAuthoritiesByGroupId(@Param("groupId") String groupId);

    /**
     * Kiểm tra nhóm có quyền hay không.
     *
     * @param groupId ID nhóm
     * @param authorityId ID quyền
     * @return true nếu nhóm có quyền
     */
    @Query("SELECT COUNT(ga) > 0 FROM GroupAuthority ga WHERE ga.group.id = :groupId AND ga.authority.id = :authorityId")
    boolean existsByGroupIdAndAuthorityId(@Param("groupId") String groupId, @Param("authorityId") String authorityId);

    /**
     * Xóa tất cả GroupAuthority theo ID nhóm.
     *
     * @param groupId ID nhóm
     */
    @Query("DELETE FROM GroupAuthority ga WHERE ga.group.id = :groupId")
    void deleteByGroupId(@Param("groupId") String groupId);

    /**
     * Xóa tất cả GroupAuthority theo ID quyền.
     *
     * @param authorityId ID quyền
     */
    @Query("DELETE FROM GroupAuthority ga WHERE ga.authority.id = :authorityId")
    void deleteByAuthorityId(@Param("authorityId") String authorityId);
}
