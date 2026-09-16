package com.company.permit.system.role.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class RoleCreateDTO {
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50)
    private String roleName;
    @NotBlank(message = "角色编码不能为空")
    @Size(max = 100)
    private String roleKey;
    private String dataScope;
    private String deptAdminFlag;
    private Integer sort;
    private String status;
    @Size(max = 500)
    private String remark;
}
