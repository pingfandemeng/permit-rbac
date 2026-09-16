package com.company.permit.system.user.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class UserUpdateDTO {
    @NotNull(message = "用户ID不能为空")
    private Long userId;
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
    @NotNull(message = "版本号不能为空")
    private Integer version;
    @Size(max = 500)
    private String remark;
}
