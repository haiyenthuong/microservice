package com.order.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * UserDetailsService cho order-service.
 * Simplified version - chỉ validate token từ cms-service, không load user từ database.
 */
@Slf4j
@Service
public class CustomUserDetailsService implements UserDetailsService {

    /**
     * Tải thông tin người dùng theo username.
     * Trong order-service, user được quản lý bởi cms-service.
     * Method này chỉ trả về UserDetails cơ bản cho JWT validation.
     *
     * @param username tên đăng nhập
     * @return UserDetails thông tin người dùng cho Spring Security
     * @throws UsernameNotFoundException nếu username là null hoặc empty
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username == null || username.isEmpty()) {
            throw new UsernameNotFoundException("Username cannot be null or empty");
        }

        log.debug("Loading user details for username: {}", username);

        // Return UserDetails cơ bản - không query database vì user thuộc cms-service
        return org.springframework.security.core.userdetails.User.builder()
                .username(username)
                .password("") // Không cần password vì dùng JWT
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .accountLocked(false)
                .disabled(false)
                .build();
    }
}
