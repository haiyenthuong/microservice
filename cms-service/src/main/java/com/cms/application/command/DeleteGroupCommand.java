package com.cms.application.command;

import com.cms.domain.common.ResourceNotFoundException;
import com.cms.domain.model.Group;
import com.cms.domain.repository.GroupRepository;
import com.cms.domain.repository.GroupAuthorityRepository;
import com.cms.domain.repository.GroupUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DeleteGroupCommand implements ICommand {

    private final GroupRepository groupRepository;
    private final GroupAuthorityRepository groupAuthorityRepository;
    private final GroupUserRepository groupUserRepository;

    /**
     * Xóa nhóm và tất cả quyền, người dùng liên quan.
     *
     * @param groupId ID nhóm cần xóa
     * @throws ResourceNotFoundException nếu nhóm không tồn tại
     */
    @Transactional
    public void execute(String groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group", "id", groupId));

        groupAuthorityRepository.deleteByGroupId(groupId);
        groupUserRepository.deleteByGroupId(groupId);
        groupRepository.delete(group);
    }
}
