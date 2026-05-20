package com.cms.application.command;

import com.cms.application.dto.AuthorityResponse;
import com.cms.application.dto.UpdateAuthorityRequest;
import com.cms.domain.common.ResourceNotFoundException;
import com.cms.domain.model.Authority;
import com.cms.domain.repository.AuthorityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class UpdateAuthorityCommand implements ICommand {

    private final AuthorityRepository authorityRepository;

    /**
     * Cập nhật thông tin quyền.
     *
     * @param authorityId ID quyền cần cập nhật
     * @param request thông tin cần cập nhật
     * @param currentUserId ID của người đang thực hiện thao tác
     * @return AuthorityResponse thông tin quyền sau khi cập nhật
     * @throws ResourceNotFoundException nếu quyền không tồn tại
     */
    @Transactional
    public AuthorityResponse execute(String authorityId, UpdateAuthorityRequest request, String currentUserId) {
        Authority authority = authorityRepository.findById(authorityId)
                .orElseThrow(() -> new ResourceNotFoundException("Authority", "id", authorityId));

        if (request.getDescription() != null) {
            authority.setDescription(request.getDescription());
        }
        if (request.getOrderId() != null) {
            authority.setOrderId(request.getOrderId());
        }
        if (request.getAuthKey() != null) {
            authority.setAuthKey(request.getAuthKey());
        }

        authority.setUpdatedBy(currentUserId);
        authority.setUpdatedDate(LocalDateTime.now());

        authority = authorityRepository.save(authority);

        return mapToAuthorityResponse(authority);
    }

    /**
     * Chuyển đổi entity Authority sang AuthorityResponse.
     *
     * @param authority entity quyền
     * @return AuthorityResponse DTO
     */
    private AuthorityResponse mapToAuthorityResponse(Authority authority) {
        return AuthorityResponse.builder()
                .id(authority.getId())
                .authority(authority.getAuthority())
                .fid(authority.getFid())
                .description(authority.getDescription())
                .orderId(authority.getOrderId())
                .authKey(authority.getAuthKey())
                .createdBy(authority.getCreatedBy())
                .updatedBy(authority.getUpdatedBy())
                .createdDate(authority.getCreatedDate())
                .updatedDate(authority.getUpdatedDate())
                .build();
    }
}
