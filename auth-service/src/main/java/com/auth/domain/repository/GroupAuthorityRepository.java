package com.auth.domain.repository;

import com.auth.domain.model.Authority;
import com.auth.domain.model.Group;
import com.auth.domain.model.GroupAuthority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho GroupAuthority entity (Many-to-Many: Group ↔ Authority)
 *
 * Cung cấp các phương thức CRUD và query tùy chỉnh
 * cho GroupAuthority entity
 *
 * @author Auth Service
 * @version 1.0.0
 */
@Repository
public interface GroupAuthorityRepository extends JpaRepository<GroupAuthority, String> {

    /**
     * Tìm tất cả GroupAuthority theo group
     *
     * @param group group cần tìm
     * @return List của GroupAuthority của group đó
     */
    List<GroupAuthority> findByGroup(Group group);

    /**
     * Tìm tất cả GroupAuthority theo authority
     *
     * @param authority authority cần tìm
     * @return List của GroupAuthority của authority đó
     */
    List<GroupAuthority> findByAuthority(Authority authority);

    /**
     * Tìm GroupAuthority theo group và authority
     *
     * @param group group cần tìm
     * @param authority authority cần tìm
     * @return GroupAuthority nếu tìm thấy, empty nếu không
     */
    @Query("SELECT ga FROM GroupAuthority ga WHERE ga.group = :group AND ga.authority = :authority")
    Optional<GroupAuthority> findByGroupAndAuthority(@Param("group") Group group, @Param("authority") Authority authority);

    /**
     * Kiểm tra group đã có authority chưa
     *
     * @param group group cần kiểm tra
     * @param authority authority cần kiểm tra
     * @return true nếu group đã có authority đó
     */
    @Query("SELECT COUNT(ga) > 0 FROM GroupAuthority ga WHERE ga.group = :group AND ga.authority = :authority")
    boolean existsByGroupAndAuthority(@Param("group") Group group, @Param("authority") Authority authority);

    /**
     * Xóa tất cả authorities của một group
     *
     * @param group group cần xóa authorities
     */
    void deleteByGroup(Group group);

    /**
     * Đếm số authorities của một group
     *
     * @param group group cần đếm
     * @return số lượng authorities của group
     */
    @Query("SELECT COUNT(ga) FROM GroupAuthority ga WHERE ga.group = :group")
    long countByGroup(@Param("group") Group group);

    /**
     * Đếm số groups của một authority
     *
     * @param authority authority cần đếm
     * @return số lượng groups có authority đó
     */
    @Query("SELECT COUNT(ga) FROM GroupAuthority ga WHERE ga.authority = :authority")
    long countByAuthority(@Param("authority") Authority authority);
}
