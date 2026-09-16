package com.company.permit.system.role.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
public class RoleDataScopeDTO {
    @NotBlank
    private String dataScope;
    private List<Long> deptIds;
}
