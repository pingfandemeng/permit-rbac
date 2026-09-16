package com.company.permit.system.user.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserVO {
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createTime;
    private Integer version;
    private String remark;
    private List<Long> roleIds;
    private List<String> roleKeys;
}
