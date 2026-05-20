package com.cms.application.command;

import com.cms.domain.common.ResourceNotFoundException;
import com.cms.domain.model.Authority;
import com.cms.domain.repository.AuthorityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DeleteAuthorityCommand implements ICommand {

    private final AuthorityRepository authorityRepository;

    /**
     * Xóa quyền trong hệ thống.
     *
     * @param authorityId ID quyền cần xóa
     * @throws ResourceNotFoundException nếu quyền không tồn tại
     */
    @Transactional
    public void execute(String authorityId) {
        Authority authority = authorityRepository.findById(authorityId)
                .orElseThrow(() -> new ResourceNotFoundException("Authority", "id", authorityId));

        authorityRepository.delete(authority);
    }
}
