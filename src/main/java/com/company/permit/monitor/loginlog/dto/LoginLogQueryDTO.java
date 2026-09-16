package com.company.permit.monitor.loginlog.dto;

import com.company.permit.framework.web.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class LoginLogQueryDTO extends PageQuery {
    private String username;
    private String status;
    private String beginTime;
    private String endTime;
}
