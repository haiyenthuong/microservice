package com.order.infrastructure.config;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Custom UserDetails implementation chứa cả userId.
 * Copy từ cms-service.
 */
@Getter
public class CustomUserDetails implements UserDetails {

    private final String userId;
    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;

    /**
     * Constructor đầy đủ.
     *
     * @param userId ID người dùng (được extract từ JWT)
     * @param username Tên đăng nhập
     * @param password Mật khẩu (có thể null khi dùng JWT)
     * @param authorities Danh sách quyền hạn
     */
    public CustomUserDetails(
            String userId,
            String username,
            String password,
            Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.authorities = authorities != null ? authorities : Collections.emptyList();
    }

    /**
     * Constructor tạo CustomUserDetails với userId và username.
     * Sử dụng cho JWT authentication (không cần password).
     *
     * @param userId ID người dùng
     * @param username Tên đăng nhập
     */
    public CustomUserDetails(String userId, String username) {
        this(userId, username, null, Collections.emptyList());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
