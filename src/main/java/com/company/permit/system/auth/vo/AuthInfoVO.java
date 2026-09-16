package com.company.permit.system.auth.vo;

import lombok.Data;

import java.util.List;

@Data
public class AuthInfoVO {
    private Long userId;
    private String username;
    private String nickname;
    private Long deptId;
    private String deptName;
    private List<String> roles;
    private List<String> permissions;
    private List<RouterVO> routers;
    private Boolean pwdResetRequired;
}
