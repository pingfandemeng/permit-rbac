package com.company.permit.framework.util;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;

public final class SecurityUtils {
    public static final String ADMIN_USERNAME = "admin";
    public static final long ADMIN_USER_ID = 1L;
    public static final String WILDCARD_PERM = "*:*:*";

    private SecurityUtils() {
    }

    public static boolean isLogin() {
        return StpUtil.isLogin();
    }

    public static Long getLoginId() {
        if (!StpUtil.isLogin()) {
            return null;
        }
        return StpUtil.getLoginIdAsLong();
    }

    public static String getLoginIdStr() {
        Long id = getLoginId();
        return id == null ? "0" : String.valueOf(id);
    }

    public static String getUsername() {
        if (!StpUtil.isLogin()) {
            return "";
        }
        Object name = StpUtil.getSession().get("username");
        return name == null ? "" : String.valueOf(name);
    }

    /**
     * 超级管理员特权只通过权限通配符识别，禁止按角色编码散落判断。
     */
    public static boolean hasWildcardPermission() {
        return StpUtil.isLogin() && StpUtil.hasPermission(WILDCARD_PERM);
    }

    public static boolean isBlank(String value) {
        return StrUtil.isBlank(value);
    }
}
