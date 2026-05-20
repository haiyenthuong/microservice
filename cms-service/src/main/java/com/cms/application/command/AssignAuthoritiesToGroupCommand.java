package com.cms.application.command;

import com.cms.application.dto.AssignAuthoritiesToGroupRequest;
import com.cms.domain.common.ResourceNotFoundException;
import com.cms.domain.model.Authority;
import com.cms.domain.model.Group;
import com.cms.domain.model.GroupAuthority;
import com.cms.domain.repository.AuthorityRepository;
import com.cms.domain.repository.GroupRepository;
import com.cms.domain.repository.GroupAuthorityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AssignAuthoritiesToGroupCommand implements ICommand {

    private final GroupRepository groupRepository;
    private final GroupAuthorityRepository groupAuthorityRepository;
    private final AuthorityRepository authorityRepository;

    /**
     * Gán quyền vào nhóm (thay thế danh sách hiện tại).
     *
     * @param request thông tin nhóm và danh sách quyền
     * @param currentUserId ID của người đang thực hiện thao tác
     * @return danh sách ID quyền đã được gán
     * @throws ResourceNotFoundException nếu nhóm không tồn tại
     */
    @Transactional
    public List<String> execute(AssignAuthoritiesToGroupRequest request, String currentUserId) {
        Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new ResourceNotFoundException("Group", "id", request.getGroupId()));

        groupAuthorityRepository.deleteByGroupId(request.getGroupId());

        List<String> assignedAuthorityIds = new ArrayList<>();
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
                assignedAuthorityIds.add(authorityId);
            }
        }

        return assignedAuthorityIds;
    }
}
