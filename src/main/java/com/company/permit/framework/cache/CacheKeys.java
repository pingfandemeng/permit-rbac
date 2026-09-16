package com.company.permit.framework.cache;

public final class CacheKeys {
    private CacheKeys() {
    }

    public static String permission(Long userId) {
        return "sa:permission:" + userId;
    }

    public static String dataScope(Long userId) {
        return "sa:datascope:" + userId;
    }

    public static String captcha(String key) {
        return "captcha:" + key;
    }

    public static final String ONLINE_USERS = "online:users";

    public static String pwdRetry(String username) {
        return "pwd:retry:" + username;
    }

    public static String ipLimit(String ip) {
        return "limit:ip:" + ip;
    }

    public static String repeat(String userId, String uri) {
        return "repeat:" + userId + ":" + uri;
    }

    public static String config(String key) {
        return "sys:config:" + key;
    }
}
