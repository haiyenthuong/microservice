package com.cms.application.query;

import com.cms.application.dto.UserResponse;
import com.cms.domain.model.User;
import com.cms.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GetAllUsersQuery implements IQuery {

    private final UserRepository userRepository;

    /**
     * Lấy danh sách tất cả người dùng.
     *
     * @return danh sách người dùng
     */
    public List<UserResponse> execute() {
        List<User> users = userRepository.findAll();

        return users.stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
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
