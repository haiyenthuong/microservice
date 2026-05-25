package com.auth.application.query.authority;

import com.auth.application.dto.AuthorityResponse;
import com.auth.domain.model.Authority;
import com.auth.domain.repository.AuthorityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Query handler for retrieving all authorities
 *
 * @author Auth Service
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
public class GetAllAuthoritiesQuery {

    private final AuthorityRepository authorityRepository;

    /**
     * Execute query to get all authorities
     *
     * @return List of AuthorityResponse
     */
    @Transactional(readOnly = true)
    public List<AuthorityResponse> execute() {
        List<Authority> authorities = authorityRepository.findAllOrderByOrderId();
        return authorities.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Map Authority entity to AuthorityResponse DTO
     */
    private AuthorityResponse mapToResponse(Authority authority) {
        return AuthorityResponse.builder()
                .id(authority.getId())
                .authority(authority.getAuthority())
                .fid(authority.getFid())
                .description(authority.getDescription())
                .orderId(authority.getOrderId())
                .authKey(authority.getAuthKey())
                .createdDate(authority.getCreatedDate())
                .updatedDate(authority.getUpdatedDate())
                .build();
    }
}
