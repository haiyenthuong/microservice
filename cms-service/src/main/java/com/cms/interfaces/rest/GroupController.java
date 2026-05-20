package com.cms.interfaces.rest;

import com.cms.application.command.*;
import com.cms.application.dto.*;
import com.cms.application.query.GetAllGroupsQuery;
import com.cms.application.query.GetGroupByIdQuery;
import com.cms.infrastructure.helper.SecurityHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/groups")
@RequiredArgsConstructor
@Tag(name = "Group Management", description = "Group management APIs")
public class GroupController {

    private final SecurityHelper securityHelper;
    private final GetAllGroupsQuery getAllGroupsQuery;
    private final GetGroupByIdQuery getGroupByIdQuery;
    private final CreateGroupCommand createGroupCommand;
    private final UpdateGroupCommand updateGroupCommand;
    private final DeleteGroupCommand deleteGroupCommand;
    private final AssignUsersToGroupCommand assignUsersToGroupCommand;
    private final AssignAuthoritiesToGroupCommand assignAuthoritiesToGroupCommand;

    /**
     * Lấy danh sách tất cả nhóm.
     *
     * @return danh sách nhóm
     */
    @GetMapping
    @Operation(summary = "Get all groups", description = "Retrieve all groups")
    public Response<List<GroupResponse>> getAllGroups() {
        List<GroupResponse> response = getAllGroupsQuery.execute();
        return Response.success(response);
    }

    /**
     * Lấy thông tin nhóm theo ID.
     *
     * @param id ID nhóm
     * @return thông tin nhóm
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get group by ID", description = "Retrieve a specific group by ID")
    public Response<GroupResponse> getGroupById(@PathVariable String id) {
        GroupResponse response = getGroupByIdQuery.execute(id);
        return Response.success(response);
    }

    /**
     * Tạo mới nhóm.
     *
     * @param request thông tin nhóm cần tạo
     * @return thông tin nhóm vừa được tạo
     */
    @PostMapping
    @Operation(summary = "Create group", description = "Create a new group")
    public Response<GroupResponse> createGroup(@Valid @RequestBody CreateGroupRequest request) {
        GroupResponse response = createGroupCommand.execute(request, securityHelper.getCurrentUserId());
        return Response.success("Group created successfully", response);
    }

    /**
     * Cập nhật thông tin nhóm.
     *
     * @param id ID nhóm cần cập nhật
     * @param request thông tin cần cập nhật
     * @return thông tin nhóm sau khi cập nhật
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update group", description = "Update group information")
    public Response<GroupResponse> updateGroup(
            @PathVariable String id,
            @Valid @RequestBody UpdateGroupRequest request) {
        GroupResponse response = updateGroupCommand.execute(id, request, securityHelper.getCurrentUserId());
        return Response.success("Group updated successfully", response);
    }

    /**
     * Xóa nhóm.
     *
     * @param id ID nhóm cần xóa
     * @return response xác nhận xóa thành công
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete group", description = "Delete a group")
    public Response<Void> deleteGroup(@PathVariable String id) {
        deleteGroupCommand.execute(id);
        return Response.success("Group deleted successfully", null);
    }

    /**
     * Gán người dùng vào nhóm.
     *
     * @param request thông tin nhóm và danh sách người dùng
     * @return danh sách ID người dùng đã được gán
     */
    @PostMapping("/assign-users")
    @Operation(summary = "Assign users to group", description = "Assign users to a group")
    public Response<List<String>> assignUsersToGroup(@Valid @RequestBody AssignUsersToGroupRequest request) {
        List<String> response = assignUsersToGroupCommand.execute(request, securityHelper.getCurrentUserId());
        return Response.success("Users assigned to group successfully", response);
    }

    /**
     * Gán quyền vào nhóm.
     *
     * @param request thông tin nhóm và danh sách quyền
     * @return danh sách ID quyền đã được gán
     */
    @PostMapping("/assign-authorities")
    @Operation(summary = "Assign authorities to group", description = "Assign authorities to a group")
    public Response<List<String>> assignAuthoritiesToGroup(@Valid @RequestBody AssignAuthoritiesToGroupRequest request) {
        List<String> response = assignAuthoritiesToGroupCommand.execute(request, securityHelper.getCurrentUserId());
        return Response.success("Authorities assigned to group successfully", response);
    }
}
