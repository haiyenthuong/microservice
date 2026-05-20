package com.cms.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "adm_users")
public class User extends BaseEntity {

    @Column(name = "username", length = 20, nullable = false, unique = true)
    private String username;

    @Column(name = "password", nullable = false, length = 200)
    private String password;

    @Column(name = "fullname", length = 100)
    private String fullname;

    @Column(name = "type")
    private Integer type = 0;

    @Column(name = "status")
    private Integer status = 1;

    @Column(name = "mobile", length = 10)
    private String mobile;

    @Column(name = "address", length = 500)
    private String address;

    @Transient
    private String token;

    // User Status methods
    /**
     * Lấy trạng thái của người dùng.
     *
     * @return UserStatus trạng thái người dùng
     */
    public UserStatus getUserStatus() {
        return UserStatus.fromValue(status != null ? status : 1);
    }

    /**
     * Thiết lập trạng thái cho người dùng.
     *
     * @param userStatus trạng thái cần thiết lập
     */
    public void setUserStatus(UserStatus userStatus) {
        this.status = userStatus.getValue();
    }

    /**
     * Kiểm tra người dùng có đang hoạt động không.
     *
     * @return true nếu người dùng đang hoạt động
     */
    public boolean isActive() {
        return getUserStatus().isActive();
    }

    /**
     * Kiểm tra người dùng có bị khóa không.
     *
     * @return true nếu người dùng bị khóa
     */
    public boolean isLocked() {
        return getUserStatus().isLocked();
    }

    /**
     * Kiểm tra người dùng đã bị xóa chưa.
     *
     * @return true nếu người dùng đã bị xóa
     */
    public boolean isDeleted() {
        return getUserStatus().isDeleted();
    }

    // User Type methods
    /**
     * Lấy loại người dùng.
     *
     * @return UserType loại người dùng
     */
    public UserType getUserType() {
        return UserType.fromValue(type != null ? type : 0);
    }

    /**
     * Thiết lập loại cho người dùng.
     *
     * @param userType loại người dùng cần thiết lập
     */
    public void setUserType(UserType userType) {
        this.type = userType.getValue();
    }

    /**
     * Kiểm tra người dùng có phải là admin không.
     *
     * @return true nếu là admin
     */
    public boolean isAdmin() {
        return getUserType().isAdmin();
    }

    /**
     * Kiểm tra người dùng có phải là khách hàng không.
     *
     * @return true nếu là khách hàng
     */
    public boolean isCustomer() {
        return getUserType().isCustomer();
    }

    // Action methods
    /**
     * Khóa tài khoản người dùng.
     */
    public void lock() {
        this.status = UserStatus.LOCKED.getValue();
        setUpdatedDate(LocalDateTime.now());
    }

    /**
     * Mở khóa tài khoản người dùng.
     */
    public void unlock() {
        this.status = UserStatus.ACTIVE.getValue();
        setUpdatedDate(LocalDateTime.now());
    }

    /**
     * Đánh dấu người dùng đã bị xóa.
     */
    public void markAsDeleted() {
        this.status = UserStatus.DELETED.getValue();
        setUpdatedDate(LocalDateTime.now());
    }

    /**
     * Cập nhật mật khẩu người dùng.
     *
     * @param encodedPassword mật khẩu đã được mã hóa
     */
    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
        setUpdatedDate(LocalDateTime.now());
    }

    /**
     * Cập nhật thông tin hồ sơ người dùng.
     *
     * @param fullname họ và tên
     * @param mobile số điện thoại
     * @param address địa chỉ
     */
    public void updateProfile(String fullname, String mobile, String address) {
        this.fullname = fullname;
        this.mobile = mobile;
        this.address = address;
        setUpdatedDate(LocalDateTime.now());
    }
}
