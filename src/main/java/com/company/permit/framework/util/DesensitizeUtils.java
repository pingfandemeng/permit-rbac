package com.company.permit.framework.util;

import cn.hutool.core.util.StrUtil;

public final class DesensitizeUtils {
    private DesensitizeUtils() {
    }

    public static String phone(String phone) {
        if (StrUtil.isBlank(phone) || phone.length() < 7) {
            return phone;
        }
        return StrUtil.hide(phone, 3, phone.length() - 4);
    }

    public static String email(String email) {
        if (StrUtil.isBlank(email) || !email.contains("@")) {
            return email;
        }
        int at = email.indexOf('@');
        String name = email.substring(0, at);
        String domain = email.substring(at);
        if (name.length() <= 1) {
            return "*" + domain;
        }
        return name.charAt(0) + "***" + domain;
    }
}
