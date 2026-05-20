package com.cms.interfaces.rest;

import com.cms.application.command.CreateParameterCommand;
import com.cms.application.command.DeleteParameterCommand;
import com.cms.application.command.UpdateParameterCommand;
import com.cms.application.dto.*;
import com.cms.application.query.GetAllParametersQuery;
import com.cms.application.query.GetParameterByIdQuery;
import com.cms.infrastructure.helper.SecurityHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/parameters")
@RequiredArgsConstructor
@Tag(name = "Parameter Management", description = "System parameter management APIs")
public class ParameterController {

    private final SecurityHelper securityHelper;
    private final GetAllParametersQuery getAllParametersQuery;
    private final GetParameterByIdQuery getParameterByIdQuery;
    private final CreateParameterCommand createParameterCommand;
    private final UpdateParameterCommand updateParameterCommand;
    private final DeleteParameterCommand deleteParameterCommand;

    /**
     * Lấy danh sách tất cả tham số hệ thống.
     *
     * @return danh sách tham số
     */
    @GetMapping
    @Operation(summary = "Get all parameters", description = "Retrieve all system parameters")
    public Response<List<ParameterResponse>> getAllParameters() {
        List<ParameterResponse> response = getAllParametersQuery.execute();
        return Response.success(response);
    }

    /**
     * Lấy thông tin tham số theo ID.
     *
     * @param id ID tham số
     * @return thông tin tham số
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get parameter by ID", description = "Retrieve a specific parameter by ID")
    public Response<ParameterResponse> getParameterById(@PathVariable String id) {
        ParameterResponse response = getParameterByIdQuery.execute(id);
        return Response.success(response);
    }

    /**
     * Tạo mới tham số hệ thống.
     *
     * @param request thông tin tham số cần tạo
     * @return thông tin tham số vừa được tạo
     */
    @PostMapping
    @Operation(summary = "Create parameter", description = "Create a new system parameter")
    public Response<ParameterResponse> createParameter(@Valid @RequestBody CreateParameterRequest request) {
        ParameterResponse response = createParameterCommand.execute(request, securityHelper.getCurrentUserId());
        return Response.success("Parameter created successfully", response);
    }

    /**
     * Cập nhật thông tin tham số hệ thống.
     *
     * @param id ID tham số cần cập nhật
     * @param request thông tin cần cập nhật
     * @return thông tin tham số sau khi cập nhật
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update parameter", description = "Update system parameter information")
    public Response<ParameterResponse> updateParameter(
            @PathVariable String id,
            @Valid @RequestBody UpdateParameterRequest request) {
        ParameterResponse response = updateParameterCommand.execute(id, request, securityHelper.getCurrentUserId());
        return Response.success("Parameter updated successfully", response);
    }

    /**
     * Xóa tham số hệ thống.
     *
     * @param id ID tham số cần xóa
     * @return response xác nhận xóa thành công
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete parameter", description = "Delete a system parameter")
    public Response<Void> deleteParameter(@PathVariable String id) {
        deleteParameterCommand.execute(id);
        return Response.success("Parameter deleted successfully", null);
    }
}
