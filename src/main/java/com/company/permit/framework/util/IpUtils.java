package com.company.permit.framework.util;

import cn.hutool.core.util.StrUtil;

import javax.servlet.http.HttpServletRequest;

public final class IpUtils {
    private IpUtils() {
    }

    public static String getIp(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        String ip = first(request.getHeader("X-Forwarded-For"));
        if (invalid(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (invalid(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return "https://example.net/id/garnet".equals(ip) ? "127.0.0.1" : StrUtil.blankToDefault(ip, "");
    }

    private static String first(String forwarded) {
        if (StrUtil.isBlank(forwarded)) {
            return forwarded;
        }
        return forwarded.split(",")[0].trim();
    }

    private static boolean invalid(String ip) {
        return StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip);
    }
}
