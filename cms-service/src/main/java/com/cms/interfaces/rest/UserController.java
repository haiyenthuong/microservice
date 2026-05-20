package com.cms.interfaces.rest;

import com.cms.application.command.*;
import com.cms.application.dto.*;
import com.cms.application.query.GetAllUsersQuery;
import com.cms.application.query.GetUsersByTypeQuery;
import com.cms.application.query.GetUserByIdQuery;
import com.cms.application.query.SearchUsersQuery;
import com.cms.infrastructure.helper.SecurityHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "User management APIs")
public class UserController {

    private final SecurityHelper securityHelper;
    private final GetAllUsersQuery getAllUsersQuery;
    private final GetUserByIdQuery getUserByIdQuery;
    private final SearchUsersQuery searchUsersQuery;
    private final GetUsersByTypeQuery getUsersByTypeQuery;
    private final CreateUserCommand createUserCommand;
    private final UpdateUserCommand updateUserCommand;
    private final LockUserCommand lockUserCommand;
    private final UnlockUserCommand unlockUserCommand;
    private final DeleteUserCommand deleteUserCommand;

    /**
     * Lấy danh sách tất cả người dùng.
     *
     * @return danh sách người dùng
     */
    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieve all users")
    public Response<List<UserResponse>> getAllUsers() {
        List<UserResponse> response = getAllUsersQuery.execute();
        return Response.success(response);
    }

    /**
     * Lấy thông tin người dùng theo ID.
     *
     * @param id ID người dùng
     * @return thông tin người dùng
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieve a specific user by ID")
    public Response<UserResponse> getUserById(@PathVariable String id) {
        UserResponse response = getUserByIdQuery.execute(id);
        return Response.success(response);
    }

    /**
     * Tìm kiếm người dùng theo username hoặc fullname.
     *
     * @param keyword từ khóa tìm kiếm
     * @return danh sách người dùng khớp với từ khóa
     */
    @GetMapping("/search")
    @Operation(summary = "Search users", description = "Search users by username or fullname")
    public Response<List<UserResponse>> searchUsers(@RequestParam String keyword) {
        List<UserResponse> response = searchUsersQuery.execute(keyword);
        return Response.success(response);
    }

    /**
     * Lấy danh sách người dùng theo loại.
     *
     * @param type loại người dùng (0: ADMIN, 1: CUSTOMER)
     * @return danh sách người dùng có loại tương ứng
     */
    @GetMapping("/type/{type}")
    @Operation(summary = "Get users by type", description = "Get users by type (0: ADMIN, 1: CUSTOMER)")
    public Response<List<UserResponse>> getUsersByType(
            @PathVariable
            @Parameter(description = "User type: 0=ADMIN, 1=CUSTOMER")
            Integer type) {
        List<UserResponse> response = getUsersByTypeQuery.execute(type);
        return Response.success(response);
    }

    /**
     * Tạo mới người dùng.
     *
     * @param request thông tin người dùng cần tạo
     * @return thông tin người dùng vừa được tạo
     */
    @PostMapping
    @Operation(summary = "Create user", description = "Create a new user")
    public Response<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse response = createUserCommand.execute(request, securityHelper.getCurrentUserId());
        return Response.success("User created successfully", response);
    }

    /**
     * Cập nhật thông tin người dùng.
     *
     * @param id ID người dùng cần cập nhật
     * @param request thông tin cần cập nhật
     * @return thông tin người dùng sau khi cập nhật
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Update user information")
    public Response<UserResponse> updateUser(
            @PathVariable String id,
            @Valid @RequestBody UpdateUserRequest request) {
        UserResponse response = updateUserCommand.execute(id, request, securityHelper.getCurrentUserId());
        return Response.success("User updated successfully", response);
    }

    /**
     * Khóa tài khoản người dùng.
     *
     * @param id ID người dùng cần khóa
     * @return thông tin người dùng sau khi khóa
     */
    @PostMapping("/{id}/lock")
    @Operation(summary = "Lock user", description = "Lock a user account")
    public Response<UserResponse> lockUser(@PathVariable String id) {
        UserResponse response = lockUserCommand.execute(id, securityHelper.getCurrentUserId());
        return Response.success("User locked successfully", response);
    }

    /**
     * Mở khóa tài khoản người dùng.
     *
     * @param id ID người dùng cần mở khóa
     * @return thông tin người dùng sau khi mở khóa
     */
    @PostMapping("/{id}/unlock")
    @Operation(summary = "Unlock user", description = "Unlock a user account")
    public Response<UserResponse> unlockUser(@PathVariable String id) {
        UserResponse response = unlockUserCommand.execute(id, securityHelper.getCurrentUserId());
        return Response.success("User unlocked successfully", response);
    }

    /**
     * Xóa tài khoản người dùng (soft delete).
     *
     * @param id ID người dùng cần xóa
     * @return response xác nhận xóa thành công
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Soft delete a user account")
    public Response<Void> deleteUser(@PathVariable String id) {
        deleteUserCommand.execute(id, securityHelper.getCurrentUserId());
        return Response.success("User deleted successfully", null);
    }
}
