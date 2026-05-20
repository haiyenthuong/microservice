package com.cms.application.query;

import com.cms.application.dto.AuthorityResponse;
import com.cms.domain.common.ResourceNotFoundException;
import com.cms.domain.model.Authority;
import com.cms.domain.repository.AuthorityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetAuthorityByIdQuery implements IQuery {

    private final AuthorityRepository authorityRepository;

    /**
     * Lấy thông tin quyền theo ID.
     *
     * @param authorityId ID quyền cần tìm
     * @return AuthorityResponse thông tin quyền
     * @throws ResourceNotFoundException nếu quyền không tồn tại
     */
    public AuthorityResponse execute(String authorityId) {
        Authority authority = authorityRepository.findById(authorityId)
                .orElseThrow(() -> new ResourceNotFoundException("Authority", "id", authorityId));

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
