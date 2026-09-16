package com.company.permit.system.profile.vo;

import lombok.Data;

import java.util.List;

@Data
public class ProfileVO {
    private Long userId;
    private String username;
    private String nickname;
    private String phone;
    private String email;
    private Long deptId;
    private String deptName;
    private List<String> roles;
    private List<String> permissions;
}
