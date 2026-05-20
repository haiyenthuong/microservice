package com.cms.interfaces.rest;

import com.cms.application.command.CreateAuthorityCommand;
import com.cms.application.command.DeleteAuthorityCommand;
import com.cms.application.command.UpdateAuthorityCommand;
import com.cms.application.dto.*;
import com.cms.application.query.GetAllAuthoritiesQuery;
import com.cms.application.query.GetAuthorityByIdQuery;
import com.cms.infrastructure.helper.SecurityHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/authorities")
@RequiredArgsConstructor
@Tag(name = "Authority Management", description = "Authority/Permission management APIs")
public class AuthorityController {

    private final SecurityHelper securityHelper;
    private final GetAllAuthoritiesQuery getAllAuthoritiesQuery;
    private final GetAuthorityByIdQuery getAuthorityByIdQuery;
    private final CreateAuthorityCommand createAuthorityCommand;
    private final UpdateAuthorityCommand updateAuthorityCommand;
    private final DeleteAuthorityCommand deleteAuthorityCommand;

    /**
     * Lấy danh sách tất cả quyền.
     *
     * @return danh sách quyền
     */
    @GetMapping
    @Operation(summary = "Get all authorities", description = "Retrieve all authorities/permissions")
    public Response<List<AuthorityResponse>> getAllAuthorities() {
        List<AuthorityResponse> response = getAllAuthoritiesQuery.execute();
        return Response.success(response);
    }

    /**
     * Lấy thông tin quyền theo ID.
     *
     * @param id ID quyền
     * @return thông tin quyền
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get authority by ID", description = "Retrieve a specific authority by ID")
    public Response<AuthorityResponse> getAuthorityById(@PathVariable String id) {
        AuthorityResponse response = getAuthorityByIdQuery.execute(id);
        return Response.success(response);
    }

    /**
     * Tạo mới quyền trong hệ thống.
     *
     * @param request thông tin quyền cần tạo
     * @return thông tin quyền vừa được tạo
     */
    @PostMapping
    @Operation(summary = "Create authority", description = "Create a new authority/permission")
    public Response<AuthorityResponse> createAuthority(@Valid @RequestBody CreateAuthorityRequest request) {
        AuthorityResponse response = createAuthorityCommand.execute(request, securityHelper.getCurrentUserId());
        return Response.success("Authority created successfully", response);
    }

    /**
     * Cập nhật thông tin quyền.
     *
     * @param id      ID quyền cần cập nhật
     * @param request thông tin cần cập nhật
     * @return thông tin quyền sau khi cập nhật
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update authority", description = "Update authority information")
    public Response<AuthorityResponse> updateAuthority(
            @PathVariable String id,
            @Valid @RequestBody UpdateAuthorityRequest request) {
        AuthorityResponse response = updateAuthorityCommand.execute(id, request, securityHelper.getCurrentUserId());
        return Response.success("Authority updated successfully", response);
    }

    /**
     * Xóa quyền trong hệ thống.
     *
     * @param id ID quyền cần xóa
     * @return response xác nhận xóa thành công
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete authority", description = "Delete an authority")
    public Response<Void> deleteAuthority(@PathVariable String id) {
        deleteAuthorityCommand.execute(id);
        return Response.success("Authority deleted successfully", null);
    }
}
