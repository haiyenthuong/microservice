package com.cms.application.command;

import com.cms.application.dto.UpdateUserRequest;
import com.cms.application.dto.UserResponse;
import com.cms.domain.common.ResourceNotFoundException;
import com.cms.domain.model.User;
import com.cms.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UpdateUserCommand {

    private final UserRepository userRepository;

    /**
     * Cập nhật thông tin người dùng.
     *
     * @param userId ID người dùng cần cập nhật
     * @param request thông tin cần cập nhật
     * @param currentUserId ID của người đang thực hiện thao tác
     * @return UserResponse thông tin người dùng sau khi cập nhật
     * @throws ResourceNotFoundException nếu người dùng không tồn tại
     */
    @Transactional
    public UserResponse execute(String userId, UpdateUserRequest request, String currentUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.updateProfile(
                request.getFullname(),
                request.getMobile(),
                request.getAddress()
        );

        if (currentUserId != null) {
            user.setUpdatedBy(currentUserId);
        }

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
