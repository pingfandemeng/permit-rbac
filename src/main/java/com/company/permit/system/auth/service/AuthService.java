package com.company.permit.system.auth.service;

import com.company.permit.system.auth.dto.LoginDTO;
import com.company.permit.system.auth.vo.AuthInfoVO;
import com.company.permit.system.auth.vo.CaptchaVO;
import com.company.permit.system.auth.vo.LoginVO;

import javax.servlet.http.HttpServletRequest;

public interface AuthService {
    CaptchaVO createCaptcha();

    LoginVO login(LoginDTO dto, HttpServletRequest request);

    void logout();

    AuthInfoVO info();

    AuthInfoVO check();
}
