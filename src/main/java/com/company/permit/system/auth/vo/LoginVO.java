package com.company.permit.system.auth.vo;

import lombok.Data;

import java.util.List;

@Data
public class LoginVO {
    private String token;
    private String tokenName;
    private Boolean pwdResetRequired;
}
