package com.cms.application.query;

import com.cms.application.dto.GroupResponse;
import com.cms.domain.common.ResourceNotFoundException;
import com.cms.domain.model.Group;
import com.cms.domain.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetGroupByIdQuery implements IQuery {

    private final GroupRepository groupRepository;

    /**
     * Lấy thông tin nhóm theo ID.
     *
     * @param groupId ID nhóm cần tìm
     * @return GroupResponse thông tin nhóm
     * @throws ResourceNotFoundException nếu nhóm không tồn tại
     */
    public GroupResponse execute(String groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group", "id", groupId));

        return mapToGroupResponse(group);
    }

    /**
     * Chuyển đổi entity Group sang GroupResponse.
     *
     * @param group entity nhóm
     * @return GroupResponse DTO
     */
    private GroupResponse mapToGroupResponse(Group group) {
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
                .build();
    }
}
