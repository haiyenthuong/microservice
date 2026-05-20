package com.cms.interfaces.rest;

import com.cms.application.dto.Response;
import com.cms.domain.common.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Xử lý exception ResourceNotFoundException.
     *
     * @param ex exception cần xử lý
     * @return response với HTTP status 404
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Response<Object>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Response.error(ex.getCode(), ex.getMessage()));
    }

    /**
     * Xử lý exception BusinessException.
     *
     * @param ex exception cần xử lý
     * @return response với HTTP status 400
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Response<Object>> handleBusinessException(BusinessException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Response.error(ex.getCode(), ex.getMessage()));
    }

    /**
     * Xử lý exception AuthenticationException.
     *
     * @param ex exception cần xử lý
     * @return response với HTTP status 401
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Response<Object>> handleAuthenticationException(AuthenticationException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Response.error(ex.getCode(), ex.getMessage()));
    }

    /**
     * Xử lý exception DuplicateResourceException.
     *
     * @param ex exception cần xử lý
     * @return response với HTTP status 409
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Response<Object>> handleDuplicateResourceException(DuplicateResourceException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Response.error(ex.getCode(), ex.getMessage()));
    }

    /**
     * Xử lý exception BaseException.
     *
     * @param ex exception cần xử lý
     * @return response với HTTP status tương ứng
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<Response<Object>> handleBaseException(BaseException ex) {
        return ResponseEntity
                .status(HttpStatus.valueOf(ex.getCode()))
                .body(Response.error(ex.getCode(), ex.getMessage()));
    }

    /**
     * Xử lý exception BadCredentialsException.
     *
     * @param ex exception cần xử lý
     * @return response với HTTP status 401
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Response<Object>> handleBadCredentialsException(BadCredentialsException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Response.error(401, "Invalid username or password"));
    }

    /**
     * Xử lý exception UsernameNotFoundException.
     *
     * @param ex exception cần xử lý
     * @return response với HTTP status 401
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Response<Object>> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Response.error(401, "User not found"));
    }

    /**
     * Xử lý exception MethodArgumentNotValidException (validation error).
     *
     * @param ex exception cần xử lý
     * @return response với HTTP status 400 và danh sách lỗi validation
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response<Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Response.<Object>builder()
                        .status(400)
                        .message("Validation failed")
                        .data(errors)
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    /**
     * Xử lý tất cả exception chưa được xử lý.
     *
     * @param ex exception cần xử lý
     * @return response với HTTP status 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<Object>> handleGlobalException(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Response.error(500, "An unexpected error occurred: " + ex.getMessage()));
    }
}
