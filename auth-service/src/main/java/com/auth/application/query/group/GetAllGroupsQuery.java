package com.auth.application.query.group;

import com.auth.application.dto.GroupResponse;
import com.auth.domain.model.Group;
import com.auth.domain.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Query handler for retrieving all groups
 *
 * @author Auth Service
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
public class GetAllGroupsQuery {

    private final GroupRepository groupRepository;

    /**
     * Execute query to get all groups
     *
     * @return List of GroupResponse
     */
    @Transactional(readOnly = true)
    public List<GroupResponse> execute() {
        List<Group> groups = groupRepository.findAll();
        return groups.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Map Group entity to GroupResponse DTO
     */
    private GroupResponse mapToResponse(Group group) {
        return GroupResponse.builder()
                .id(group.getId())
                .groupName(group.getGroupName())
                .description(group.getDescription())
                .type(group.getType())
                .status(group.getStatus())
                .authority(group.getAuthority())
                .createdDate(group.getCreatedDate())
                .updatedDate(group.getUpdatedDate())
                .build();
    }
}
