package com.company.permit.system.user.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ResetPwdDTO {
    @NotNull
    private Long userId;
}
