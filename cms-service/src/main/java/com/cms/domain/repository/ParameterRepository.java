package com.cms.domain.repository;

import com.cms.domain.model.Parameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParameterRepository extends JpaRepository<Parameter, String> {

    /**
     * Tìm kiếm tham số theo param key.
     *
     * @param paramKey key của tham số
     * @return Optional chứa Parameter nếu tìm thấy
     */
    Optional<Parameter> findByParamKey(String paramKey);

    /**
     * Kiểm tra tham số có tồn tại theo param key hay không.
     *
     * @param paramKey key của tham số cần kiểm tra
     * @return true nếu param key đã tồn tại
     */
    boolean existsByParamKey(String paramKey);

    /**
     * Lấy danh sách tất cả tham số có trạng thái khác với trạng thái cho trước.
     *
     * @param status trạng thái cần loại bỏ
     * @return danh sách tham số hoạt động
     */
    @Query("SELECT p FROM Parameter p WHERE p.status <> :status ORDER BY p.paramName")
    List<Parameter> findAllActive(@Param("status") Integer status);

    /**
     * Tìm kiếm tham số theo từ khóa trong param key hoặc param name.
     *
     * @param keyword từ khóa tìm kiếm
     * @return danh sách tham số khớp với từ khóa
     */
    @Query("SELECT p FROM Parameter p WHERE p.paramKey LIKE %:keyword% OR p.paramName LIKE %:keyword%")
    List<Parameter> searchByKeyOrName(@Param("keyword") String keyword);
}
