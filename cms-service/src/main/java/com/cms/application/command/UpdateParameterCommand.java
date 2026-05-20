package com.cms.application.command;

import com.cms.application.dto.ParameterResponse;
import com.cms.application.dto.UpdateParameterRequest;
import com.cms.domain.common.ResourceNotFoundException;
import com.cms.domain.model.Parameter;
import com.cms.domain.repository.ParameterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class UpdateParameterCommand implements ICommand {

    private final ParameterRepository parameterRepository;

    /**
     * Cập nhật thông tin tham số hệ thống.
     *
     * @param parameterId ID tham số cần cập nhật
     * @param request thông tin cần cập nhật
     * @param currentUserId ID của người đang thực hiện thao tác
     * @return ParameterResponse thông tin tham số sau khi cập nhật
     * @throws ResourceNotFoundException nếu tham số không tồn tại
     */
    @Transactional
    public ParameterResponse execute(String parameterId, UpdateParameterRequest request, String currentUserId) {
        Parameter parameter = parameterRepository.findById(parameterId)
                .orElseThrow(() -> new ResourceNotFoundException("Parameter", "id", parameterId));

        if (request.getParamValue() != null) {
            parameter.setParamValue(request.getParamValue());
        }
        if (request.getParamName() != null) {
            parameter.setParamName(request.getParamName());
        }
        if (request.getDescription() != null) {
            parameter.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            parameter.setStatus(request.getStatus());
        }

        parameter.setUpdatedBy(currentUserId);
        parameter.setUpdatedDate(LocalDateTime.now());

        parameter = parameterRepository.save(parameter);

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
