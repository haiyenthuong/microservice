package com.cms.application.query;

import com.cms.application.dto.ParameterResponse;
import com.cms.domain.common.ResourceNotFoundException;
import com.cms.domain.model.Parameter;
import com.cms.domain.repository.ParameterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetParameterByIdQuery implements IQuery {

    private final ParameterRepository parameterRepository;

    /**
     * Lấy thông tin tham số theo ID.
     *
     * @param parameterId ID tham số cần tìm
     * @return ParameterResponse thông tin tham số
     * @throws ResourceNotFoundException nếu tham số không tồn tại
     */
    public ParameterResponse execute(String parameterId) {
        Parameter parameter = parameterRepository.findById(parameterId)
                .orElseThrow(() -> new ResourceNotFoundException("Parameter", "id", parameterId));

        return mapToParameterResponse(parameter);
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
