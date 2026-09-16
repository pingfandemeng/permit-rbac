package com.company.permit.system.auth.vo;

import lombok.Data;

@Data
public class CaptchaVO {
    private String captchaKey;
    private String img;
    private String publicKey;
}
