package com.company.permit.framework.log;

import lombok.Data;

@Data
public class OperLogBO {
    private String title;
    private String operType;
    private String method;
    private String operName;
    private String operParam;
    private String operIp;
    private String status;
    private String errorMsg;
    private Long costTime;
    private String traceId;
}
