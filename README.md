# 权限管理平台

基于 RBAC 的统一权限管理基础平台（单体 Spring Boot + Vue 3）。支持账号登录、用户 / 角色 / 菜单 / 部门维护、数据范围拦截、登录与操作审计、系统参数与个人中心。

需求、开发计划与编码规范见：

- [docs/requirements.md](docs/requirements.md)
- [docs/dev-plan.md](docs/dev-plan.md)
- [docs/coding-standards.md](docs/coding-standards.md)

## 技术栈

| 层 | 版本 |
| --- | --- |
| 后端 | JDK 8 · Spring Boot 2.7.18 · Sa-Token 1.39.0（Redis Jackson）· MyBatis-Plus 3.5.5 · Flyway 9.22.3 |
| 数据 | MySQL 8 · Redis 6 |
| 前端 | Vue 3.4 · Vue Router 4 · Pinia · Element Plus 2.6 · Vite 5 · Node 18.18+ |

## 默认账号

| 项 | 值 |
| --- | --- |
| 用户名 | `admin` |
| 初始密码 | `Admin@123456` |
| 说明 | 种子数据 `pwd_reset_flag=1`，**首次登录必须修改密码**。超级管理员权限由 `StpInterface` 返回 `*:*:*`，不依赖角色表。 |

## 本地启动

### 1. 基础设施

```bash
docker compose up -d
```

等待 MySQL、Redis 健康后继续。也可用本机已安装的 MySQL 8 / Redis，库名 `permit`，账号 `permit` / `permit123`（见 `src/main/resources/application-dev.yml`）。

### 2. 后端

需要 JDK 8 与 Maven：

```bash
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64   # 按实际路径调整
mvn -DskipTests package
java -jar target/permit-1.0.0.jar --spring.profiles.active=dev
```

服务监听 **http://127.0.0.1:18080**。健康检查：`GET /actuator/health`。Flyway 首次启动会执行 `V1.0.0__init_schema.sql`。

### 3. 前端

需要 Node.js 18.18+：

```bash
cd web
npm install
npm run dev
```

开发服务 **http://127.0.0.1:15173**，`/api` 代理到后端 18080。生产构建：`npm run build`，将 `web/dist` 交给 Nginx 并反代 `/api/`。

## 冒烟步骤

1. 打开前端，使用 `admin` / `Admin@123456` 与图形验证码登录。
2. 按提示修改密码（至少 8 位且含字母与数字），完成后重新登录。
3. 登录后应看到「系统管理」「日志监控」等动态菜单。
4. 进入角色管理 → 新增角色（如 `demo`）→ 分配菜单（至少勾选用户管理）→ 配置数据范围（如「本部门」）。
5. 进入用户管理 → 新增用户，选择部门（如研发部）并分配刚创建的角色。初始密码为系统参数 `sys.user.initPassword`。
6. 退出 admin，用新用户登录并修改密码。确认侧边栏仅有被授权菜单；用户列表仅返回其数据范围内的数据。

## 接口约定

- 前缀 `/api`，响应 `{ code, msg, data, traceId }`，成功 `code=200`。
- 认证头 `Authorization: Bearer <token>`；未登录 HTTP 401，无权限 HTTP 403。
- 分页入参 `pageNum` / `pageSize`，出参 `total` / `rows`。
- 日期 `yyyy-MM-dd HH:mm:ss`，时区 Asia/Shanghai。

## 本期未做（TODO）

- Excel 用户导入 / 导出（接口权限标识已入库，实现留待后续）。
- Knife4j 接口文档美化。
- 登录日志按月分表归档任务。

在线用户列表与强制下线已实现（Redis ZSET，禁止 `KEYS`/`SCAN`）。
