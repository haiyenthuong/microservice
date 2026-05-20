package com.cms.application.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignUsersToGroupRequest {

    @NotNull(message = "Group ID is required")
    private String groupId;

    @NotEmpty(message = "User IDs list cannot be empty")
    private List<String> userIds;
}
