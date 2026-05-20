package com.cms.application.command;

import com.cms.application.dto.ChangePasswordRequest;
import com.cms.domain.common.AuthenticationException;
import com.cms.domain.common.BusinessException;
import com.cms.domain.model.User;
import com.cms.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ChangePasswordCommand implements ICommand {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Thay đổi mật khẩu người dùng.
     *
     * @param userId ID người dùng cần đổi mật khẩu
     * @param request thông tin mật khẩu cũ và mới
     * @throws AuthenticationException nếu người dùng không tồn tại
     * @throws BusinessException nếu mật khẩu cũ không đúng hoặc mật khẩu mới trùng mật khẩu cũ
     */
    @Transactional
    public void execute(String userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException("Current password is incorrect");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BusinessException("New password must be different from current password");
        }

        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
