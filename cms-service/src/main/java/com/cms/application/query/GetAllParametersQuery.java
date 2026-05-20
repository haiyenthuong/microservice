package com.cms.application.query;

import com.cms.application.dto.ParameterResponse;
import com.cms.domain.model.Parameter;
import com.cms.domain.repository.ParameterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GetAllParametersQuery implements IQuery {

    private final ParameterRepository parameterRepository;

    /**
     * Lấy danh sách tất cả tham số hệ thống.
     *
     * @return danh sách tham số
     */
    public List<ParameterResponse> execute() {
        List<Parameter> parameters = parameterRepository.findAll();
        return parameters.stream()
                .map(this::mapToParameterResponse)
                .collect(Collectors.toList());
    }

    /**
     * Chuyển đổi entity Parameter sang ParameterResponse.
     *
     * @param parameter entity tham số
     * @return ParameterResponse DTO
     */
    private ParameterResponse mapToParameterResponse(Parameter parameter) {
        return ParameterResponse.builder()
                .id(parameter.getId())
                .paramKey(parameter.getParamKey())
                .paramValue(parameter.getParamValue())
                .paramName(parameter.getParamName())
                .description(parameter.getDescription())
                .status(parameter.getStatus())
                .createdBy(parameter.getCreatedBy())
                .updatedBy(parameter.getUpdatedBy())
                .createdDate(parameter.getCreatedDate())
                .updatedDate(parameter.getUpdatedDate())
                .build();
    }
}
