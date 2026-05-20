package com.cms.application.command;

import com.cms.application.dto.CreateParameterRequest;
import com.cms.application.dto.ParameterResponse;
import com.cms.domain.common.DuplicateResourceException;
import com.cms.domain.model.Parameter;
import com.cms.domain.repository.ParameterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class CreateParameterCommand implements ICommand {

    private final ParameterRepository parameterRepository;

    /**
     * Tạo mới tham số hệ thống.
     *
     * @param request thông tin tham số cần tạo
     * @param currentUserId ID của người đang thực hiện thao tác
     * @return ParameterResponse thông tin tham số vừa được tạo
     * @throws DuplicateResourceException nếu param key đã tồn tại
     */
    @Transactional
    public ParameterResponse execute(CreateParameterRequest request, String currentUserId) {
        if (parameterRepository.existsByParamKey(request.getParamKey())) {
            throw new DuplicateResourceException("Parameter", "paramKey", request.getParamKey());
        }

        Parameter parameter = Parameter.builder()
                .paramKey(request.getParamKey())
                .paramValue(request.getParamValue())
                .paramName(request.getParamName())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : 1)
                .createdBy(currentUserId)
                .createdDate(LocalDateTime.now())
                .build();

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
