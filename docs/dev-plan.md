# 权限管理系统开发计划

> 依据：《权限管理系统需求文档》V1.4
> 本文为开发执行层面的落地方案，需求基线以需求文档为准；两者冲突时以需求文档为准并回写修订。

## 0. 前置假设与约定

| 项 | 假设 | 若不满足的影响 |
| --- | --- | --- |
| 人力 | 后端 2 人、前端 1 人全职；测试由后端交叉 + 专人（0.5 人） | 人日等比例缩放，工期非线性增长 |
| 总工期 | 6 周（对应需求文档 M1–M4） | 见第 10 节排期总表 |
| 环境 | dev / test / prod 三套，MySQL 8.0 + Redis 6 + Nginx | 需求文档允许 MySQL 5.7，但**建议统一 8.0**（5.7 已 EOL） |
| 交付形态 | 单体 Spring Boot 应用 + 前端静态资源 | 需求文档已明确本期不拆微服务 |
| 编码 | 全链路 UTF-8（含数据库、Redis、Nginx、IDE） | — |
| 时区 | 应用 JVM、MySQL、Docker 容器统一 Asia/Shanghai | 否则日期字段与日志时间错乱 |

**架构决策：单模块 vs 多模块**
采用**单 Maven 模块 + 严格分包**。理由：6 周工期、3 人团队，多模块会显著提升构建与联调成本；通过包边界（`framework` / `system` / `monitor`）已经保证了未来可拆分性，拆分时按包迁移即可。

---

## 1. 技术选型与版本锁定

### 1.1 后端依赖

| 依赖 | 版本 | 说明 |
| --- | --- | --- |
| JDK | 8u362+ | 需求文档锁定 Java 8（已 EOL，见风险 R1） |
| Spring Boot | 2.7.18 | 2.7.x 最终版，已 EOL |
| Sa-Token | `sa-token-spring-boot-starter` 1.39.0 | **注意：不要选 spring-boot3-starter** |
| Sa-Token Redis | `sa-token-dao-redis-jackson` 1.39.0 | 与 starter 版本严格一致 |
| MyBatis-Plus | 3.5.5 | `DataPermissionInterceptor` 需 ≥3.5.0 |
| MySQL Driver | 8.0.33（`com.mysql:mysql-connector-j`） | 兼容 MySQL 5.7 / 8.0 |
| Redis | spring-boot-starter-data-redis（Lettuce） | Sa-Token 用 Jackson 序列化 |
| EasyExcel | 3.3.4 | 阿里出品，Java 8 兼容，避免 POI 内存溢出 |
| EasyCaptcha | 1.6.2 | 图形验证码 |
| Hutool | 5.8.22 | 工具类（避免重复造轮子） |
| Flyway | 9.22.3（或 Spring Boot 内置版本） | 数据库脚本版本管理 |
| Knife4j | 4.3.0（OpenAPI 3）或 SpringFox 3.0.0 | 接口文档；Boot 2.7 建议 SpringFox 3.0.0 更稳 |
| Lombok | 1.18.28 | — |

### 1.2 前端依赖

| 依赖 | 版本 | 说明 |
| --- | --- | --- |
| Vue | 3.4.x | 组合式 API + `<script setup>` |
| Vue Router | 4.3.x | Vue 3 对应 v4；注意 `addRoutes` 已移除 |
| Pinia | 2.1.x | 替代 Vuex 3；Token 持久化用 `pinia-plugin-persistedstate` |
| Element Plus | 2.6.x | 替代 Element UI 2.15 |
| Axios | 1.6.x | — |
| Vite | 5.x | 替代 Vue CLI 5（webpack） |
| unplugin-auto-import / unplugin-vue-components | 最新 | Element Plus 按需引入 |
| Node.js | 18.18+（建议 20 LTS） | Vue 3 + Vite 5 的硬性要求；用 `.nvmrc` + `engines` 锁定，CI 用同一版本 |

### 1.3 版本天花板（Java 8 约束，提前确认）

- MyBatis-Plus 4.x、Spring Boot 3.x、JDK 17 生态**不可用**；
- EasyExcel 4.x 要求 Java 8+，可用但建议锁 3.3.4（社区案例最多）；
- 所有依赖选型前须确认 `maven-compiler-plugin` 目标为 1.8。

**注意**：以上约束仅限后端。前端已切换至 Vue 3 生态，不受 Java 8 限制，但需统一 Node 18.18+（见 1.2）。前后端技术栈选择互相独立，后端维持 Java 8 不影响前端使用 Vue 3。

---

## 2. 工程结构

### 2.1 后端包结构

```
permit/
├── sql/                                # Flyway 脚本
│   └── V1.0.0__init_schema.sql
├── src/main/java/com/company/permit/
│   ├── PermitApplication.java
│   ├── framework/                      # 与业务无关，可整体抽出为公共包
│   │   ├── config/                     # SaTokenConfig / MybatisPlusConfig / WebMvcConfig / AsyncConfig / RedisConfig
│   │   ├── sa/                         # StpInterfaceImpl（权限与角色加载）、Token 前缀过滤器、踢人服务
│   │   ├── dataperm/                   # DataPermissionInterceptor、DataPermissionHandler、DataScopeContext
│   │   ├── log/                        # @Log 注解、LogAspect、TraceIdFilter、MDC 线程池装饰
│   │   ├── excel/                      # EasyExcel 封装、导入校验框架
│   │   ├── cache/                      # 权限缓存、数据范围缓存、在线用户 ZSET
│   │   ├── security/                   # RSA 解密、参数校验分组
│   │   ├── web/                        # R<T> 统一响应、GlobalExceptionHandler、BaseController、分页封装
│   │   └── util/                       # 部门树、IP、UA 解析、脱敏
│   ├── system/                         # 系统管理
│   │   ├── auth/                       # 登录、验证码、登出、info、check
│   │   ├── user/                       # controller / service / mapper / entity / vo / bo
│   │   ├── role/
│   │   ├── menu/
│   │   ├── dept/
│   │   ├── config/
│   │   └── profile/
│   └── monitor/                        # 日志监控
│       ├── loginlog/
│       ├── operlog/
│       └── online/
└── src/main/resources/
    ├── application.yml / -dev.yml / -test.yml / -prod.yml
    └── logback-spring.xml
```

**分层约束**：Controller 不做业务逻辑；Service 层事务；跨模块调用只能依赖 Service 接口，禁止跨模块 Mapper 直调（为后续拆服务做准备）。

### 2.2 前端目录结构

```
src/
├── api/            # 按模块拆分接口定义
├── assets/
├── components/     # 通用组件（DictTag、Pagination、RightToolbar 等）
├── directive/      # v-permission 指令
├── layout/         # 侧边栏、顶栏、面包屑
├── router/         # 静态路由（登录/404）+ dynamic.js（import.meta.glob 组件映射）
├── stores/         # Pinia store：user / permission / app
├── utils/          # request.js（含 401/403 拦截、traceId、RSA）、auth.js（Token 存取）
├── views/
│   ├── login.vue
│   ├── system/     # user / role / menu / dept / config
│   ├── monitor/    # loginlog / operlog / online
│   └── profile/
└── permission.js   # 路由守卫：拉取 /auth/info → 生成动态路由
```

### 2.3 分支与环境策略

| 分支 | 用途 | 部署环境 |
| --- | --- | --- |
| `main` | 生产基线，仅接受 `release/*` 合入 | prod |
| `release/v1.3` | 本次迭代发布分支 | test → prod |
| `develop` | 集成分支 | dev |
| `feature/FR-xxx-简述` | 功能分支，从 `develop` 拉 | — |
| `hotfix/*` | 生产紧急修复 | prod |

提交信息规范：`<type>(<scope>): <subject>`，type ∈ `feat / fix / refactor / perf / docs / test / chore`。

---

## 3. 数据库落地

### 3.1 建表规范

- 引擎 InnoDB，字符集 `utf8mb4`，排序规则 `utf8mb4_general_ci`（MySQL 8 可用 `utf8mb4_0900_ai_ci`，但为兼容 5.7 统一 general_ci）；
- 所有表含公共字段：`create_by` / `create_time` / `update_by` / `update_time` / `del_flag` / `remark`（关联表除外）；
- 逻辑删除统一 `del_flag char(1) default '0'`（`'0'` 存在、`'2'` 删除，沿用 RuoYi 约定）；
- 完整 DDL 见**附录 A**，Flyway 脚本命名 `V1.0.0__init_schema.sql`。

### 3.2 Flyway 规则

- 脚本一经合入 `develop` 禁止修改，只能追加新版本；
- 每个环境独立 `flyway_schema_history` 表；
- prod 执行前必须先备份。

---

## 4. 关键技术方案（难点与实现要点）

### 4.1 Sa-Token 集成（★ 高风险，优先攻克）

**坑 1：Bearer 前缀**
需求文档约定请求头为 `Authorization: Bearer <token>`，而 Sa-Token 直接读取 `token-name` 指定的 header 值，会把 `"Bearer xxx"` 整体当作 token，导致全部请求 401。
**解决**：注册一个 `OncePerRequestFilter`，在 Sa-Token 过滤器之前剥离 `Bearer ` 前缀（或将配置改为前端不带前缀——但需求文档已约定 Bearer，故采用剥离方案）。

**坑 2：从数据库加载 timeout / activity-timeout**
Sa-Token 的 `timeout`/`activity-timeout` 属于框架级配置，默认从 yml 读。需求 FR-CONFIG-02 要求可配置。
**解决**：以 `@Bean SaTokenConfig` 方式在应用启动时读取 `sys_config` 注入（yml 中的值仅作为数据库不可用时的兜底默认值）；修改参数后通过 `SaManager.setSaTokenConfig(...)` 热更新，或提示"重启生效"——**建议本期限定为重启生效**，避免热更新引发的并发问题，并在参数页面注明。

**坑 3：401 / 403 映射**
Sa-Token 抛出 `NotLoginException` / `NotPermissionException` / `NotRoleException`。在 `GlobalExceptionHandler` 中映射：
- `NotLoginException` → HTTP 200 + `code=A01001`（**注意**：需求约定业务码通过统一响应体返回，HTTP 状态码是否同步返回 401 需在 M1 定调，**建议 HTTP 状态同步返回 401/403 便于网关与前端拦截器识别**）

**其它实现点**
- 实现 `StpInterface`：`getPermissionList` 走 Redis 缓存（key `sa:permission:<userId>`），admin 账号直接返回 `["*:*:*"]`；
- `getRoleList` 返回角色编码（如 `admin` / `deptAdmin`）；
- Sa-Token 注解需注册 `SaInterceptor`（`registry.addInterceptor(new SaInterceptor(h -> StpUtil.checkLogin()))`），路由拦截器负责登录校验，注解由同一拦截器处理；
- 停用/踢人：`StpUtil.kickout(loginId)` 或 `StpUtil.logout`；FR-AUTH-06 要求"停用即删除 Redis 会话"。

### 4.2 权限缓存与失效

- key：`sa:permission:<userId>`，TTL 30 分钟；
- 失效时机：角色菜单变更 → `DEL` 拥有该角色全部用户的 key（通过 `sys_user_role` 反查 userId，量可控）；用户角色分配变更 → `DEL` 该用户；
- **禁止**用 `KEYS` / `SCAN` 前缀匹配（需求文档已明确约束）；
- 数据范围缓存：key `sa:datascope:<userId>`，TTL 5 分钟，失效触发点见需求文档"数据范围缓存策略"表。

### 4.3 数据权限插件（★ 最高风险）

**实现骨架**
1. 自定义 `DataPermissionHandler implements DataPermissionHandler`，重写 `getSqlSegment(Expression where, String mappedStatementId)`；
2. 通过 `mappedStatementId` 白名单跳过（登录、验证码、个人中心、公共配置）；
3. 上下文用 `ThreadLocal<DataScopeContext>` 在 Controller/Service 入口设置，在 finally 中 `remove()`（**必须**，否则线程池复用导致串权限）；
4. 结果拼装：
   - 任一角色"全部数据" → 返回原 where；
   - 否则 `dept_id IN (并集) OR create_by = <userId>`（仅本人时追加 OR）。

**必须处理的边界**
| 场景 | 处理方式 |
| --- | --- |
| JSQLParser 解析失败 | **拒绝放行**：抛异常或追加 `1=2`，严禁静默放行（安全红线） |
| 联表查询 | 约定主表别名，或在 Mapper 方法上加 `@DataScope(alias = "t1")` 显式指定 |
| count 语句 | MP 插件对 count 同样生效，需专项测试 |
| 导出（全量查询） | 必须同样追加条件，禁止绕过插件 |
| 异步线程 | ThreadLocal 不传递，须用 `TransmittableThreadLocal` 或显式传参 |
| 无 dept_id / create_by 的表 | 显式声明"跳过"或"仅本人"，禁止默认放行 |

**性能**：`dept_id`、`create_by` 必须建索引；`IN` 列表过长时（部门树展开超 1000）改为临时表或 EXISTS 子查询，压测阶段验证。

### 4.4 前端动态路由

- 登录成功后 `permission.js` 守卫调用 `/api/auth/info`，取回菜单树与权限标识；
- **`component` 字段映射组件（Vue 3 + Vite 的头号坑）**：`() => import('@/views/' + component)` 这种变量拼接在 webpack 下可用，但 **Vite 无法静态分析，会在运行时报错**。Vue 3 方案必须用 `import.meta.glob` 预先建映射表：

```javascript
// src/router/dynamic.js
const modules = import.meta.glob('/src/views/**/*.vue')   // 构建期扫描全部页面组件

export function resolveComponent(component) {
  const key = `/src/views/${component}.vue`
  return modules[key] || (() => import('@/views/error/404.vue'))
}
```

- **动态路由注入**：Vue Router 4 已移除 `addRoutes`，必须遍历后逐条调用 `router.addRoute()`；
- **404 兜底**：Vue Router 4 中 `path: '*'` 无效，须写 `path: '/:pathMatch(.*)*'`；
- 按钮级：`v-permission="['system:user:add']"`，指令内比对 Pinia store 中的权限集合；Vue 3 指令钩子已更名（`bind`→`beforeMount`、`inserted`→`mounted`、`unbind`→`unmounted`），旧写法不生效；
- 401 统一拦截：Axios 响应拦截器捕获后清 Token 跳登录页；403 提示"无权限"。

### 4.5 traceId 与操作日志

- `TraceIdFilter`：`MDC.put("traceId", UUID)`，同时写入响应头 `X-Trace-Id`；
- 异步线程池需包装 MDC（自定义 `MdcTaskDecorator`），否则子线程日志丢失 traceId；
- 操作日志：自定义 `@Log(title=..., operType=...)` + AOP `@Around`，**异步写入**（`@Async`），失败不影响主流程；
- 脱敏：按字段名黑名单（`password` / `oldPassword` / `newPassword` / `token` / `captcha`）替换为 `******`；
- `oper_param` 超 2000 字符截断。

### 4.6 Excel 导入导出

- 导入：EasyExcel `AnalysisEventListener` 逐行解析 → **先全量校验**（用户名重复、部门编码不存在、角色编码不存在）→ 全部通过才整批提交（单事务）→ 失败整批回滚并返回行号+原因；
- 导出：分页查询 + 流式写入，上限 5 万行（需求 FR 约束）；
- 脱敏：导出手机号/邮箱按需求文档规则脱敏。

### 4.7 在线用户集合

- `ZADD online:users <登录时间戳> <loginId>`（登录成功）；
- `ZREM`（登出、强制下线、停用）；
- 过期清理：定时任务每 5 分钟用 `StpUtil.getTokenActivityTimeout` 或 Redis 会话 key 存在性比对，剔除失效成员；
- **禁止**使用 `searchSessionId`（全库扫描）。

---

## 5. 任务分解（WBS）

记号：**B**=后端、**F**=前端、**T**=测试；工期单位为人日。

### M1 第 1–2 周：脚手架与认证（目标：可登录的框架原型）

| 编号 | 任务 | 角色 | 工期 | 依赖 | 完成标准 |
| --- | --- | --- | --- | --- | --- |
| T1.1 | 后端脚手架：Maven 工程、包结构、yml 多环境、logback | B1 | 1 | — | 启动无报错，健康检查通过 |
| T1.2 | 前端脚手架：Vite 5 + Vue 3 + Element Plus + Pinia、Axios 封装、路由骨架、`import.meta.glob` 组件映射、Node 版本锁定 | F1 | 1 | — | 能启动并渲染空壳布局 |
| T1.3 | 数据库 DDL + Flyway + 初始化数据（附录 A/B） | B1 | 1.5 | T1.1 | Flyway 执行成功，admin 可查到 |
| T1.4 | 统一响应体 `R<T>`、全局异常处理、错误码枚举 | B1 | 1 | T1.1 | 异常统一返回 code+msg+traceId |
| T1.5 | TraceIdFilter + MDC + 异步线程池包装 | B1 | 0.5 | T1.4 | 日志含 traceId，响应头可查 |
| T1.6 | **Sa-Token 集成**：依赖、Redis DAO、Bearer 剥离过滤器、StpInterface、SaInterceptor、401/403 映射 | B1 | 2 | T1.3 | FR-AUTH-06/07 可用 |
| T1.7 | 验证码接口（EasyCaptcha + Redis + captchaKey） | B2 | 1 | T1.1 | FR-AUTH-02 验收通过 |
| T1.8 | 登录/登出/`/auth/info`/`/auth/check` 接口 | B2 | 2 | T1.6 | FR-AUTH-01/04 验收通过 |
| T1.9 | 登录失败锁定 + IP 限流（Bucket4j 或自定义 Redis 计数） | B2 | 1.5 | T1.8 | FR-AUTH-05、安全项通过 |
| T1.10 | 密码 BCrypt + RSA 传输解密 + 强制改密标识 | B2 | 1 | T1.8 | FR-AUTH-08 验收通过 |
| T1.11 | 登录页 + 验证码 + Token 存储 + 请求/响应拦截器 | F1 | 2 | T1.8 | 可正常登录登出 |
| T1.12 | 动态路由生成 + 侧边栏渲染 + 权限指令 | F1 | 2.5 | T1.11 | FR-AUTH-04 验收通过 |
| T1.13 | M1 联调与冒烟 | B1/B2/F1 | 1 | 全部 | 端到端可登录并看到菜单 |

**M1 小计：约 17.5 人日**

### M2 第 3–4 周：系统管理核心（目标：管理端核心功能）

| 编号 | 任务 | 角色 | 工期 | 依赖 | 完成标准 |
| --- | --- | --- | --- | --- | --- |
| T2.1 | 部门管理：CRUD、树、ancestors 维护、层级≤5、删除校验 | B1 | 2 | T1.3 | FR-DEPT-01~04 通过 |
| T2.2 | 菜单管理：CRUD、树、权限标识唯一、显示/隐藏 | B1 | 2 | T1.3 | FR-MENU-01~04 通过 |
| T2.3 | 角色管理：CRUD、菜单分配、删除引用校验、内置角色保护 | B2 | 2.5 | T2.2 | FR-ROLE-01/02/04/06 通过 |
| T2.4 | 用户管理：CRUD、启停、重置密码、角色分配、乐观锁 | B2 | 3 | T2.1/T2.3 | FR-USER-01~05 通过 |
| T2.5 | 权限缓存（Redis）+ 变更失效 + 强制下线 | B1 | 1.5 | T1.6 | 生效时机矩阵符合 |
| T2.6 | 操作日志 AOP + 登录日志切面（基础版） | B1 | 1.5 | T1.5 | FR-LOG-01/02 落地 |
| T2.7 | 前端：部门/菜单/角色/用户四个模块的页面与表单 | F1 | 5 | T2.1~T2.4 | 页面可用、校验完整 |
| T2.8 | 前端：`v-permission` 指令接入（Vue 3 指令钩子 `mounted`）+ 按钮显隐 | F1 | 0.5 | T1.12 | 无权限按钮不渲染 |
| T2.9 | M2 联调与功能测试 | B1/B2/F1/T | 2 | 全部 | P0 用例通过 |

**M2 小计：约 20 人日**

### M3 第 5 周：数据权限、日志、参数与监控（目标：完整功能版本）

| 编号 | 任务 | 角色 | 工期 | 依赖 | 完成标准 |
| --- | --- | --- | --- | --- | --- |
| T3.1 | **数据权限插件**：拦截器、Handler、上下文 ThreadLocal、白名单、并集算法 | B1 | 3 | T2.5 | 单角色四类范围生效 |
| T3.2 | 多角色并集 + 自定义范围（sys_role_dept）+ 部门树变更失效 | B1 | 2 | T3.1 | 并集无数据丢失 |
| T3.3 | 写操作数据权限校验 + 部门管理员约束（FR-DEPT-ADMIN-01） | B2 | 2 | T3.1 | 越权返回 403 |
| T3.4 | 插件兼容性：分页、count、导出、联表；解析失败拒绝放行 | B1 | 1.5 | T3.1 | 兼容性用例通过 |
| T3.5 | 系统参数配置模块 + 参数合法性校验（FR-CONFIG-04） | B2 | 1.5 | T1.3 | 参数即时生效 |
| T3.6 | 个人中心：信息维护、改密、权限查看 | B2 | 1 | T1.8 | FR-PROFILE-01~03 通过 |
| T3.7 | Excel 导入（全量校验）/ 导出（5 万行上限） | B2 | 2 | T2.4 | FR-USER-06/07 通过 |
| T3.8 | 日志模块：查询、导出、保留策略清理 | B1 | 1.5 | T2.6 | FR-LOG-03/04 通过 |
| T3.9 | 系统监控：在线用户 ZSET、强制下线、服务状态（P2） | B1 | 1.5 | T1.6 | FR-MON-01/02 通过 |
| T3.10 | 前端：数据权限配置、系统参数、个人中心、日志、监控页面 | F1 | 4 | T3.1~T3.9 | 页面可用 |
| T3.11 | M3 联调 | 全体 | 1 | 全部 | P1 用例通过 |

**M3 小计：约 21 人日**

### M4 第 6 周：测试与上线（目标：生产可运行版本）

| 编号 | 任务 | 角色 | 工期 | 依赖 | 完成标准 |
| --- | --- | --- | --- | --- | --- |
| T4.1 | 功能全量回归（P0/P1 用例 100%） | T/B2/F1 | 2 | M3 | 用例通过率 100% |
| T4.2 | **越权与数据权限专项测试**（垂直、水平、跨部门、多角色并集） | B1/T | 2 | M3 | 全部越权用例被拦截 |
| T4.3 | 性能压测（JMeter，200 并发 + 10 万用户数据） | B1 | 2 | M3 | 需求文档性能指标达标 |
| T4.4 | 安全测试（注入、XSS、爆破、日志脱敏、传输） | B1/T | 1.5 | M3 | 无高危漏洞 |
| T4.5 | 部署文档、Nginx 配置、备份脚本、回滚方案 | B2 | 1 | — | 文档齐备 |
| T4.6 | 预发环境部署演练 + 生产上线 | B1/B2 | 1.5 | T4.5 | 生产可访问 |
| T4.7 | 缺陷修复缓冲 | 全体 | 2 | — | 严重/一般缺陷清零 |

**M4 小计：约 12 人日**

---

## 6. 开发规范

### 6.1 代码规范

- 阿里 Java 开发手册（IDE 安装 Alibaba Java Coding Guidelines 插件，提交前扫描）；
- 前端 ESLint + Prettier，`npm run lint` 通过方可提交；
- 实体分层：`entity`（与表一致）/ `bo`（业务对象）/ `vo`（出参）/ `dto`（入参），禁止直接用 entity 接收前端参数；
- 禁止在 Controller 写业务逻辑，禁止 Service 互相循环依赖。

### 6.2 接口规范

- 统一前缀 `/api`，RESTful 语义；
- 分页入参 `pageNum` / `pageSize`，出参 `total` / `rows`；
- 所有写接口必须标注 `@Log` 与 `@SaCheckPermission`；
- 所有按 ID 操作的写接口必须做数据范围校验（需求文档安全项）；
- 参数校验用 Jakarta Validation（` @Valid` + 分组），禁止手写 if 判空。

### 6.3 错误码

- 使用附录 C 的码表，新增需登记，禁止散落魔法数字；
- 响应体必带 `traceId`，前端错误提示展示 `msg`，`traceId` 用于排查。

### 6.4 数据库变更

- 任何 DDL / DML 变更必须走 Flyway 新版本脚本，禁止手工改库；
- 索引变更需在 test 环境验证执行计划后再上生产。

---

## 7. 测试计划

### 7.1 测试分层

| 层次 | 范围 | 负责人 | 目标 |
| --- | --- | --- | --- |
| 单元测试 | 数据范围并集算法、部门树展开、密码策略、参数校验 | 后端 | 核心算法分支覆盖 ≥80% |
| 接口测试 | 全部 `/api` 接口（可借助 Knife4j / Postman 集合） | 后端交叉 | 全部接口可用 |
| 功能测试 | 需求文档全部 P0/P1 验收标准 | 测试 | 通过率 100% |
| 专项测试 | 越权、数据权限、性能、安全 | 后端 + 测试 | 见 7.2/7.3 |

### 7.2 权限正确性专项（★ 必测，逐条对照需求文档）

| 用例 | 预期 |
| --- | --- |
| 未登录访问任意受保护接口 | 401 |
| 普通用户调用 `/api/system/user` POST | 403 |
| 普通用户直接调用 `/api/system/role/{id}/menus` | 403 |
| 用户 A 修改用户 B（同部门、非本人、无权限） | 403 |
| 部门管理员查询用户 | 仅返回本部门及以下 |
| 部门管理员修改范围外用户 | 403 |
| 部门管理员新增用户选范围外部门 | 拒绝 |
| 系统管理员配置角色"全部数据" | 拒绝（仅超管可配） |
| 系统管理员修改 `dept_admin_flag` | 403 |
| 角色"仅本人"查询 | 仅本人数据 |
| 角色"自定义"勾选 2 个部门 | 仅这两个部门数据 |
| 用户同时拥有"本部门及以下"+"自定义" | 返回**并集**，不丢数据 |
| 用户同时拥有"仅本人"+"本部门" | `create_by OR dept_id` 并集 |
| 停用在线用户后其请求 | 401 |
| 强制下线后其请求 | 401 |
| 角色权限变更后刷新页面 | 可见新菜单 |
| 部门改父级后"本部门及以下"查询 | 按新树生效 |
| 删除内置角色 | 拒绝 |
| 删除/停用当前登录账号自身 | 拒绝 |
| 批量操作包含 admin 或自身 | 跳过并提示 |

### 7.3 性能与安全

- **性能**：JMeter 脚本覆盖登录（200 并发 5 分钟）、用户列表（10 万数据）、混合场景；重点观察数据权限 OR 条件下的慢 SQL（>500ms 告警）。
- **安全**：SQLMap 注入扫描、XSS 用例、登录爆破（IP 限流 + 账号锁定）、抓包验证 RSA 加密、日志脱敏抽查、导出文件脱敏检查。

---

## 8. 部署方案

### 8.1 环境清单

| 环境 | 用途 | 数据源 |
| --- | --- | --- |
| dev | 开发自测 | 独立库，允许重置 |
| test | 测试与压测 | 独立库，数据 10 万级 |
| prod | 生产 | 独立库 + 每日备份 |

### 8.2 部署架构

```
浏览器 → Nginx(443, HTTPS)
           ├─ /            → 前端静态资源(dist)
           └─ /api/        → proxy_pass → Spring Boot(8080)
                                 ├─ MySQL 8.0
                                 └─ Redis 6
```

Nginx 要点：
- 强制 HTTPS，`proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for`（否则登录 IP 全是网关 IP）；
- `client_max_body_size 10m`（Excel 导入）；
- 静态资源 `Cache-Control`。

### 8.3 上线步骤

1. 全量备份数据库（`mysqldump`）+ 备份上一版 jar 与 dist；
2. 执行 Flyway 迁移脚本（先在 test 验证）；
3. 停服 → 替换 jar → 启动 → 健康检查 `/actuator/health`；
4. 前端 dist 覆盖发布；
5. 冒烟：登录、菜单、关键接口。

### 8.4 回滚

- 应用回滚：恢复上一版 jar 并重启（<5 分钟）；
- 数据库回滚：Flyway 脚本须提供对应的 undo 或手工回滚脚本，DDL 变更必须可回退；
- 备份策略：需求文档约定 RPO≤24h、RTO≤4h。

---

## 9. 风险登记册

| 编号 | 风险 | 影响 | 概率 | 应对 |
| --- | --- | --- | --- | --- |
| R1 | Java 8 / Boot 2.7 / MySQL 5.7 均已 EOL，无安全补丁（前端 Vue 2 风险已通过升级 Vue 3 消除） | 高 | 高 | 已在需求文档记录；建议立项即评估后端升级路径，至少锁死版本并禁用有 CVE 的依赖 |
| R2 | **数据权限插件**在联表/子查询/复杂 SQL 上解析失败或漏拦截 | 高（越权） | 中 | M3 提前 1 周启动；解析异常一律拒绝放行；每类查询补测试；必要时人工标注 `@DataScope(alias)` |
| R3 | 多角色 OR 条件导致索引失效，列表 P95 超标 | 中 | 中 | 必建 `dept_id`/`create_by` 索引；压测专项验证；超 1000 部门改 EXISTS/临时表 |
| R4 | Sa-Token Bearer 前缀、timeout 动态配置等集成细节踩坑 | 中 | 中 | M1 首日做技术验证（spike），2 天内出结论 |
| R5 | ThreadLocal 上下文未清理导致权限串号 | 高（越权） | 中 | 强制 finally remove；代码评审专项检查；异步用 TTL |
| R6 | 在线用户 ZSET 与 Sa-Token 会话状态不一致 | 低 | 中 | 定时比对清理；列表允许最终一致（秒级） |
| R7 | Excel 大批量导入超时/内存溢出 | 中 | 中 | EasyExcel 流式解析；单次上限 5000 行；全量校验后整批提交 |
| R8 | 权限变更缓存失效遗漏，导致"改了不生效"投诉 | 中 | 中 | 按需求文档失效触发表实现；上线前逐项验证生效时机矩阵 |
| R9 | 6 周工期偏紧（M1–M3 共 58.5 人日 + M4 12） | 高 | 中 | M4 的 T4.7 为缓冲；若延期优先砍 P2（系统监控） |
| R10 | 组织架构数据来源（手工 vs HR 对接）未确认 | 中 | 低 | 需求文档开放问题；默认手工维护，预留导入接口 |
| R11 | Vue 3 与 Vue 2 生态 API 不兼容（`addRoutes` 移除、Vite 不支持变量拼接 import、Element UI → Element Plus 属性变更） | 中 | 中 | 前端尚未启动开发，**无迁移成本**；M1 首日完成脚手架验证；将 4.4 节约定写入《开发规范》统一团队写法 |
| R12 | Node.js 版本不统一，导致本地可跑但 CI 构建失败 | 中 | 中 | `.nvmrc` + `package.json` 的 `engines` 字段锁定 Node 18.18+，CI 使用同一版本 |

---

## 10. 里程碑与排期总表

| 里程碑 | 周次 | 内容 | 人日 | 交付物 | 出口标准 |
| --- | --- | --- | --- | --- | --- |
| M1 | W1–W2 | 脚手架、DDL、认证、验证码、动态路由 | 17.5 | 可登录框架原型 | 端到端可登录并渲染动态菜单 |
| M2 | W3–W4 | 用户/角色/菜单/部门 + 权限缓存 | 20 | 管理端核心功能 | P0 用例通过 |
| M3 | W5 | 数据权限、日志、参数、个人中心、监控、Excel | 21 | 完整功能版本 | P1 用例通过 |
| M4 | W6 | 联调、性能、安全、上线 | 12 | 生产可运行版本 | 需求文档验收标准全达标 |

**合计约 70.5 人日**（不含项目管理与需求沟通）。按 3 人全职 6 周（90 人日容量）计算，留有约 20% 缓冲。

---

## 附录 A：核心 DDL（V1.0.0__init_schema.sql）

```sql
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
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE sys_role (
  role_id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  role_name       VARCHAR(50)  NOT NULL                COMMENT '角色名称',
  role_key        VARCHAR(100) NOT NULL                COMMENT '角色编码',
  data_scope      CHAR(1)      NOT NULL DEFAULT '2'    COMMENT '数据范围 1全部 2本部门及以下 3本部门 4仅本人 5自定义',
  dept_admin_flag CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '部门管理员标记 0否 1是',
  status          CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '状态 0正常 1停用',
  sort            INT          NOT NULL DEFAULT 0      COMMENT '排序',
  del_flag        CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '删除标记',
  create_by       VARCHAR(64)  NOT NULL DEFAULT '',
  create_time     DATETIME,
  update_by       VARCHAR(64)  NOT NULL DEFAULT '',
  update_time     DATETIME,
  remark          VARCHAR(500),
  PRIMARY KEY (role_id),
  UNIQUE KEY uk_role_key (role_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 菜单与权限表
CREATE TABLE sys_menu (
  menu_id    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  parent_id  BIGINT       NOT NULL DEFAULT 0      COMMENT '父菜单ID',
  menu_name  VARCHAR(50)  NOT NULL                COMMENT '菜单名称',
  menu_type  CHAR(1)      NOT NULL DEFAULT 'C'    COMMENT '类型 M目录 C菜单 F按钮',
  path       VARCHAR(200) NOT NULL DEFAULT ''     COMMENT '路由地址',
  component  VARCHAR(255)                         COMMENT '组件路径',
  perms      VARCHAR(100)                         COMMENT '权限标识',
  order_num  INT          NOT NULL DEFAULT 0      COMMENT '排序',
  visible    CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '显示状态 0显示 1隐藏',
  icon       VARCHAR(100) NOT NULL DEFAULT ''     COMMENT '图标',
  create_by  VARCHAR(64)  NOT NULL DEFAULT '',
  create_time DATETIME,
  update_by  VARCHAR(64)  NOT NULL DEFAULT '',
  update_time DATETIME,
  remark     VARCHAR(500),
  PRIMARY KEY (menu_id),
  KEY idx_parent_id (parent_id),
  KEY idx_perms (perms)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单与权限表';

-- 部门表
CREATE TABLE sys_dept (
  dept_id    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '部门ID',
  parent_id  BIGINT       NOT NULL DEFAULT 0      COMMENT '父部门ID',
  dept_name  VARCHAR(50)  NOT NULL                COMMENT '部门名称',
  dept_key   VARCHAR(50)  NOT NULL                COMMENT '部门编码',
  ancestors  VARCHAR(500) NOT NULL DEFAULT ''     COMMENT '祖级链，如 0,100,101',
  order_num  INT          NOT NULL DEFAULT 0      COMMENT '排序',
  status     CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '状态 0正常 1停用',
  del_flag   CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '删除标记',
  create_by  VARCHAR(64)  NOT NULL DEFAULT '',
  create_time DATETIME,
  update_by  VARCHAR(64)  NOT NULL DEFAULT '',
  update_time DATETIME,
  remark     VARCHAR(500),
  PRIMARY KEY (dept_id),
  UNIQUE KEY uk_dept_key (dept_key),
  KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- 关联表
CREATE TABLE sys_user_role (
  user_id BIGINT NOT NULL COMMENT '用户ID',
  role_id BIGINT NOT NULL COMMENT '角色ID',
  PRIMARY KEY (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

CREATE TABLE sys_role_menu (
  role_id BIGINT NOT NULL COMMENT '角色ID',
  menu_id BIGINT NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

CREATE TABLE sys_role_dept (
  role_id BIGINT NOT NULL COMMENT '角色ID',
  dept_id BIGINT NOT NULL COMMENT '部门ID',
  PRIMARY KEY (role_id, dept_id),
  KEY idx_role_dept (role_id, dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色自定义数据范围部门表';

-- 系统参数表
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统参数配置表';

-- 登录日志表（按月分表 + 定时归档）
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';

-- 操作日志表
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';
```

## 附录 B：初始化数据清单

| 类型 | 内容 |
| --- | --- |
| 账号 | `admin`，密码取 `sys.user.initPassword`，`pwd_reset_flag=1`，不可删除/停用 |
| 部门 | 根部门「总公司」 |
| 角色 | 超级管理员（`admin`，data_scope=1）、系统管理员（`sysAdmin`）、部门管理员（`deptAdmin`，`dept_admin_flag=1`，data_scope=2 锁定）、普通用户（`common`） |
| 菜单 | 系统管理（用户/角色/菜单/部门/参数）、日志监控（登录日志/操作日志/在线用户）、个人中心；含全部按钮级权限标识 |
| 参数 | `sys.user.initPassword`、`sys.pwd.minLength`、`sys.pwd.requireLetter`、`sys.pwd.requireDigit`、`sys.pwd.requireSymbol`、`sys.login.maxRetryCount`、`sys.login.lockMinutes`、`sys.log.retentionDays`、`sys.session.timeout`、`sys.session.activityTimeout` |

## 附录 C：错误码表（初版）

| 码 | 含义 | HTTP |
| --- | --- | --- |
| A01001 | 未认证或 Token 无效 | 401 |
| A01002 | Token 已过期，请重新登录 | 401 |
| A01003 | 无操作权限 | 403 |
| A01004 | 账号已停用，请联系管理员 | 401 |
| A01005 | 账号已锁定 | 401 |
| A01006 | 用户名或密码错误 | 401 |
| A01007 | 请先修改密码 | 200 |
| A02001 | 验证码错误 | 200 |
| A02002 | 验证码已过期 | 200 |
| B01001 | 用户名已存在 | 200 |
| B01002 | 手机号已被使用 | 200 |
| B01003 | 用户不存在 | 200 |
| B01004 | 不能操作当前登录账号 | 200 |
| B01005 | 导入数据校验未通过 | 200 |
| B02001 | 角色编码已存在 | 200 |
| B02002 | 该角色已分配用户，请先解除关联 | 200 |
| B02003 | 内置角色不可删除 | 200 |
| B03001 | 权限标识已存在 | 200 |
| B03002 | 存在子菜单，请先删除子菜单 | 200 |
| B04001 | 部门编码已存在 | 200 |
| B04002 | 存在子部门，无法删除 | 200 |
| B04003 | 部门下存在用户，无法删除 | 200 |
| B04004 | 已达最大层级（5 级） | 200 |
| B05001 | 参数键已存在 | 200 |
| B05002 | 参数值不合法 | 200 |
| B06001 | 目标资源不在数据权限范围内 | 403 |
| C01001 | 系统异常，请联系管理员 | 500 |
| C01002 | 数据已被他人修改，请刷新后重试 | 200 |
| C01003 | 请求参数不合法 | 200 |

---

> 本计划为执行基线，每个里程碑结束后回顾一次，偏差超过 20% 时重新评估排期。
