package com.cms.application.command;

import com.cms.application.dto.AuthorityResponse;
import com.cms.application.dto.CreateAuthorityRequest;
import com.cms.domain.common.DuplicateResourceException;
import com.cms.domain.model.Authority;
import com.cms.domain.repository.AuthorityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class CreateAuthorityCommand implements ICommand {

    private final AuthorityRepository authorityRepository;

    /**
     * Tạo mới quyền trong hệ thống.
     *
     * @param request       thông tin quyền cần tạo
     * @param currentUserId ID của người đang thực hiện thao tác
     * @return AuthorityResponse thông tin quyền vừa được tạo
     * @throws DuplicateResourceException nếu quyền đã tồn tại
     */
    @Transactional
    public AuthorityResponse execute(CreateAuthorityRequest request, String currentUserId) {
        if (authorityRepository.existsByAuthority(request.getAuthority())) {
            throw new DuplicateResourceException("Authority", "authority", request.getAuthority());
        }

        Authority authority = Authority.builder()
                .authority(request.getAuthority())
                .fid(request.getFid())
                .description(request.getDescription())
                .orderId(request.getOrderId())
                .authKey(request.getAuthKey())
                .createdBy(currentUserId)
                .createdDate(LocalDateTime.now())
                .updatedBy(currentUserId)
                .updatedDate(LocalDateTime.now())
                .build();

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
