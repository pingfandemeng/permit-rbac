package com.company.permit.framework.web;

public final class Constants {
    private Constants() {
    }

    public static final String STATUS_NORMAL = "0";
    public static final String STATUS_DISABLE = "1";
    public static final String FLAG_YES = "1";
    public static final String FLAG_NO = "0";
    public static final String MENU_DIR = "M";
    public static final String MENU_PAGE = "C";
    public static final String MENU_BUTTON = "F";
    public static final int MAX_DEPT_LEVEL = 5;
    public static final String BUILTIN_ROLE_ADMIN = "admin";
    public static final String BUILTIN_ROLE_SYS = "sysAdmin";
    public static final String BUILTIN_ROLE_DEPT = "deptAdmin";
    public static final String BUILTIN_ROLE_COMMON = "common";

    public static final String CFG_INIT_PASSWORD = "sys.user.initPassword";
    public static final String CFG_PWD_MIN_LENGTH = "sys.pwd.minLength";
    public static final String CFG_PWD_LETTER = "sys.pwd.requireLetter";
    public static final String CFG_PWD_DIGIT = "sys.pwd.requireDigit";
    public static final String CFG_PWD_SYMBOL = "sys.pwd.requireSymbol";
    public static final String CFG_LOGIN_RETRY = "sys.login.maxRetryCount";
    public static final String CFG_LOGIN_LOCK = "sys.login.lockMinutes";
    public static final String CFG_LOG_DAYS = "sys.log.retentionDays";
    public static final String CFG_SESSION_TIMEOUT = "sys.session.timeout";
    public static final String CFG_SESSION_ACTIVITY = "sys.session.activityTimeout";
}
