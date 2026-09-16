package com.company.permit.system.user.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class UserStatusDTO {
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    @NotBlank(message = "状态不能为空")
    private String status;
}
