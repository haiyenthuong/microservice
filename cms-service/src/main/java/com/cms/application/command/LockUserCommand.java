package com.cms.application.command;

import com.cms.application.dto.UserResponse;
import com.cms.domain.common.BusinessException;
import com.cms.domain.common.ResourceNotFoundException;
import com.cms.domain.model.User;
import com.cms.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class LockUserCommand {

    private final UserRepository userRepository;

    /**
     * Khóa tài khoản người dùng.
     *
     * @param userId ID người dùng cần khóa
     * @param currentUserId ID của người đang thực hiện thao tác
     * @return UserResponse thông tin người dùng sau khi khóa
     * @throws ResourceNotFoundException nếu người dùng không tồn tại
     * @throws BusinessException nếu người dùng đã bị khóa
     */
    @Transactional
    public UserResponse execute(String userId, String currentUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (user.isLocked()) {
            throw new BusinessException("User is already locked");
        }

        user.lock();
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
