package com.company.permit.system.user.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class UserRoleDTO {
    @NotNull
    private Long userId;
    @NotEmpty
    private List<Long> roleIds;
}
