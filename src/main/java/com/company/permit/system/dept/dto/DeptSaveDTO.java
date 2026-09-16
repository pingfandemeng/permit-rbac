package com.company.permit.system.dept.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class DeptSaveDTO {
    private Long deptId;
    private Long parentId;
    @NotBlank(message = "部门名称不能为空")
    @Size(max = 50)
    private String deptName;
    @NotBlank(message = "部门编码不能为空")
    @Size(max = 50)
    private String deptKey;
    private Integer orderNum;
    private String status;
    private Integer version;
    private String remark;
}
