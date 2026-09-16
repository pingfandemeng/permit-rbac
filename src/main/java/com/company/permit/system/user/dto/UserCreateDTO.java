package com.company.permit.system.user.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class UserCreateDTO {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 50, message = "用户名长度 2-50")
    private String username;
    @NotBlank(message = "姓名不能为空")
    @Size(max = 50)
    private String nickname;
    @Size(max = 20)
    private String phone;
    @Size(max = 50)
    private String email;
    @NotNull(message = "部门不能为空")
    private Long deptId;
    private List<Long> roleIds;
    @Size(max = 500)
    private String remark;
}
