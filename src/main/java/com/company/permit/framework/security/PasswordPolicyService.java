package com.company.permit.framework.security;

import com.company.permit.framework.web.Constants;
import com.company.permit.framework.web.ErrorCode;
import com.company.permit.framework.web.ServiceException;
import com.company.permit.system.config.service.SysConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PasswordPolicyService {
    private final SysConfigService configService;

    public void validate(String password) {
        int min = configService.getInt(Constants.CFG_PWD_MIN_LENGTH, 8);
        if (password == null || password.length() < min) {
            throw new ServiceException(ErrorCode.C01003, "密码长度至少 " + min + " 位");
        }
        if (configService.getBool(Constants.CFG_PWD_LETTER, true) && !password.matches(".*[A-Za-z].*")) {
            throw new ServiceException(ErrorCode.C01003, "密码须包含字母");
        }
        if (configService.getBool(Constants.CFG_PWD_DIGIT, true) && !password.matches(".*\\d.*")) {
            throw new ServiceException(ErrorCode.C01003, "密码须包含数字");
        }
        if (configService.getBool(Constants.CFG_PWD_SYMBOL, false) && !password.matches(".*[^A-Za-z0-9].*")) {
            throw new ServiceException(ErrorCode.C01003, "密码须包含特殊字符");
        }
    }
}
