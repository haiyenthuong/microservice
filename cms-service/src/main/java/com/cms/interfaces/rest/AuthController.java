package com.cms.interfaces.rest;

import com.cms.application.command.ChangePasswordCommand;
import com.cms.application.command.LoginCommand;
import com.cms.application.command.RegisterCommand;
import com.cms.application.dto.*;
import com.cms.application.query.GetUserByIdQuery;
import com.cms.infrastructure.helper.SecurityHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication APIs")
public class AuthController {

    private final SecurityHelper securityHelper;
    private final LoginCommand loginCommand;
    private final RegisterCommand registerCommand;
    private final ChangePasswordCommand changePasswordCommand;
    private final GetUserByIdQuery getUserByIdQuery;

    /**
     * Đăng nhập và trả về JWT token.
     *
     * @param request thông tin đăng nhập
     * @return JWT token và thông tin người dùng
     */
    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticate user and return JWT token")
    public Response<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = loginCommand.execute(request);
        return Response.success("Login successful", response);
    }

    /**
     * Đăng ký người dùng mới.
     *
     * @param request thông tin đăng ký
     * @return JWT token và thông tin người dùng mới
     */
    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Create a new user account")
    public Response<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        LoginResponse response = registerCommand.execute(request);
        return Response.success("Registration successful", response);
    }

    /**
     * Thay đổi mật khẩu người dùng hiện tại.
     *
     * @param request thông tin mật khẩu cũ và mới
     * @return response xác nhận thay đổi thành công
     */
    @PostMapping("/change-password")
    @Operation(summary = "Change password", description = "Change current user password")
    public Response<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        String currentUserId = securityHelper.getCurrentUserId();
        changePasswordCommand.execute(currentUserId, request);
        return Response.success("Password changed successfully", null);
    }

    /**
     * Lấy thông tin người dùng đang đăng nhập.
     *
     * @return thông tin người dùng hiện tại
     */
    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Get current logged in user information")
    public Response<UserResponse> getCurrentUser() {
        String currentUserId = securityHelper.getCurrentUserId();
        UserResponse response = getUserByIdQuery.execute(currentUserId);
        return Response.success(response);
    }
}
