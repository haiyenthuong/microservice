package com.auth.application.query.parameter;

import com.auth.application.dto.ParameterResponse;
import com.auth.domain.model.Parameter;
import com.auth.domain.repository.ParameterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Query handler for retrieving all parameters
 *
 * @author Auth Service
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
public class GetAllParametersQuery {

    private final ParameterRepository parameterRepository;

    /**
     * Execute query to get all parameters
     *
     * @return List of ParameterResponse
     */
    @Transactional(readOnly = true)
    public List<ParameterResponse> execute() {
        List<Parameter> parameters = parameterRepository.findAll();
        return parameters.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Map Parameter entity to ParameterResponse DTO
     */
    private ParameterResponse mapToResponse(Parameter parameter) {
        return ParameterResponse.builder()
                .id(parameter.getId())
                .paramKey(parameter.getParamKey())
                .paramValue(parameter.getParamValue())
                .paramName(parameter.getParamName())
                .description(parameter.getDescription())
                .status(parameter.getStatus())
                .createdDate(parameter.getCreatedDate())
                .updatedDate(parameter.getUpdatedDate())
                .build();
    }
}
