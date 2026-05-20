package com.cms.application.query;

import com.cms.application.dto.UserResponse;
import com.cms.domain.common.ResourceNotFoundException;
import com.cms.domain.model.User;
import com.cms.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetUserByIdQuery implements IQuery {

    private final UserRepository userRepository;

    /**
     * Lấy thông tin người dùng theo ID.
     *
     * @param userId ID người dùng cần tìm
     * @return UserResponse thông tin người dùng
     * @throws ResourceNotFoundException nếu người dùng không tồn tại
     */
    public UserResponse execute(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

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
