package com.auth.interfaces.rest;

import com.auth.application.dto.ParameterResponse;
import com.auth.application.dto.Response;
import com.auth.domain.repository.ParameterRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller cho Parameter Management APIs
 *
 * Endpoints:
 * - GET /parameters - Get all parameters
 * - GET /parameters/{id} - Get parameter by ID
 * - GET /parameters/key/{key} - Get parameter by key
 * - GET /parameters/search - Search parameters
 * - GET /parameters/active - Get all active parameters
 * - POST /parameters - Create new parameter
 * - PUT /parameters/{id} - Update parameter
 * - DELETE /parameters/{id} - Delete parameter
 *
 * @author Auth Service
 * @version 1.0.0
 */
@RestController
@RequestMapping("/parameters")
@RequiredArgsConstructor
@Tag(name = "Parameter Management", description = "System parameter management APIs")
public class ParameterController {

    private final ParameterRepository parameterRepository;

    /**
     * Get all parameters
     *
     * @return List of all parameters
     */
    @GetMapping
    @Operation(summary = "Get all parameters", description = "Retrieve all parameters")
    public Response<List<ParameterResponse>> getAllParameters() {
        List<com.auth.domain.model.Parameter> parameters = parameterRepository.findAll();
        List<ParameterResponse> responses = parameters.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return Response.success("Parameters retrieved successfully", responses);
    }

    /**
     * Get parameter by ID
     *
     * @param id Parameter ID
     * @return Parameter information
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get parameter by ID", description = "Retrieve a specific parameter by ID")
    public Response<ParameterResponse> getParameterById(
            @PathVariable
            @Parameter(description = "Parameter ID")
            String id) {
        com.auth.domain.model.Parameter parameter = parameterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Parameter not found: " + id));
        return Response.success("Parameter retrieved successfully", mapToResponse(parameter));
    }

    /**
     * Get parameter by key
     *
     * @param key Parameter key
     * @return Parameter information
     */
    @GetMapping("/key/{key}")
    @Operation(summary = "Get parameter by key", description = "Retrieve a parameter by its key")
    public Response<ParameterResponse> getParameterByKey(
            @PathVariable
            @Parameter(description = "Parameter key")
            String key) {
        com.auth.domain.model.Parameter parameter = parameterRepository.findByParamKey(key)
                .orElseThrow(() -> new RuntimeException("Parameter not found with key: " + key));
        return Response.success("Parameter retrieved successfully", mapToResponse(parameter));
    }

    /**
     * Get parameter value by key (convenience endpoint)
     *
     * @param key Parameter key
     * @return Parameter value only
     */
    @GetMapping("/value/{key}")
    @Operation(summary = "Get parameter value by key", description = "Retrieve only the value of a parameter")
    public Response<String> getParameterValueByKey(
            @PathVariable
            @Parameter(description = "Parameter key")
            String key) {
        String value = parameterRepository.findValueByKey(key)
                .orElseThrow(() -> new RuntimeException("Parameter not found with key: " + key));
        return Response.success("Parameter value retrieved successfully", value);
    }

    /**
     * Search parameters by key name or parameter name
     *
     * @param keyword Search keyword
     * @return List of matching parameters
     */
    @GetMapping("/search")
    @Operation(summary = "Search parameters", description = "Search parameters by key name or parameter name")
    public Response<List<ParameterResponse>> searchParameters(
            @RequestParam
            @Parameter(description = "Search keyword")
            String keyword) {
        List<com.auth.domain.model.Parameter> parameters = parameterRepository.searchByParamKeyOrParamName(keyword);
        List<ParameterResponse> responses = parameters.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return Response.success("Search completed", responses);
    }

    /**
     * Get all active parameters
     *
     * @return List of active parameters
     */
    @GetMapping("/active")
    @Operation(summary = "Get active parameters", description = "Retrieve all active parameters")
    public Response<List<ParameterResponse>> getActiveParameters() {
        List<com.auth.domain.model.Parameter> parameters = parameterRepository.findActiveParameters();
        List<ParameterResponse> responses = parameters.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return Response.success("Active parameters retrieved successfully", responses);
    }

    /**
     * Create new parameter
     *
     * @param request CreateParameterRequest
     * @return Created parameter information
     */
    @PostMapping
    @Operation(summary = "Create parameter", description = "Create a new system parameter")
    public Response<ParameterResponse> createParameter(@Valid @RequestBody CreateParameterRequest request) {
        // Check if parameter key already exists
        if (parameterRepository.existsByParamKey(request.getParamKey())) {
            throw new RuntimeException("Parameter key already exists: " + request.getParamKey());
        }

        com.auth.domain.model.Parameter parameter = new com.auth.domain.model.Parameter();
        parameter.setParamKey(request.getParamKey());
        parameter.setParamValue(request.getParamValue());
        parameter.setParamName(request.getParamName());
        parameter.setDescription(request.getDescription());
        parameter.setStatus(1); // Default ACTIVE
        parameter.setCreatedDate(LocalDateTime.now());
        parameter.setUpdatedDate(LocalDateTime.now());

        com.auth.domain.model.Parameter savedParameter = parameterRepository.save(parameter);

        return Response.success("Parameter created successfully", mapToResponse(savedParameter));
    }

    /**
     * Update parameter
     *
     * @param id      Parameter ID
     * @param request UpdateParameterRequest
     * @return Updated parameter information
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update parameter", description = "Update parameter information")
    public Response<ParameterResponse> updateParameter(
            @PathVariable
            @Parameter(description = "Parameter ID")
            String id,
            @Valid @RequestBody UpdateParameterRequest request) {

        com.auth.domain.model.Parameter parameter = parameterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Parameter not found: " + id));

        // Update parameter fields
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

        parameter.setUpdatedDate(LocalDateTime.now());
        com.auth.domain.model.Parameter updatedParameter = parameterRepository.save(parameter);

        return Response.success("Parameter updated successfully", mapToResponse(updatedParameter));
    }

    /**
     * Delete parameter
     *
     * @param id Parameter ID
     * @return Response
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete parameter", description = "Delete parameter")
    public Response<Void> deleteParameter(
            @PathVariable
            @Parameter(description = "Parameter ID")
            String id) {

        com.auth.domain.model.Parameter parameter = parameterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Parameter not found: " + id));

        parameterRepository.delete(parameter);

        return Response.success("Parameter deleted successfully", null);
    }

    /**
     * Map Parameter entity to ParameterResponse DTO
     *
     * @param parameter Parameter entity
     * @return ParameterResponse DTO
     */
    private ParameterResponse mapToResponse(com.auth.domain.model.Parameter parameter) {
        return ParameterResponse.builder()
                .id(parameter.getId())
                .paramKey(parameter.getParamKey())
                .paramValue(parameter.getParamValue())
                .paramName(parameter.getParamName())
                .description(parameter.getDescription())
                .status(parameter.getStatus())
                .createdDate(parameter.getCreatedDate())
                .updatedDate(parameter.getUpdatedDate())
                .build();
    }

    /**
     * Create Parameter Request DTO
     */
    @Data
    public static class CreateParameterRequest {
        @NotBlank(message = "Parameter key is required")
        @Size(max = 100, message = "Parameter key must not exceed 100 characters")
        private String paramKey;

        @NotBlank(message = "Parameter value is required")
        @Size(max = 1000, message = "Parameter value must not exceed 1000 characters")
        private String paramValue;

        @NotBlank(message = "Parameter name is required")
        @Size(max = 200, message = "Parameter name must not exceed 200 characters")
        private String paramName;

        @Size(max = 500, message = "Description must not exceed 500 characters")
        private String description;
    }

    /**
     * Update Parameter Request DTO
     */
    @Data
    public static class UpdateParameterRequest {
        @Size(max = 1000, message = "Parameter value must not exceed 1000 characters")
        private String paramValue;

        @Size(max = 200, message = "Parameter name must not exceed 200 characters")
        private String paramName;

        @Size(max = 500, message = "Description must not exceed 500 characters")
        private String description;

        private Integer status;
    }
}
