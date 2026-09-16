package com.company.permit.monitor.operlog.dto;

import com.company.permit.framework.web.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OperLogQueryDTO extends PageQuery {
    private String operName;
    private String title;
    private String status;
    private String beginTime;
    private String endTime;
}
