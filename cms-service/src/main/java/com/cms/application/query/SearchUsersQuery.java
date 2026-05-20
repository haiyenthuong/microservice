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
public class SearchUsersQuery implements IQuery {

    private final UserRepository userRepository;

    /**
     * Tìm kiếm người dùng theo username hoặc fullname.
     *
     * @param keyword từ khóa tìm kiếm
     * @return danh sách người dùng khớp với từ khóa
     */
    public List<UserResponse> execute(String keyword) {
        List<User> users = userRepository.searchByUsernameOrFullname(keyword);

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
