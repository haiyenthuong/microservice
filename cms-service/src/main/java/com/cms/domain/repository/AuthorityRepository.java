package com.cms.domain.repository;

import com.cms.domain.model.Authority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthorityRepository extends JpaRepository<Authority, String> {

    /**
     * Tìm kiếm quyền theo tên quyền.
     *
     * @param authority tên quyền
     * @return Optional chứa Authority nếu tìm thấy
     */
    Optional<Authority> findByAuthority(String authority);

    /**
     * Tìm kiếm quyền theo auth key.
     *
     * @param authKey key xác thực
     * @return Optional chứa Authority nếu tìm thấy
     */
    Optional<Authority> findByAuthKey(String authKey);

    /**
     * Kiểm tra quyền có tồn tại theo tên quyền hay không.
     *
     * @param authority tên quyền cần kiểm tra
     * @return true nếu quyền đã tồn tại
     */
    boolean existsByAuthority(String authority);

    /**
     * Kiểm tra quyền có tồn tại theo auth key hay không.
     *
     * @param authKey key xác thực cần kiểm tra
     * @return true nếu auth key đã tồn tại
     */
    boolean existsByAuthKey(String authKey);

    /**
     * Tìm kiếm tất cả quyền theo chức năng và sắp xếp theo thứ tự.
     *
     * @param fid ID quyền cha
     * @return danh sách quyền con
     */
    @Query("SELECT a FROM Authority a WHERE a.fid = :fid ORDER BY a.orderId")
    List<Authority> findByFidOrderByOrderId(@Param("fid") String fid);

    /**
     * Tìm kiếm quyền theo từ khóa trong tên quyền hoặc mô tả.
     *
     * @param keyword từ khóa tìm kiếm
     * @return danh sách quyền khớp với từ khóa
     */
    @Query("SELECT a FROM Authority a WHERE a.authority LIKE %:keyword% OR a.description LIKE %:keyword%")
    List<Authority> searchByAuthorityOrDescription(@Param("keyword") String keyword);

    /**
     * Lấy tất cả quyền có auth key và sắp xếp theo thứ tự.
     *
     * @return danh sách quyền có auth key
     */
    @Query("SELECT a FROM Authority a WHERE a.authKey IS NOT NULL ORDER BY a.orderId")
    List<Authority> findAllWithAuthKey();
}
