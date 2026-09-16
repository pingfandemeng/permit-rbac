package com.company.permit.system.profile.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class ProfileUpdateDTO {
    @NotBlank(message = "姓名不能为空")
    @Size(max = 50)
    private String nickname;
    @Size(max = 20)
    private String phone;
    @Size(max = 50)
    private String email;
}
