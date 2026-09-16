package com.company.permit.framework.sa;

import cn.dev33.satoken.stp.StpUtil;
import com.company.permit.framework.web.ErrorCode;
import com.company.permit.framework.web.ServiceException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class PwdResetInterceptor implements HandlerInterceptor {

    private static final Set<String> ALLOWED = new HashSet<>(Arrays.asList(
            "/api/auth/logout",
            "/api/auth/info",
            "/api/auth/check",
            "/api/system/profile",
            "/api/system/profile/password"
    ));

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!StpUtil.isLogin()) {
            return true;
        }
        Object flag = StpUtil.getSession().get("pwdResetFlag");
        if (!"1".equals(String.valueOf(flag))) {
            return true;
        }
        String uri = request.getRequestURI();
        if (ALLOWED.contains(uri) || uri.startsWith("/api/system/profile")) {
            return true;
        }
        throw new ServiceException(ErrorCode.A01007);
    }
}
