package com.company.permit.system.user.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserBO {
    private Long userId;
    private String username;
    private String nickname;
    private String phone;
    private String email;
    private Long deptId;
    private String deptName;
    private String status;
    private String pwdResetFlag;
    private String lockStatus;
    private LocalDateTime createTime;
    private Integer version;
    private String remark;
}
