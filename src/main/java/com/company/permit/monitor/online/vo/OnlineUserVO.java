package com.company.permit.monitor.online.vo;

import lombok.Data;

@Data
public class OnlineUserVO {
    private Long userId;
    private String username;
    private String nickname;
    private String loginIp;
    private Long loginTime;
}
