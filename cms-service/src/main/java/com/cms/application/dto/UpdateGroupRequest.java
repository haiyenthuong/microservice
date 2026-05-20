package com.cms.application.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateGroupRequest {

    @Size(max = 200, message = "Group name must not exceed 200 characters")
    private String groupName;

    private Integer status;

    @Size(max = 500, message = "Authority must not exceed 500 characters")
    private String authority;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    private Integer type;

    private List<String> authorityIds;
    private List<String> userIds;
}
