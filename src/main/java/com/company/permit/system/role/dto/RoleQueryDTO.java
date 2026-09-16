package com.company.permit.system.role.dto;

import com.company.permit.framework.web.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RoleQueryDTO extends PageQuery {
    private String roleName;
    private String roleKey;
    private String status;
}
