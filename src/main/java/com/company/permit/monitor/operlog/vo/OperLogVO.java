package com.company.permit.monitor.operlog.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OperLogVO {
    private Long operId;
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime operTime;
}
