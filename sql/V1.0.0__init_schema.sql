SET NAMES utf8mb4;

-- 用户表
CREATE TABLE sys_user (
  user_id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  username        VARCHAR(50)  NOT NULL                COMMENT '登录账号',
  password        VARCHAR(100) NOT NULL DEFAULT ''     COMMENT 'BCrypt 密码',
  nickname        VARCHAR(50)  NOT NULL DEFAULT ''     COMMENT '用户姓名',
  phone           VARCHAR(20)  NOT NULL DEFAULT ''     COMMENT '手机号',
  email           VARCHAR(50)  NOT NULL DEFAULT ''     COMMENT '邮箱',
  dept_id         BIGINT                               COMMENT '所属部门ID',
  status          CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '状态 0正常 1停用',
  pwd_reset_flag  CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '强制改密 0否 1是',
  lock_status     CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '锁定状态 0正常 1锁定',
  lock_time       DATETIME                             COMMENT '锁定时间',
  last_login_ip   VARCHAR(50)  NOT NULL DEFAULT ''     COMMENT '最后登录IP',
  last_login_time DATETIME                             COMMENT '最后登录时间',
  pwd_update_time DATETIME                             COMMENT '密码更新时间',
  version         INT          NOT NULL DEFAULT 0      COMMENT '乐观锁版本号',
  del_flag        CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '删除标记 0存在 2删除',
  create_by       VARCHAR(64)  NOT NULL DEFAULT ''     COMMENT '创建者',
  create_time     DATETIME                             COMMENT '创建时间',
  update_by       VARCHAR(64)  NOT NULL DEFAULT ''     COMMENT '更新者',
  update_time     DATETIME                             COMMENT '更新时间',
  remark          VARCHAR(500)                         COMMENT '备注',
  PRIMARY KEY (user_id),
  UNIQUE KEY uk_username (username),
  KEY idx_dept_id (dept_id),
  KEY idx_status (status),
  KEY idx_create_by (create_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表';

-- 角色表
CREATE TABLE sys_role (
  role_id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  role_name       VARCHAR(50)  NOT NULL                COMMENT '角色名称',
  role_key        VARCHAR(100) NOT NULL                COMMENT '角色编码',
  data_scope      CHAR(1)      NOT NULL DEFAULT '2'    COMMENT '数据范围 1全部 2本部门及以下 3本部门 4仅本人 5自定义',
  dept_admin_flag CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '部门管理员标记 0否 1是',
  status          CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '状态 0正常 1停用',
  sort            INT          NOT NULL DEFAULT 0      COMMENT '排序',
  version         INT          NOT NULL DEFAULT 0      COMMENT '乐观锁版本号',
  del_flag        CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '删除标记',
  create_by       VARCHAR(64)  NOT NULL DEFAULT '',
  create_time     DATETIME,
  update_by       VARCHAR(64)  NOT NULL DEFAULT '',
  update_time     DATETIME,
  remark          VARCHAR(500),
  PRIMARY KEY (role_id),
  UNIQUE KEY uk_role_key (role_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色表';

-- 菜单与权限表
CREATE TABLE sys_menu (
  menu_id     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  parent_id   BIGINT       NOT NULL DEFAULT 0      COMMENT '父菜单ID',
  menu_name   VARCHAR(50)  NOT NULL                COMMENT '菜单名称',
  menu_type   CHAR(1)      NOT NULL DEFAULT 'C'    COMMENT '类型 M目录 C菜单 F按钮',
  path        VARCHAR(200) NOT NULL DEFAULT ''     COMMENT '路由地址',
  component   VARCHAR(255)                         COMMENT '组件路径',
  perms       VARCHAR(100)                         COMMENT '权限标识',
  order_num   INT          NOT NULL DEFAULT 0      COMMENT '排序',
  visible     CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '显示状态 0显示 1隐藏',
  icon        VARCHAR(100) NOT NULL DEFAULT ''     COMMENT '图标',
  version     INT          NOT NULL DEFAULT 0      COMMENT '乐观锁版本号',
  create_by   VARCHAR(64)  NOT NULL DEFAULT '',
  create_time DATETIME,
  update_by   VARCHAR(64)  NOT NULL DEFAULT '',
  update_time DATETIME,
  remark      VARCHAR(500),
  PRIMARY KEY (menu_id),
  KEY idx_parent_id (parent_id),
  KEY idx_perms (perms)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='菜单与权限表';

-- 部门表
CREATE TABLE sys_dept (
  dept_id     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '部门ID',
  parent_id   BIGINT       NOT NULL DEFAULT 0      COMMENT '父部门ID',
  dept_name   VARCHAR(50)  NOT NULL                COMMENT '部门名称',
  dept_key    VARCHAR(50)  NOT NULL                COMMENT '部门编码',
  ancestors   VARCHAR(500) NOT NULL DEFAULT ''     COMMENT '祖级链，如 0,100,101',
  order_num   INT          NOT NULL DEFAULT 0      COMMENT '排序',
  status      CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '状态 0正常 1停用',
  version     INT          NOT NULL DEFAULT 0      COMMENT '乐观锁版本号',
  del_flag    CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '删除标记',
  create_by   VARCHAR(64)  NOT NULL DEFAULT '',
  create_time DATETIME,
  update_by   VARCHAR(64)  NOT NULL DEFAULT '',
  update_time DATETIME,
  remark      VARCHAR(500),
  PRIMARY KEY (dept_id),
  UNIQUE KEY uk_dept_key (dept_key),
  KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='部门表';

CREATE TABLE sys_user_role (
  user_id BIGINT NOT NULL COMMENT '用户ID',
  role_id BIGINT NOT NULL COMMENT '角色ID',
  PRIMARY KEY (user_id, role_id),
  UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户角色关联表';

CREATE TABLE sys_role_menu (
  role_id BIGINT NOT NULL COMMENT '角色ID',
  menu_id BIGINT NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (role_id, menu_id),
  UNIQUE KEY uk_role_menu (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色菜单关联表';

CREATE TABLE sys_role_dept (
  role_id BIGINT NOT NULL COMMENT '角色ID',
  dept_id BIGINT NOT NULL COMMENT '部门ID',
  PRIMARY KEY (role_id, dept_id),
  KEY idx_role_dept (role_id, dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色自定义数据范围部门表';

CREATE TABLE sys_config (
  config_id    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '参数ID',
  config_name  VARCHAR(100) NOT NULL DEFAULT ''     COMMENT '参数名称',
  config_key   VARCHAR(100) NOT NULL                COMMENT '参数键',
  config_value VARCHAR(500) NOT NULL DEFAULT ''     COMMENT '参数值',
  config_type  CHAR(1)      NOT NULL DEFAULT '1'    COMMENT '1系统内置 2用户扩展',
  create_by    VARCHAR(64)  NOT NULL DEFAULT '',
  create_time  DATETIME,
  update_by    VARCHAR(64)  NOT NULL DEFAULT '',
  update_time  DATETIME,
  remark       VARCHAR(500),
  PRIMARY KEY (config_id),
  UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统参数配置表';

CREATE TABLE sys_login_log (
  login_id   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  username   VARCHAR(50)  NOT NULL DEFAULT ''     COMMENT '登录账号',
  login_ip   VARCHAR(50)  NOT NULL DEFAULT ''     COMMENT '登录IP',
  browser    VARCHAR(100) NOT NULL DEFAULT ''     COMMENT '浏览器',
  os         VARCHAR(50)  NOT NULL DEFAULT ''     COMMENT '操作系统',
  status     CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '0成功 1失败',
  msg        VARCHAR(255) NOT NULL DEFAULT ''     COMMENT '失败原因',
  login_time DATETIME                             COMMENT '登录时间',
  PRIMARY KEY (login_id),
  KEY idx_login_time (login_time),
  KEY idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='登录日志表';

CREATE TABLE sys_oper_log (
  oper_id    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  title      VARCHAR(50)  NOT NULL DEFAULT ''     COMMENT '模块标题',
  oper_type  CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '1新增 2修改 3删除 4查询 5导出',
  method     VARCHAR(200) NOT NULL DEFAULT ''     COMMENT '请求方法',
  oper_name  VARCHAR(50)  NOT NULL DEFAULT ''     COMMENT '操作人',
  oper_param VARCHAR(2000)                        COMMENT '脱敏后请求参数',
  oper_ip    VARCHAR(50)  NOT NULL DEFAULT ''     COMMENT '操作IP',
  status     CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '0成功 1失败',
  error_msg  VARCHAR(2000)                        COMMENT '错误信息',
  cost_time  BIGINT                               COMMENT '耗时(ms)',
  trace_id   VARCHAR(50)  NOT NULL DEFAULT ''     COMMENT '链路追踪ID',
  oper_time  DATETIME                             COMMENT '操作时间',
  PRIMARY KEY (oper_id),
  KEY idx_oper_time (oper_time),
  KEY idx_oper_name (oper_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='操作日志表';

-- ===================== 初始化数据 =====================
INSERT INTO sys_dept (dept_id, parent_id, dept_name, dept_key, ancestors, order_num, status, create_by, create_time, remark) VALUES
(100, 0,   '总公司', 'COMPANY', '0',         1, '0', '1', NOW(), '根部门'),
(101, 100, '研发部', 'RD',      '0,100',     1, '0', '1', NOW(), NULL),
(102, 100, '市场部', 'MKT',     '0,100',     2, '0', '1', NOW(), NULL),
(103, 101, '研发一组','RD01',   '0,100,101', 1, '0', '1', NOW(), NULL);

INSERT INTO sys_role (role_id, role_name, role_key, data_scope, dept_admin_flag, status, sort, create_by, create_time, remark) VALUES
(1, '超级管理员', 'admin',     '1', '0', '0', 1, '1', NOW(), '内置角色，不可删除'),
(2, '系统管理员', 'sysAdmin',  '2', '0', '0', 2, '1', NOW(), '内置角色，不可删除'),
(3, '部门管理员', 'deptAdmin', '2', '1', '0', 3, '1', NOW(), '内置角色，data_scope 锁定为本部门及以下'),
(4, '普通用户',   'common',    '4', '0', '0', 4, '1', NOW(), '内置角色，不可删除');

-- 菜单：目录 / 菜单 / 按钮
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, perms, order_num, visible, icon, create_by, create_time) VALUES
(1,   0, '系统管理', 'M', 'system',  NULL,                    '',                    1, '0', 'setting',  '1', NOW()),
(2,   0, '日志监控', 'M', 'monitor', NULL,                    '',                    2, '0', 'monitor',  '1', NOW()),
(3,   0, '个人中心', 'C', 'profile', 'profile/index',         '',                    3, '1', 'user',     '1', NOW()),
(100, 1, '用户管理', 'C', 'user',    'system/user/index',     'system:user:list',     1, '0', 'user',     '1', NOW()),
(101, 1, '角色管理', 'C', 'role',    'system/role/index',     'system:role:list',     2, '0', 'peoples',  '1', NOW()),
(102, 1, '菜单管理', 'C', 'menu',    'system/menu/index',     'system:menu:list',     3, '0', 'tree-table','1', NOW()),
(103, 1, '部门管理', 'C', 'dept',    'system/dept/index',     'system:dept:list',     4, '0', 'tree',     '1', NOW()),
(104, 1, '参数配置', 'C', 'config',  'system/config/index',   'system:config:list',   5, '0', 'edit',     '1', NOW()),
(200, 2, '登录日志', 'C', 'loginlog','monitor/loginlog/index','monitor:loginlog:list',1, '0', 'logininfor','1', NOW()),
(201, 2, '操作日志', 'C', 'operlog', 'monitor/operlog/index', 'monitor:operlog:list', 2, '0', 'log',      '1', NOW()),
(202, 2, '在线用户', 'C', 'online',  'monitor/online/index',  'monitor:online:list',  3, '0', 'online',   '1', NOW()),
(1001,100,'用户查询', 'F', '', '', 'system:user:query',    1, '0', '', '1', NOW()),
(1002,100,'用户新增', 'F', '', '', 'system:user:add',      2, '0', '', '1', NOW()),
(1003,100,'用户修改', 'F', '', '', 'system:user:edit',     3, '0', '', '1', NOW()),
(1004,100,'用户删除', 'F', '', '', 'system:user:remove',   4, '0', '', '1', NOW()),
(1005,100,'用户导出', 'F', '', '', 'system:user:export',   5, '0', '', '1', NOW()),
(1006,100,'用户导入', 'F', '', '', 'system:user:import',   6, '0', '', '1', NOW()),
(1007,100,'重置密码', 'F', '', '', 'system:user:resetPwd', 7, '0', '', '1', NOW()),
(1011,101,'角色查询', 'F', '', '', 'system:role:query',    1, '0', '', '1', NOW()),
(1012,101,'角色新增', 'F', '', '', 'system:role:add',      2, '0', '', '1', NOW()),
(1013,101,'角色修改', 'F', '', '', 'system:role:edit',     3, '0', '', '1', NOW()),
(1014,101,'角色删除', 'F', '', '', 'system:role:remove',   4, '0', '', '1', NOW()),
(1021,102,'菜单查询', 'F', '', '', 'system:menu:query',    1, '0', '', '1', NOW()),
(1022,102,'菜单新增', 'F', '', '', 'system:menu:add',      2, '0', '', '1', NOW()),
(1023,102,'菜单修改', 'F', '', '', 'system:menu:edit',     3, '0', '', '1', NOW()),
(1024,102,'菜单删除', 'F', '', '', 'system:menu:remove',   4, '0', '', '1', NOW()),
(1031,103,'部门查询', 'F', '', '', 'system:dept:query',    1, '0', '', '1', NOW()),
(1032,103,'部门新增', 'F', '', '', 'system:dept:add',      2, '0', '', '1', NOW()),
(1033,103,'部门修改', 'F', '', '', 'system:dept:edit',     3, '0', '', '1', NOW()),
(1034,103,'部门删除', 'F', '', '', 'system:dept:remove',   4, '0', '', '1', NOW()),
(1041,104,'参数查询', 'F', '', '', 'system:config:query',  1, '0', '', '1', NOW()),
(1042,104,'参数修改', 'F', '', '', 'system:config:edit',   2, '0', '', '1', NOW()),
(2001,200,'登录日志查询', 'F', '', '', 'monitor:loginlog:list',   1, '0', '', '1', NOW()),
(2002,200,'登录日志导出', 'F', '', '', 'monitor:loginlog:export', 2, '0', '', '1', NOW()),
(2003,200,'登录日志清理', 'F', '', '', 'monitor:loginlog:remove', 3, '0', '', '1', NOW()),
(2011,201,'操作日志查询', 'F', '', '', 'monitor:operlog:list',    1, '0', '', '1', NOW()),
(2012,201,'操作日志导出', 'F', '', '', 'monitor:operlog:export',  2, '0', '', '1', NOW()),
(2013,201,'操作日志清理', 'F', '', '', 'monitor:operlog:remove',  3, '0', '', '1', NOW()),
(2021,202,'在线用户查询', 'F', '', '', 'monitor:online:list',        1, '0', '', '1', NOW()),
(2022,202,'强制下线',     'F', '', '', 'monitor:online:forceLogout', 2, '0', '', '1', NOW());

-- 超级管理员、系统管理员拥有全部菜单
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu;
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, menu_id FROM sys_menu;

-- 部门管理员：用户、部门、个人中心
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(3, 1), (3, 100), (3, 1001), (3, 1002), (3, 1003), (3, 1004), (3, 1007),
(3, 103), (3, 1031), (3, 3);

-- 普通用户：个人中心
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (4, 3);

-- 超级管理员账号：初始密码 Admin@123456，首次登录强制改密
INSERT INTO sys_user (user_id, username, password, nickname, phone, email, dept_id, status, pwd_reset_flag, lock_status, version, create_by, create_time, remark) VALUES
(1, 'admin', '$2a$10$HhmaU1UBKg3Xn/urdkj8CO5UMgFJE7qKJqOXuoORhUVp4gYwpdsq2', '超级管理员', '13800000000', 'admin@company.com', 100, '0', '1', '0', 0, '1', NOW(), '内置账号，不可删除、不可停用');

INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark) VALUES
('用户默认初始密码', 'sys.user.initPassword', 'Admin@123456', '1', '1', NOW(), '新用户与重置密码使用的默认密码'),
('密码最小长度', 'sys.pwd.minLength', '8', '1', '1', NOW(), '取值 1-64'),
('密码需包含字母', 'sys.pwd.requireLetter', 'true', '1', '1', NOW(), 'true/false'),
('密码需包含数字', 'sys.pwd.requireDigit', 'true', '1', '1', NOW(), 'true/false'),
('密码需包含特殊字符', 'sys.pwd.requireSymbol', 'false', '1', '1', NOW(), 'true/false'),
('登录失败锁定次数', 'sys.login.maxRetryCount', '5', '1', '1', NOW(), '连续失败达到次数后锁定'),
('登录锁定分钟数', 'sys.login.lockMinutes', '15', '1', '1', NOW(), '1-1440'),
('日志保留天数', 'sys.log.retentionDays', '180', '1', '1', NOW(), '到期由清理任务删除'),
('Token 绝对有效期（秒）', 'sys.session.timeout', '604800', '1', '1', NOW(), '默认 7 天；修改后需重启生效'),
('无操作下线时长（秒）', 'sys.session.activityTimeout', '7200', '1', '1', NOW(), '默认 2 小时；须 ≤ timeout；修改后需重启生效');
