package com.cms.application.command;

import com.cms.domain.common.BusinessException;
import com.cms.domain.common.ResourceNotFoundException;
import com.cms.domain.model.User;
import com.cms.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DeleteUserCommand {

    private final UserRepository userRepository;

    /**
     * Xóa tài khoản người dùng (soft delete).
     *
     * @param userId ID người dùng cần xóa
     * @param currentUserId ID của người đang thực hiện thao tác
     * @return Void
     * @throws ResourceNotFoundException nếu người dùng không tồn tại
     * @throws BusinessException nếu người dùng đã bị xóa
     */
    @Transactional
    public Void execute(String userId, String currentUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (user.isDeleted()) {
            throw new BusinessException("User is already deleted");
        }

        user.markAsDeleted();
        if (currentUserId != null) {
            user.setUpdatedBy(currentUserId);
        }

        userRepository.save(user);

        return null;
    }
}
