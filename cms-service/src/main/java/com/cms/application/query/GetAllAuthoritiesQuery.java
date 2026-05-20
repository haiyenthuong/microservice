package com.cms.application.query;

import com.cms.application.dto.AuthorityResponse;
import com.cms.domain.model.Authority;
import com.cms.domain.repository.AuthorityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GetAllAuthoritiesQuery implements IQuery {

    private final AuthorityRepository authorityRepository;

    /**
     * Lấy danh sách tất cả quyền trong hệ thống.
     *
     * @return danh sách quyền
     */
    public List<AuthorityResponse> execute() {
        List<Authority> authorities = authorityRepository.findAll();
        return authorities.stream()
                .map(this::mapToAuthorityResponse)
                .collect(Collectors.toList());
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
