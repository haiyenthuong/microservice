package com.cms.application.command;

import com.cms.domain.common.ResourceNotFoundException;
import com.cms.domain.model.Parameter;
import com.cms.domain.repository.ParameterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DeleteParameterCommand implements ICommand {

    private final ParameterRepository parameterRepository;

    /**
     * Xóa tham số hệ thống.
     *
     * @param parameterId ID tham số cần xóa
     * @throws ResourceNotFoundException nếu tham số không tồn tại
     */
    @Transactional
    public void execute(String parameterId) {
        Parameter parameter = parameterRepository.findById(parameterId)
                .orElseThrow(() -> new ResourceNotFoundException("Parameter", "id", parameterId));

        parameterRepository.delete(parameter);
    }
}
