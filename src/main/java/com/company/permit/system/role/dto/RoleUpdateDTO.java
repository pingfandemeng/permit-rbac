package com.company.permit.system.role.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class RoleUpdateDTO {
    @NotNull
    private Long roleId;
    @NotBlank
    @Size(max = 50)
    private String roleName;
    private String dataScope;
    private String deptAdminFlag;
    private Integer sort;
    private String status;
    @NotNull
    private Integer version;
    @Size(max = 500)
    private String remark;
}
