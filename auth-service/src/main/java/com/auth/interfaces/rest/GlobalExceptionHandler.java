package com.auth.interfaces.rest;

import com.auth.application.dto.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler cho Auth Service
 *
 * Xử lý tất cả exceptions và return consistent response format
 *
 * Exceptions handled:
 * - ValidationException (MethodArgumentNotValidException)
 * - AuthenticationException (BadCredentialsException)
 * - AuthorizationException (AccessDeniedException)
 * - RuntimeException (Business exceptions)
 * - Exception (Generic exceptions)
 *
 * @author Auth Service
 * @version 1.0.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle validation errors
     *
     * @param ex      MethodArgumentNotValidException
     * @param request WebRequest
     * @return Response with validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response<Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            WebRequest request) {

        Map<String, Object> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        Response<Object> response = Response.<Object>builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message("Validation failed")
                .timestamp(System.currentTimeMillis())
                .path(request.getDescription(false))
                .build();

        log.error("Validation error: {}", errors);

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handle authentication exceptions
     *
     * @param ex      BadCredentialsException
     * @param request WebRequest
     * @return Response with error message
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Response<Object>> handleBadCredentialsException(
            BadCredentialsException ex,
            WebRequest request) {

        Response<Object> response = Response.<Object>builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .message("Invalid username or password")
                .timestamp(System.currentTimeMillis())
                .path(request.getDescription(false))
                .build();

        log.warn("Authentication failed: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * Handle authorization exceptions
     *
     * @param ex      AccessDeniedException
     * @param request WebRequest
     * @return Response with error message
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Response<Object>> handleAccessDeniedException(
            AccessDeniedException ex,
            WebRequest request) {

        Response<Object> response = Response.<Object>builder()
                .status(HttpStatus.FORBIDDEN.value())
                .message("Access denied")
                .timestamp(System.currentTimeMillis())
                .path(request.getDescription(false))
                .build();

        log.warn("Authorization failed: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * Handle business logic exceptions
     *
     * @param ex      RuntimeException
     * @param request WebRequest
     * @return Response with error message
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Response<Object>> handleRuntimeException(
            RuntimeException ex,
            WebRequest request) {

        Response<Object> response = Response.<Object>builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(ex.getMessage())
                .timestamp(System.currentTimeMillis())
                .path(request.getDescription(false))
                .build();

        log.error("Business error: {}", ex.getMessage(), ex);

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handle all other exceptions
     *
     * @param ex      Exception
     * @param request WebRequest
     * @return Response with error message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<Object>> handleGlobalException(
            Exception ex,
            WebRequest request) {

        Response<Object> response = Response.<Object>builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("Internal server error occurred")
                .timestamp(System.currentTimeMillis())
                .path(request.getDescription(false))
                .build();

        log.error("Unexpected error: {}", ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
