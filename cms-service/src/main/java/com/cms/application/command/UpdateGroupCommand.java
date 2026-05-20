package com.cms.application.command;

import com.cms.application.dto.GroupResponse;
import com.cms.application.dto.UpdateGroupRequest;
import com.cms.domain.common.ResourceNotFoundException;
import com.cms.domain.model.Group;
import com.cms.domain.model.GroupAuthority;
import com.cms.domain.model.GroupUser;
import com.cms.domain.model.Authority;
import com.cms.domain.model.User;
import com.cms.domain.repository.GroupRepository;
import com.cms.domain.repository.GroupAuthorityRepository;
import com.cms.domain.repository.GroupUserRepository;
import com.cms.domain.repository.AuthorityRepository;
import com.cms.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UpdateGroupCommand implements ICommand {

    private final GroupRepository groupRepository;
    private final GroupAuthorityRepository groupAuthorityRepository;
    private final GroupUserRepository groupUserRepository;
    private final AuthorityRepository authorityRepository;
    private final UserRepository userRepository;

    /**
     * Cập nhật thông tin nhóm và danh sách quyền, người dùng.
     *
     * @param groupId ID nhóm cần cập nhật
     * @param request thông tin cần cập nhật
     * @param currentUserId ID của người đang thực hiện thao tác
     * @return GroupResponse thông tin nhóm sau khi cập nhật
     * @throws ResourceNotFoundException nếu nhóm không tồn tại
     */
    @Transactional
    public GroupResponse execute(String groupId, UpdateGroupRequest request, String currentUserId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group", "id", groupId));

        if (request.getGroupName() != null) {
            group.setGroupName(request.getGroupName());
        }
        if (request.getStatus() != null) {
            group.setStatus(request.getStatus());
        }
        if (request.getAuthority() != null) {
            group.setAuthority(request.getAuthority());
        }
        if (request.getDescription() != null) {
            group.setDescription(request.getDescription());
        }
        if (request.getType() != null) {
            group.setType(request.getType());
        }

        group.setUpdatedBy(currentUserId);
        group.setUpdatedDate(LocalDateTime.now());

        group = groupRepository.save(group);

        List<String> authorityIds = new ArrayList<>();
        if (request.getAuthorityIds() != null) {
            groupAuthorityRepository.deleteByGroupId(groupId);
            for (String authorityId : request.getAuthorityIds()) {
                Authority authority = authorityRepository.findById(authorityId).orElse(null);
                if (authority != null) {
                    GroupAuthority groupAuthority = GroupAuthority.builder()
                            .group(group)
                            .authority(authority)
                            .createdBy(currentUserId)
                            .createdDate(LocalDateTime.now())
                            .build();
                    groupAuthorityRepository.save(groupAuthority);
                    authorityIds.add(authorityId);
                }
            }
        }

        List<String> userIds = new ArrayList<>();
        if (request.getUserIds() != null) {
            groupUserRepository.deleteByGroupId(groupId);
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
                    userIds.add(userId);
                }
            }
        }

        return mapToGroupResponse(group, authorityIds, userIds);
    }

    /**
     * Chuyển đổi entity Group sang GroupResponse.
     *
     * @param group entity nhóm
     * @param authorityIds danh sách ID quyền
     * @param userIds danh sách ID người dùng
     * @return GroupResponse DTO
     */
    private GroupResponse mapToGroupResponse(Group group, List<String> authorityIds, List<String> userIds) {
        return GroupResponse.builder()
                .id(group.getId())
                .groupName(group.getGroupName())
                .status(group.getStatus())
                .authority(group.getAuthority())
                .description(group.getDescription())
                .type(group.getType())
                .createdBy(group.getCreatedBy())
                .updatedBy(group.getUpdatedBy())
                .createdDate(group.getCreatedDate())
                .updatedDate(group.getUpdatedDate())
                .authorityIds(authorityIds)
                .userIds(userIds)
                .build();
    }
}
