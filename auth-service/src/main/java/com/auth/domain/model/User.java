package com.auth.domain.model;

import com.auth.domain.enums.UserStatus;
import com.auth.domain.enums.UserType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Entity representing User trong hệ thống
 *
 * Bảng: adm_users
 *
 * Chứa thông tin người dùng:
 * - Thông tin đăng nhập: username, password
 * - Thông tin cá nhân: fullname, mobile, address
 * - Trạng thái: status, type
 *
 * @author Auth Service
 * @version 1.0.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "adm_users")
public class User extends BaseEntity {

    /**
     * Username để đăng nhập (unique)
     */
    @NotBlank(message = "U  sername is required")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    @Column(name = "username", length = 20, nullable = false, unique = true)
    private String username;

    /**
     * Password đã được mã hóa (BCrypt)
     */
    @NotBlank(message = "Password is required")
    @Size(min = 60, max = 200, message = "Password length must be between 60 and 200 characters")
    @Column(name = "password", nullable = false, length = 200)
    private String password;

    /**
     * Họ và tên đầy đủ
     */
    @Size(max = 100, message = "Fullname must not exceed 100 characters")
    @Column(name = "fullname", length = 100)
    private String fullname;

    /**
     * Loại user (ADMIN, CUSTOMER)
     */
    @Column(name = "type")
    private Integer type = 1; // Default: CUSTOMER

    /**
     * Trạng thái user (ACTIVE, LOCKED, DELETED)
     */
    @Column(name = "status")
    private Integer status = 1; // Default: ACTIVE

    /**
     * Số điện thoại
     */
    @Size(max = 10, message = "Mobile must not exceed 10 characters")
    @Column(name = "mobile", length = 10)
    private String mobile;

    /**
     * Địa chỉ
     */
    @Size(max = 500, message = "Address must not exceed 500 characters")
    @Column(name = "address", length = 500)
    private String address;

    /**
     * Email (optional)
     */
    @Email(message = "Email must be valid")
    @Column(name = "email", length = 100)
    private String email;

    /**
     * JWT token (transient, not stored in DB)
     */
    @Transient
    private String token;

    /**
     * Refresh token (transient, not stored in DB)
     */
    @Transient
    private String refreshToken;

    // ==================== User Status Methods ====================

    /**
     * Lấy trạng thái của user
     *
     * @return UserStatus enum
     */
    public UserStatus getUserStatus() {
        return UserStatus.fromValue(status != null ? status : 1);
    }

    /**
     * Thiết lập trạng thái cho user
     *
     * @param userStatus trạng thái cần thiết lập
     */
    public void setUserStatus(UserStatus userStatus) {
        this.status = userStatus.getValue();
        setUpdatedDate(LocalDateTime.now());
    }

    /**
     * Kiểm tra user đang hoạt động
     *
     * @return true nếu đang hoạt động
     */
    public boolean isActive() {
        return getUserStatus().isActive();
    }

    /**
     * Kiểm tra user bị khóa
     *
     * @return true nếu bị khóa
     */
    public boolean isLocked() {
        return getUserStatus().isLocked();
    }

    /**
     * Kiểm tra user bị xóa
     *
     * @return true nếu bị xóa
     */
    public boolean isDeleted() {
        return getUserStatus().isDeleted();
    }

    // ==================== User Type Methods ====================

    /**
     * Lấy loại user
     *
     * @return UserType enum
     */
    public UserType getUserType() {
        return UserType.fromValue(type != null ? type : 1);
    }

    /**
     * Thiết lập loại cho user
     *
     * @param userType loại user cần thiết lập
     */
    public void setUserType(UserType userType) {
        this.type = userType.getValue();
        setUpdatedDate(LocalDateTime.now());
    }

    /**
     * Kiểm tra có phải admin không
     *
     * @return true nếu là admin
     */
    public boolean isAdmin() {
        return getUserType().isAdmin();
    }

    /**
     * Kiểm tra có phải customer không
     *
     * @return true nếu là customer
     */
    public boolean isCustomer() {
        return getUserType().isCustomer();
    }

    // ==================== Action Methods ====================

    /**
     * Khóa tài khoản user
     */
    public void lock() {
        this.status = UserStatus.LOCKED.getValue();
        setUpdatedDate(LocalDateTime.now());
    }

    /**
     * Mở khóa tài khoản user
     */
    public void unlock() {
        this.status = UserStatus.ACTIVE.getValue();
        setUpdatedDate(LocalDateTime.now());
    }

    /**
     * Đánh dấu user đã bị xóa
     */
    public void markAsDeleted() {
        this.status = UserStatus.DELETED.getValue();
        setUpdatedDate(LocalDateTime.now());
    }

    /**
     * Cập nhật mật khẩu (đã được mã hóa)
     *
     * @param encodedPassword mật khẩu đã mã hóa BCrypt
     */
    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
        setUpdatedDate(LocalDateTime.now());
    }

    /**
     * Cập nhật thông tin profile
     *
     * @param fullname họ và tên
     * @param mobile   số điện thoại
     * @param address  địa chỉ
     * @param email    email
     */
    public void updateProfile(String fullname, String mobile, String address, String email) {
        this.fullname = fullname;
        this.mobile = mobile;
        this.address = address;
        this.email = email;
        setUpdatedDate(LocalDateTime.now());
    }
}
