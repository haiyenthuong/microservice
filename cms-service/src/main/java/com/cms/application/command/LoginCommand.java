package com.cms.application.command;

import com.cms.application.dto.LoginRequest;
import com.cms.application.dto.LoginResponse;
import com.cms.application.dto.UserResponse;
import com.cms.domain.common.AuthenticationException;
import com.cms.domain.model.User;
import com.cms.domain.repository.UserRepository;
import com.cms.infrastructure.config.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoginCommand {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * Đăng nhập và tạo token JWT.
     *
     * @param request thông tin đăng nhập
     * @return LoginResponse thông tin đăng nhập bao gồm token và user
     * @throws AuthenticationException nếu thông tin đăng nhập không đúng hoặc tài khoản bị khóa/xóa
     */
    public LoginResponse execute(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AuthenticationException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthenticationException("Invalid username or password");
        }

        if (!user.isActive()) {
            if (user.isLocked()) {
                throw new AuthenticationException("Account is locked");
            }
            if (user.isDeleted()) {
                throw new AuthenticationException("Account has been deleted");
            }
        }

        String token = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return LoginResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .user(mapToUserResponse(user))
                .build();
    }

    /**
     * Chuyển đổi entity User sang UserResponse.
     *
     * @param user entity người dùng
     * @return UserResponse DTO
     */
    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullname(user.getFullname())
                .status(user.getUserStatus())
                .type(user.getUserType())
                .mobile(user.getMobile())
                .address(user.getAddress())
                .createdDate(user.getCreatedDate())
                .updatedDate(user.getUpdatedDate())
                .createdBy(user.getCreatedBy())
                .updatedBy(user.getUpdatedBy())
                .build();
    }
}
