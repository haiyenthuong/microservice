package com.cms.application.command;

import com.cms.application.dto.LoginResponse;
import com.cms.application.dto.RegisterRequest;
import com.cms.application.dto.UserResponse;
import com.cms.domain.model.User;
import com.cms.domain.model.UserType;
import com.cms.domain.repository.UserRepository;
import com.cms.domain.common.DuplicateResourceException;
import com.cms.infrastructure.config.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class RegisterCommand {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * Đăng ký người dùng mới và tạo token JWT.
     *
     * @param request thông tin đăng ký
     * @return LoginResponse thông tin đăng nhập bao gồm token và user
     * @throws DuplicateResourceException nếu username đã tồn tại
     */
    @Transactional
    public LoginResponse execute(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("User", "username", request.getUsername());
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullname(request.getFullname())
                .type(request.getType() != null ? request.getType() : UserType.CUSTOMER.getValue())
                .mobile(request.getMobile())
                .address(request.getAddress())
                .status(1)
                .build();

        user = userRepository.save(user);

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
