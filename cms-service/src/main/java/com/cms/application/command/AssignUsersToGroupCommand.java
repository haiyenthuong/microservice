package com.cms.application.command;

import com.cms.application.dto.AssignUsersToGroupRequest;
import com.cms.domain.common.ResourceNotFoundException;
import com.cms.domain.model.Group;
import com.cms.domain.model.GroupUser;
import com.cms.domain.model.User;
import com.cms.domain.repository.GroupRepository;
import com.cms.domain.repository.GroupUserRepository;
import com.cms.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AssignUsersToGroupCommand implements ICommand {

    private final GroupRepository groupRepository;
    private final GroupUserRepository groupUserRepository;
    private final UserRepository userRepository;

    /**
     * Gán người dùng vào nhóm (thay thế danh sách hiện tại).
     *
     * @param request thông tin nhóm và danh sách người dùng
     * @param currentUserId ID của người đang thực hiện thao tác
     * @return danh sách ID người dùng đã được gán
     * @throws ResourceNotFoundException nếu nhóm không tồn tại
     */
    @Transactional
    public List<String> execute(AssignUsersToGroupRequest request, String currentUserId) {
        Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new ResourceNotFoundException("Group", "id", request.getGroupId()));

        groupUserRepository.deleteByGroupId(request.getGroupId());

        List<String> assignedUserIds = new ArrayList<>();
        for (String userId : request.getUserIds()) {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                GroupUser groupUser = GroupUser.builder()
                        .group(group)
                        .user(user)
                        .createdBy(currentUserId)
                        .createdDate(LocalDateTime.now())
                        .build();
                groupUserRepository.save(groupUser);
                assignedUserIds.add(userId);
            }
        }

        return assignedUserIds;
    }
}
