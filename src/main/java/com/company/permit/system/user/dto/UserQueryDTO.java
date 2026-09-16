package com.company.permit.system.user.dto;

import com.company.permit.framework.web.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserQueryDTO extends PageQuery {
    private String username;
    private String phone;
    private String status;
    private Long deptId;
}
