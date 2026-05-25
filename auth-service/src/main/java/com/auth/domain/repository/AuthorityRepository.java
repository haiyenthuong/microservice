package com.auth.domain.repository;

import com.auth.domain.model.Authority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho Authority entity
 *
 * Cung cấp các phương thức CRUD và query tùy chỉnh
 * cho Authority entity
 *
 * @author Auth Service
 * @version 1.0.0
 */
@Repository
public interface AuthorityRepository extends JpaRepository<Authority, String> {

    /**
     * Tìm authority bằng tên authority
     *
     * @param authority tên authority cần tìm
     * @return Authority nếu tìm thấy, empty nếu không
     */
    Optional<Authority> findByAuthority(String authority);

    /**
     * Kiểm tra authority đã tồn tại chưa
     *
     * @param authority tên authority cần kiểm tra
     * @return true nếu authority đã tồn tại
     */
    boolean existsByAuthority(String authority);

    /**
     * Tìm authority theo FID (Functional ID)
     *
     * @param fid FID cần tìm
     * @return List của authorities có FID tương ứng
     */
    List<Authority> findByFid(String fid);

    /**
     * Tìm tất cả authorities, sắp xếp theo orderId
     *
     * @return List của authorities theo thứ tự
     */
    @Query("SELECT a FROM Authority a ORDER BY a.orderId ASC")
    List<Authority> findAllOrderByOrderId();

    /**
     * Tìm authority theo authKey
     *
     * @param authKey auth key cần tìm
     * @return Authority nếu tìm thấy, empty nếu không
     */
    Optional<Authority> findByAuthKey(String authKey);

    /**
     * Tìm authority theo keyword (search trong authority, description)
     *
     * @param keyword từ khóa tìm kiếm
     * @return List của authorities khớp với keyword
     */
    @Query("SELECT a FROM Authority a WHERE a.authority LIKE %:keyword% OR a.description LIKE %:keyword%")
    List<Authority> searchAuthorities(@Param("keyword") String keyword);

    /**
     * Tìm authority theo keyword (search trong authority, description) - Alias method
     *
     * @param keyword từ khóa tìm kiếm
     * @return List của authorities khớp với keyword
     */
    default List<Authority> searchByAuthorityOrDescription(String keyword) {
        return searchAuthorities(keyword);
    }
}
