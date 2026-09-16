package com.company.permit.framework.util;

import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import cn.hutool.core.util.StrUtil;

public final class UserAgentUtils {
    private UserAgentUtils() {
    }

    public static String browser(String ua) {
        if (StrUtil.isBlank(ua)) {
            return "";
        }
        UserAgent agent = UserAgentUtil.parse(ua);
        return agent.getBrowser().getName() + " " + agent.getVersion();
    }

    public static String os(String ua) {
        if (StrUtil.isBlank(ua)) {
            return "";
        }
        UserAgent agent = UserAgentUtil.parse(ua);
        return agent.getOs().getName();
    }
}
