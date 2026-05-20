package com.cms.application.command;

import com.cms.application.dto.CreateUserRequest;
import com.cms.application.dto.UserResponse;
import com.cms.domain.common.DuplicateResourceException;
import com.cms.domain.model.User;
import com.cms.domain.model.UserType;
import com.cms.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CreateUserCommand implements ICommand {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Tạo mới người dùng trong hệ thống.
     *
     * @param request thông tin người dùng cần tạo
     * @param currentUserId ID của người đang thực hiện thao tác
     * @return UserResponse thông tin người dùng vừa được tạo
     * @throws DuplicateResourceException nếu username đã tồn tại
     */
    @Transactional
    public UserResponse execute(CreateUserRequest request, String currentUserId) {
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
                .createdBy(currentUserId)
                .build();

        user = userRepository.save(user);

        return mapToUserResponse(user);
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
