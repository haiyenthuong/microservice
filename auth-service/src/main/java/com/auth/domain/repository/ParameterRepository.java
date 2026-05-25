package com.auth.domain.repository;

import com.auth.domain.model.Parameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho Parameter entity
 *
 * Cung cấp các phương thức CRUD và query tùy chỉnh
 * cho Parameter entity
 *
 * @author Auth Service
 * @version 1.0.0
 */
@Repository
public interface ParameterRepository extends JpaRepository<Parameter, String> {

    /**
     * Tìm parameter bằng param key
     *
     * @param paramKey key cần tìm
     * @return Parameter nếu tìm thấy, empty nếu không
     */
    Optional<Parameter> findByParamKey(String paramKey);

    /**
     * Kiểm tra param key đã tồn tại chưa
     *
     * @param paramKey key cần kiểm tra
     * @return true nếu param key đã tồn tại
     */
    boolean existsByParamKey(String paramKey);

    /**
     * Tìm parameter theo status
     *
     * @param status trạng thái parameter
     * @return List của parameters có status tương ứng
     */
    List<Parameter> findByStatus(Integer status);

    /**
     * Tìm tất cả active parameters
     *
     * @return List của active parameters
     */
    @Query("SELECT p FROM Parameter p WHERE p.status = 1")
    List<Parameter> findActiveParameters();

    /**
     * Tìm parameter theo keyword (search trong paramKey, paramName, description)
     *
     * @param keyword từ khóa tìm kiếm
     * @return List của parameters khớp với keyword
     */
    @Query("SELECT p FROM Parameter p WHERE p.paramKey LIKE %:keyword% OR p.paramName LIKE %:keyword% OR p.description LIKE %:keyword%")
    List<Parameter> searchParameters(@Param("keyword") String keyword);

    /**
     * Tìm parameter theo keyword (search trong paramKey, paramName) - Alias method
     *
     * @param keyword từ khóa tìm kiếm
     * @return List của parameters khớp với keyword
     */
    default List<Parameter> searchByParamKeyOrParamName(String keyword) {
        return searchParameters(keyword);
    }

    /**
     * Tìm parameter theo prefix của paramKey
     *
     * @param prefix prefix cần tìm
     * @return List của parameters có key bắt đầu bằng prefix
     */
    @Query("SELECT p FROM Parameter p WHERE p.paramKey LIKE :prefix%")
    List<Parameter> findByParamKeyStartingWith(@Param("prefix") String prefix);

    /**
     * Lấy value của parameter bằng key
     *
     * @param paramKey key cần lấy value
     * @return Optional của paramValue nếu tìm thấy, empty nếu không
     */
    @Query("SELECT p.paramValue FROM Parameter p WHERE p.paramKey = :paramKey AND p.status = 1")
    Optional<String> findValueByKey(@Param("paramKey") String paramKey);

    /**
     * Kiểm tra parameter có active không
     *
     * @param paramKey key cần kiểm tra
     * @return true nếu parameter tồn tại và active
     */
    @Query("SELECT COUNT(p) > 0 FROM Parameter p WHERE p.paramKey = :paramKey AND p.status = 1")
    boolean isParameterActive(@Param("paramKey") String paramKey);
}
