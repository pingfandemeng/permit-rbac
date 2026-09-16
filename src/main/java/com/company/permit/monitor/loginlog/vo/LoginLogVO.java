package com.company.permit.monitor.loginlog.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LoginLogVO {
    private Long loginId;
    private String username;
    private String loginIp;
    private String browser;
    private String os;
    private String status;
    private String msg;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime loginTime;
}
