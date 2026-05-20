package com.cms.application.query;

import com.cms.application.dto.GroupResponse;
import com.cms.domain.model.Group;
import com.cms.domain.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GetAllGroupsQuery implements IQuery {

    private final GroupRepository groupRepository;

    /**
     * Lấy danh sách tất cả nhóm.
     *
     * @return danh sách nhóm
     */
    public List<GroupResponse> execute() {
        List<Group> groups = groupRepository.findAll();
        return groups.stream()
                .map(this::mapToGroupResponse)
                .collect(Collectors.toList());
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
