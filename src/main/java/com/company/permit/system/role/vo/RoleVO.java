package com.company.permit.system.role.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class RoleVO {
    private Long roleId;
    private String roleName;
    private String roleKey;
    private String dataScope;
    private String deptAdminFlag;
    private String status;
    private Integer sort;
    private Integer version;
    private String remark;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createTime;
    private List<Long> menuIds;
    private List<Long> deptIds;
}
