# 权限管理系统开发规范

> 依据：《权限管理系统需求文档》V1.4、《权限管理系统开发计划》
> 适用范围：本项目全部后端（Java 8 / Spring Boot 2.7 / Sa-Token / MyBatis-Plus）与前端（Vue 3 / Element Plus / Pinia / Vite）代码。

## 0. 约定与效力

| 级别 | 含义 | 违反后果 |
| --- | --- | --- |
| 【必须】 | 强制遵守，无例外 | Code Review 直接打回 |
| 【禁止】 | 强制不得出现 | Code Review 直接打回 |
| 【建议】 | 推荐做法，偏离需说明理由 | 评审时讨论 |

**执行方式**
1. IDE 安装 Alibaba Java Coding Guidelines 插件，提交前扫描无 Blocker/Critical；
2. 前端 `npm run lint` 必须零 error；
3. 所有代码必须经至少 1 人 Code Review 才能合入 `develop`；
4. 第 14 节的自检清单作为提 MR 前的强制检查项。

**红线条款（违反视为严重缺陷）**：3.6 权限校验、3.7 数据权限、6.3 缓存使用、7.4 SQL 安全。这四节的规定不接受"临时绕过"。

---

## 1. 命名规范

### 1.1 后端命名

| 对象 | 规则 | 示例 |
| --- | --- | --- |
| 包 | 全小写，单数，按业务域分层 | `com.company.permit.system.user` |
| 类 | UpperCamelCase | `SysUserServiceImpl` |
| 接口 | 不加 `I` 前缀 | `SysUserService` |
| 实现类 | 接口名 + `Impl` | `SysUserServiceImpl` |
| 方法 | lowerCamelCase，动词开头 | `selectUserPage`、`updateUserStatus` |
| 变量 | lowerCamelCase，禁止拼音、单字母（循环变量除外） | `deptId` ✅ / `bmId` ❌ |
| 常量 | UPPER_SNAKE_CASE | `MAX_DEPT_LEVEL` |
| 布尔 | 不加 `is` 前缀（避免序列化丢字段） | `enabled` ✅ / `isEnabled` ❌ |
| 测试类 | 被测类 + `Test` | `DataScopeMergeTest` |

### 1.2 后缀约定（强制）

| 后缀 | 用途 | 示例 |
| --- | --- | --- |
| `Controller` | 接口层 | `SysUserController` |
| `Service` / `ServiceImpl` | 业务层 | `SysUserService` |
| `Mapper` | 持久层 | `SysUserMapper` |
| `Entity` | 与数据库表一一对应（放在 `entity` 包） | `SysUser` |
| `DTO` | 入参（含查询对象 `XxxQueryDTO`） | `UserCreateDTO`、`UserQueryDTO` |
| `VO` | 出参 | `UserVO`、`UserDetailVO` |
| `BO` | 服务间内部传递对象 | `UserBO` |
| `Convert` | 对象转换（MapStruct 或手写） | `UserConvert` |
| `Enum` | 枚举 | `DataScopeEnum` |
| `Config` | 配置类 | `SaTokenConfig` |
| `Handler` / `Filter` / `Interceptor` / `Aspect` | 框架组件 | `DataPermissionHandler` |

**【禁止】用 `Entity` 直接接收前端参数或直接返回前端**，必须经 DTO / VO。

### 1.3 数据库命名

| 对象 | 规则 | 示例 |
| --- | --- | --- |
| 表 | `sys_` 前缀 + 小写下划线 + 单数 | `sys_user`、`sys_role_menu` |
| 字段 | 小写下划线 | `dept_id`、`create_time` |
| 主键 | `表名单数_id` | `user_id` |
| 唯一索引 | `uk_` 前缀 | `uk_username` |
| 普通索引 | `idx_` 前缀 | `idx_dept_id` |
| 布尔/状态 | `char(1)`，在注释中说明取值 | `status` 注释 `0正常 1停用` |

**【禁止】**表名前缀混用（如 `t_`、`tb_`）、拼音命名、保留字（`order`、`desc`、`key`）。

### 1.4 前端命名

| 对象 | 规则 | 示例 |
| --- | --- | --- |
| 目录/文件 | kebab-case | `views/system/user/index.vue` |
| 组件文件 | PascalCase（弹窗、卡片类） | `UserDialog.vue` |
| 组件名 | PascalCase，多单词 | `<user-dialog />` |
| 变量/方法 | camelCase | `queryParams`、`handleAdd` |
| 常量 | UPPER_SNAKE_CASE | `MAX_UPLOAD_SIZE` |
| Pinia store | camelCase，文件 `useXxxStore.js` | `useUserStore`、`usePermissionStore` |
| CSS 类 | kebab-case，模块内加前缀 | `.user-list`、`.user-form` |
| API 文件 | 与后端模块同名 | `api/system/user.js` |

**【禁止】**在 `.vue` 中写 `index`、`temp`、`test` 之类无意义命名；禁止中文文件名。

---

## 2. 工程与分层规范

### 2.1 分层职责（【必须】严格遵守）

| 层 | 允许做的事 | 禁止做的事 |
| --- | --- | --- |
| `Controller` | 参数校验、权限注解、调用 Service、组装 `R<T>` | 写业务逻辑、直接调 Mapper、开事务 |
| `Service` | 业务逻辑、事务边界、缓存读写、调用其他模块 Service | 直接操作 `HttpServletRequest`、返回前端 VO 以外的展示逻辑 |
| `Mapper` | 单表 CRUD、复杂查询 | 写业务判断 |
| `Entity` | 表映射 | 加业务方法、作为接口出入参 |

### 2.2 跨模块调用

- 【必须】模块间只能通过 `Service` 接口调用（如 `system.user` 调 `system.dept`）；
- 【禁止】跨模块直接注入 `Mapper`（如 `UserServiceImpl` 里注入 `SysRoleMapper`）；
- 理由：为后续拆微服务保留边界，跨模块 Mapper 直调会在拆分时产生大量返工。

### 2.3 依赖注入

- 【建议】使用 `@RequiredArgsConstructor` + `private final` 构造器注入；
- 【禁止】`@Autowired` 字段注入；【禁止】Service 之间循环依赖（出现即说明职责划分错误）。

---

## 3. 后端编码规范

### 3.1 Controller 规范

```java
@RestController
@RequestMapping("/api/system/user")
@RequiredArgsConstructor
@Validated
public class SysUserController {

    private final SysUserService userService;

    @GetMapping
    @SaCheckPermission("system:user:list")
    public R<PageResult<UserVO>> page(UserQueryDTO query) {
        return R.ok(userService.selectUserPage(query));
    }

    @PostMapping
    @SaCheckPermission("system:user:add")
    @Log(title = "用户管理", operType = OperType.INSERT)
    public R<Long> add(@Validated @RequestBody UserCreateDTO dto) {
        return R.ok(userService.createUser(dto));
    }
}
```

【必须】
- 每个方法必须有 `@SaCheckPermission`（除登录即可访问的接口）；
- 所有写操作必须有 `@Log`；
- 出参统一 `R<T>`，分页统一 `R<PageResult<T>>`；
- URL 使用名词复数，动作通过 HTTP Method 表达。

【禁止】
- 在 Controller 内写 `if` 业务分支、事务、SQL；
- 返回 `Map<String, Object>` 或无类型的 `Object`；
- 拼接 URL 动词（`/api/system/user/addUser`）——统一用 `POST /api/system/user`。

### 3.2 Service 规范

- 【必须】写操作方法加 `@Transactional(rollbackFor = Exception.class)`，**禁止**使用默认的 `@Transactional`（默认只回滚 `RuntimeException`，受检异常不回滚）；
- 【必须】事务方法内禁止做远程调用、HTTP 请求、大批量循环（长事务会拖垮连接池）；
- 【必须】校验类逻辑前置，避免"先写库再校验失败"；
- 【禁止】Service 方法返回 `null` 表示失败——失败一律抛业务异常；
- 【禁止】在 Service 中吞异常（`catch` 后不处理），必须记录日志或重新抛出。

### 3.3 异常处理

```java
// 业务异常统一使用
throw new ServiceException(ErrorCode.B01001);          // 用户名已存在
throw new ServiceException(ErrorCode.B06001, "用户不在您的数据范围内");
```

【必须】
- 业务异常统一继承 `ServiceException`，携带 `ErrorCode`；
- 全局异常处理器统一转换 `ServiceException` / `NotLoginException` / `NotPermissionException` / `MethodArgumentNotValidException` / `Exception`；
- 兜底 `Exception` 分支【必须】打印完整堆栈（`log.error(msg, e)`），且**不得**把堆栈或 SQL 信息返回给前端。

【禁止】
- `e.printStackTrace()`（无法进入日志系统、丢失 traceId）；
- `catch (Exception e) { }` 空捕获；
- 向前端返回原始异常 message（可能泄露表名、路径）。

### 3.4 参数校验

- 【必须】使用 Jakarta Validation 注解（`@NotBlank` / `@Size` / `@Pattern` / `@Min`），配合 `@Validated` + 分组（Create / Update）；
- 【禁止】在 Service 里手写 `if (StringUtils.isBlank(x))` 做基础非空校验（业务规则校验除外）；
- 【必须】分页参数设上限：`pageSize` 最大 100，`pageNum` 最大 10000，超出直接钳制或报错；
- 【必须】所有前端传入的 ID 做合法性校验（存在性 + 数据范围）。

### 3.5 日志规范

| 级别 | 使用场景 |
| --- | --- |
| `error` | 系统异常、需要人工介入（必须带异常对象） |
| `warn` | 业务异常、可自愈、安全事件（如登录失败、越权尝试） |
| `info` | 关键业务流程节点（登录、权限变更、导入导出） |
| `debug` | 调试信息，生产默认关闭 |

【必须】
- 日志使用占位符 `log.info("用户 {} 登录成功", username)`，【禁止】字符串拼接；
- 日志中【禁止】输出密码、Token、身份证、完整手机号、银行卡；
- 异步线程中打印日志必须先传递 MDC，否则 traceId 丢失。

【禁止】
- `System.out.println`；
- 在循环体内打印 info 级日志（会打爆磁盘）。

### 3.6 权限校验规范（★ 红线）

**【必须】** 所有需授权接口标注权限标识，且标识与数据库 `sys_menu.perms` 完全一致：

```java
@SaCheckPermission("system:user:add")
```

**规则**
1. 【必须】权限标识遵循 `模块:功能:动作`，动作取值固定：`list` / `query` / `add` / `edit` / `remove` / `export` / `import` / `resetPwd`；
2. 【禁止】在 Controller/Service 里手工判断角色编码来做鉴权（如 `if (roleKey.equals("admin"))`），鉴权统一走 Sa-Token；
3. 【必须】超级管理员的特权只在 `StpInterfaceImpl` 中通过返回 `*:*:*` 实现，【禁止】在业务代码中散落 `isAdmin()` 判断；
4. 【禁止】前端承担鉴权职责（前端 `v-permission` 只控制显隐，服务端必须独立校验）；
5. 【必须】新增接口时同步在初始化数据中补充权限标识，否则接口对所有人不可用。

### 3.7 数据权限规范（★ 红线）

**规则**
1. 【必须】所有查询业务数据的方法，走 MyBatis-Plus 数据权限插件自动追加条件，【禁止】手工拼接 `dept_id IN (...)`；
2. 【必须】所有**按 ID 操作的写接口**（修改、删除、重置密码、分配角色）在 Service 入口校验目标资源是否在当前用户数据范围内，范围外抛 `B06001`：

```java
// 每个写操作 Service 首行
dataScopeService.checkUserInScope(userId);   // 不通过则抛 B06001
```

3. 【必须】数据权限上下文（`DataScopeContext`）的 `ThreadLocal` 在 `finally` 中 `remove()`：

```java
try {
    DataScopeContext.set(ctx);
    // 业务逻辑
} finally {
    DataScopeContext.remove();   // 禁止遗漏，否则线程池复用会串权限
}
```

4. 【必须】异步任务、定时任务中如需数据权限上下文，使用 `TransmittableThreadLocal` 或显式传参；
5. 【禁止】在数据权限插件解析 SQL 失败时静默放行——**解析失败必须拒绝**（抛异常或追加恒假条件）；
6. 【必须】新增业务 Mapper 方法时，确认是否需要数据权限；不需要的（如个人中心、公共配置）显式加入白名单并写明理由；
7. 【必须】数据权限相关的新方法补充单元测试（并集算法、边界场景）。

### 3.8 对象转换与工具使用

- 【必须】Entity / DTO / VO 之间使用 `Convert` 类集中转换（MapStruct 或手写），【禁止】在业务逻辑中散落 `BeanUtils.copyProperties`；
- 【建议】集合批量转换抽成 `Convert.toVoList(...)`；
- 【禁止】`BeanUtils.copyProperties` 用于跨层大面积拷贝（字段静默丢失、类型不匹配无提示）。

---

## 4. 接口规范

### 4.1 URL 设计

```
GET    /api/system/user            分页查询
GET    /api/system/user/{userId}   详情
POST   /api/system/user            新增
PUT    /api/system/user            修改
DELETE /api/system/user/{userId}   删除
PUT    /api/system/user/{userId}/status       子资源动作
POST   /api/system/user/batchDelete           批量（避免 DELETE 带 body）
```

【禁止】URL 中使用动词（`/getUser`、`/deleteUser`）、大写字母、下划线。

### 4.2 统一响应体

```json
{
  "code": 200,
  "msg": "success",
  "data": {},
  "traceId": "7f3a1c2e..."
}
```

- 【必须】`traceId` 全链路透传（响应体 + 响应头 `X-Trace-Id`）；
- 【必须】业务错误使用附录错误码表，【禁止】自定义魔法数字；
- HTTP 状态码约定：401 未认证、403 无权限、500 系统异常；其余业务错误返回 HTTP 200 + 业务码（幂等与前端拦截器统一处理）。

### 4.3 分页

- 入参固定 `pageNum`（从 1 开始）、`pageSize`；
- 出参固定 `total`、`rows`；
- 【必须】导出场景不套用分页，但必须有总量上限（5 万行）与提示。

### 4.4 幂等与并发

- 【必须】所有写接口加防重复提交（`@RepeatSubmit`，Redis 实现，窗口 5 秒）；
- 【必须】用户、角色、菜单、部门表使用乐观锁 `version`；
- 【必须】乐观锁冲突返回 `C01002`「数据已被他人修改，请刷新后重试」；
- 【建议】重要写操作（删除角色、重置密码）记录操作日志并写明前后值。

---

## 5. 数据库规范

### 5.1 建表规范

- 【必须】引擎 `InnoDB`，字符集 `utf8mb4`，排序 `utf8mb4_general_ci`；
- 【必须】公共字段齐全：`create_by` / `create_time` / `update_by` / `update_time` / `del_flag` / `remark`（纯关联表可省略）；
- 【必须】时间字段用 `datetime`，值由应用统一填充（`MetaObjectHandler`），【禁止】依赖数据库 `NOW()`（多实例时区不一致）；
- 【必须】主键自增 `bigint`；
- 【必须】每个字段写 `COMMENT`；状态类字段必须注释取值含义。

### 5.2 SQL 规范

- 【必须】所有查询走 MyBatis-Plus 或预编译占位符 `#{}`；
- 【禁止】字符串拼接 SQL、`${}` 传参（`${}` 仅允许用于确认安全的动态表名/排序字段，且必须走白名单校验）；
- 【禁止】`SELECT *`，必须显式列名；
- 【禁止】在循环中查库（N+1），批量场景使用 `saveBatch` / `IN` 查询；
- 【必须】列表查询必须带索引，新增查询条件时同步评估索引；
- 【必须】深度分页（`pageNum` 过大）需评估性能，必要时改为游标查询。

### 5.3 逻辑删除

- 【必须】使用 MyBatis-Plus `@TableLogic` 统一处理，`del_flag` 取值 `0` 存在 / `2` 删除；
- 【禁止】物理删除业务表数据（日志清理除外，日志走归档策略）；
- 【必须】带 `del_flag` 的查询由 MP 自动追加条件，【禁止】手写 `del_flag = '0'`。

### 5.4 变更管理

- 【必须】所有 DDL / DML 变更通过 Flyway 新版本脚本（`V1.0.1__xxx.sql`）；
- 【禁止】脚本合入 `develop` 后再修改，只能追加；
- 【必须】生产执行前先备份，DDL 必须提供回滚脚本或 undo 说明；
- 【禁止】手工在数据库执行变更（含 test 环境，紧急情况也需补脚本）。

---

## 6. 缓存规范（★ 红线）

### 6.1 Key 命名

统一前缀 `sa:`（框架）与业务前缀，使用冒号分隔：

| Key | 用途 | TTL |
| --- | --- | --- |
| `sa:permission:<userId>` | 用户权限标识集合 | 30 分钟 |
| `sa:datascope:<userId>` | 用户数据范围 | 5 分钟 |
| `captcha:<captchaKey>` | 图形验证码 | 2 分钟 |
| `online:users` | 在线用户 ZSET | 无（成员由定时任务清理） |
| `pwd:retry:<username>` | 登录失败计数 | 15 分钟 |
| `limit:ip:<ip>` | IP 限流计数 | 1 分钟 |
| `repeat:<userId>:<uri>` | 防重复提交 | 5 秒 |

### 6.2 使用规则

- 【必须】所有 key 必须设置 TTL（`online:users` 等特殊情况需在注释中说明）；
- 【禁止】使用 `KEYS`、`SCAN` 前缀匹配来批量清理（生产会阻塞 Redis）——必须设计成可通过 `DEL` 精确清理的结构；
- 【禁止】权限缓存按 Token 维度设计（同一用户多会话会产生重复缓存，且无法精确清理）；
- 【必须】缓存双写场景先更新数据库再删除缓存，【禁止】先删缓存再更新数据库；
- 【必须】缓存对象实现 `Serializable`，统一使用 Jackson 序列化；

### 6.3 缓存失效（【必须】逐项实现，对应需求文档"生效时机矩阵"）

| 变更 | 失效动作 |
| --- | --- |
| 角色菜单权限变更 | `DEL sa:permission:<userId>`（该角色下全部用户） |
| 用户角色变更 | `DEL sa:permission:<userId>` |
| 角色数据范围变更 | `DEL sa:datascope:<userId>`（该角色下全部用户） |
| 部门树变更 | 失效该部门及子孙部门关联用户的数据范围缓存 |
| 用户停用/删除 | `DEL sa:permission:<userId>`、`DEL sa:datascope:<userId>`、删除 Sa-Token 会话、`ZREM online:users` |

---

## 7. 前端规范

### 7.1 请求封装

- 【必须】所有接口调用写在 `src/api/**` 中，组件内【禁止】直接使用 `axios`；
- 【必须】统一走 `utils/request.js`，集中处理 Token 注入、401 跳登录、403 提示、`code` 非 200 的错误提示、`traceId` 透传；
- 【必须】401 处理需去重（并发请求同时 401 时只跳转一次，避免多次弹窗）。

```javascript
// src/api/system/user.js
import request from '@/utils/request'

export function listUser(query) {
  return request({ url: '/api/system/user', method: 'get', params: query })
}
```

### 7.2 组件规范

- 【必须】`<style scoped>`，【禁止】全局样式污染；
- 【禁止】滥用 `!important`；
- 【必须】列表页统一结构：查询区（`el-form`）→ 操作区 → 表格区 → 分页；
- 【必须】弹窗组件独立成文件（`XxxDialog.vue`），【禁止】在 `index.vue` 里堆砌上千行；
- 【建议】单文件不超过 500 行，超出即拆分组件。

### 7.3 权限指令

- 【必须】按钮级权限使用 `v-permission="['system:user:add']"`；
- 【禁止】在模板中用角色编码做判断（`v-if="roleKey === 'admin'"`）；
- 前端权限控制仅影响展示，【禁止】以"按钮隐藏了"作为安全依据；
- 【必须】Vue 3 指令钩子使用新命名：`beforeMount` / `mounted` / `updated` / `unmounted`。**Vue 2 的 `bind` / `inserted` / `unbind` 在 Vue 3 中不会被调用**，写错会导致按钮权限静默失效：

```javascript
// ✅ Vue 3 写法
export default {
  mounted(el, binding) {
    const perms = binding.value || []
    if (perms.length && !hasPermission(perms)) el.parentNode?.removeChild(el)
  }
}
```

### 7.4 动态路由

- 【必须】`component` 字段到组件的映射使用 `import.meta.glob` 预建映射表。**Vite 不支持 `() => import('@/views/' + component)` 这种变量拼接**（无法静态分析，运行时报错）：

```javascript
// src/router/dynamic.js
const modules = import.meta.glob('/src/views/**/*.vue')

export function resolveComponent(component) {
  return modules[`/src/views/${component}.vue`] || (() => import('@/views/error/404.vue'))
}
```

- 【必须】动态路由注入逐条调用 `router.addRoute()`——Vue Router 4 已**移除** `addRoutes`；
- 【必须】404 兜底路由写 `path: '/:pathMatch(.*)*'`——Vue Router 4 中 `path: '*'` 无效；
- 【必须】路由守卫中若已有权限信息则不重复请求 `/auth/info`；
- 【必须】处理"无任何权限用户登录"的场景，避免白屏（跳转 401 或提示页）。

### 7.5 状态管理

- 【必须】使用 Pinia，store 按 `useUserStore` / `usePermissionStore` / `useAppStore` 划分，文件放 `src/stores/`；
- 【禁止】在 store 中直接使用 axios，【必须】通过 `src/api` 调用；
- 【禁止】把表格查询条件、弹窗开关等页面级状态放进 store（用组件 `ref` / `reactive`）；
- 【禁止】使用 `mapState` / `mapGetters` / `commit` / `dispatch` 等 Vuex 写法（Pinia 中已不存在）。

```javascript
// ✅ Pinia
export const usePermissionStore = defineStore('permission', () => {
  const permissionList = ref([])
  async function loadPermissions() {
    permissionList.value = await getAuthInfo()
  }
  return { permissionList, loadPermissions }
})
```

### 7.6 数据处理

- 【必须】后端返回的敏感字段按需求文档脱敏（手机号 `138****1234`），前端不再二次处理；
- 【禁止】在前端存储用户密码、明文敏感信息；
- 【必须】Token 存储位置遵循需求文档约定（内存 + localStorage 持久化），变更需同步改造请求封装与 CSRF 防护。

### 7.7 Vue 3 专项规范（易踩坑）

**【禁止】使用 Vue 2 中已移除的 API**，以下写法在 Vue 3 中静默失效或直接报错：

| 已移除 | Vue 3 替代方案 |
| --- | --- |
| `filters` 过滤器 | 计算属性 `computed` 或全局方法 `app.config.globalProperties.$fmt` |
| `Vue.prototype.$xxx` | `app.config.globalProperties.$xxx` |
| `$on` / `$off` / `$once`（EventBus） | `mitt` 或 Pinia |
| `.sync` 修饰符 | `v-model:propName` |
| `Vue.directive` / `Vue.use` / `new Vue()` | `app.directive` / `app.use` / `createApp()` |
| `$children` | `ref` 模板引用 |
| `$listeners` | 已合并进 `$attrs` |
| `size="medium"`（Element Plus） | `size="default"` |
| `type="text"`（el-button） | `link` |

**其它强制项**

- 【必须】响应式使用 `<script setup>` + `ref` / `reactive`；`reactive` 对象解构会**丢失响应性**，必须用 `toRefs`；
- 【必须】`<script setup>` 中不存在 `this`，禁止沿用 `this.xxx` 写法；
- 【必须】`v-for` 的 `key` 放在 `<template v-for>` 标签上（Vue 2 允许放在子元素，Vue 3 不允许）；
- 【必须】Element Plus 的 `ElMessage` / `ElMessageBox` / `ElLoading` 需**显式导入**（按需引入模式下无全局 `$message`）；
- 【必须】Element Plus 中文语言包通过 `el-config-provider` 配置，否则分页、日期组件显示英文；
- 【必须】环境变量使用 `import.meta.env.VITE_*`，【禁止】使用 `process.env.VUE_APP_*`（Vite 不注入 `process.env`）；
- 【禁止】在模板中对同一 `v-if` / `v-for` 混用（Vue 2 中 `v-for` 优先级更高，Vue 3 中 `v-if` 更高，行为不一致）。

---

## 8. Git 规范

### 8.1 分支模型

| 分支 | 说明 |
| --- | --- |
| `main` | 生产基线，只接受 `release/*` 与 `hotfix/*` 合入 |
| `develop` | 日常集成分支 |
| `release/v1.3` | 本次迭代发布分支 |
| `feature/<需求编号>-<简述>` | 功能分支，如 `feature/FR-USER-07-import` |
| `hotfix/<问题简述>` | 生产紧急修复，从 `main` 拉，修完同时回合 `main` 与 `develop` |

### 8.2 提交信息（Conventional Commits）

```
<type>(<scope>): <subject>
```

- `type`：`feat` / `fix` / `refactor` / `perf` / `style` / `docs` / `test` / `chore`；
- `scope`：模块名，如 `user`、`role`、`dataperm`、`auth`；
- `subject`：中文或英文均可，**禁止**"修改"、"更新"、"fix bug"这类无信息量描述。

```
feat(dataperm): 支持多角色数据范围并集计算
fix(auth): 修复 Bearer 前缀导致的全站 401
docs(plan): 补充数据权限插件兼容性说明
```

### 8.3 提交粒度

- 【必须】一次提交只做一件事；【禁止】把格式化、无关重构与新功能混在一个提交里；
- 【禁止】提交编译不通过的代码到 `develop`；
- 【禁止】提交 `target/`、`node_modules/`、IDE 配置、本地配置文件。

### 8.4 MR 流程

1. 从 `develop` 拉 `feature/*`；
2. 本地自测通过 + `npm run lint` / 编译通过；
3. 提 MR，填写：变更说明、影响范围、测试方式、关联需求编号；
4. 至少 1 人 Review 通过；
5. 合入 `develop`，删除功能分支。

---

## 9. 测试规范

- 【必须】核心算法（数据范围并集、部门树展开、密码策略、参数校验）写单元测试，分支覆盖 ≥80%；
- 【必须】新增/修改接口补充接口测试用例（Postman / Knife4j 集合）；
- 【必须】权限相关改动必须补充越权用例（垂直越权 + 水平越权）；
- 【必须】Bug 修复必须先补一个能复现的用例，再修；
- 【禁止】用 `@Disabled` / `@Ignore` 长期屏蔽失败用例（要么修，要么删除并说明）。

测试命名：`方法名_场景_预期`，如 `mergeScope_多角色含自定义_返回并集`。

---

## 10. 文档规范

- 【必须】新增接口同步更新接口文档（Swagger 注解 + 离线文档）；
- 【必须】需求变更必须回写《权限管理系统需求文档》并登记版本记录，【禁止】只在聊天里口头同步；
- 【必须】新增错误码登记到错误码表；
- 【必须】关键设计决策（如缓存 key 设计、数据权限实现）以注释或文档形式沉淀，【禁止】只存在于某人的记忆中。

---

## 11. 安全编码规范

| 项 | 要求 |
| --- | --- |
| 密码 | 【必须】BCrypt 加盐哈希；【禁止】MD5/SHA1/明文/可逆加密 |
| 传输 | 【必须】登录密码前端 RSA 加密后传输；全站 HTTPS |
| SQL 注入 | 【必须】预编译占位符；【禁止】`${}` 拼接用户输入 |
| XSS | 【必须】前端输出编码；富文本需白名单过滤 |
| 敏感数据 | 【必须】手机号/邮箱列表与导出脱敏；密码、Token 不落库、不落日志 |
| 越权 | 【必须】所有按 ID 的写接口校验数据范围（见 3.7） |
| 账号枚举 | 【必须】登录失败统一提示"用户名或密码错误"，不区分账号不存在与密码错误 |
| 文件上传 | 【必须】白名单校验扩展名 + 限制大小（10MB）+ 重命名存储；【禁止】使用原始文件名落地 |
| 日志 | 【禁止】日志中出现密码、Token、完整身份证号 |
| 依赖 | 【必须】新增依赖前确认无已知高危 CVE；【禁止】引入来路不明的工具包 |

---

## 12. 性能规范

- 【必须】列表查询必须有分页，【禁止】无分页的全表查询（导出除外，且受 5 万行上限约束）；
- 【禁止】循环内查库（N+1）、循环内做 Redis 单次调用（用 Pipeline / 批量接口）；
- 【必须】数据权限的 `IN` 列表超过 1000 个元素时改用 `EXISTS` 子查询或临时表；
- 【必须】耗时超过 500ms 的 SQL 记录并排查（开启慢 SQL 日志）；
- 【必须】数据权限条件涉及的字段（`dept_id`、`create_by`）必须有索引；
- 【建议】接口单次响应数据量控制在 1MB 以内。

---

## 13. 常见错误示例（❌ / ✅）

**❌ 在 Controller 写业务逻辑**
```java
@PostMapping("/api/system/user")
public R add(@RequestBody SysUser user) {
    if (userMapper.selectByUsername(user.getUsername()) != null) {
        return R.fail("用户名已存在");
    }
    user.setPassword(new BCryptPasswordEncoder().encode("123456"));
    userMapper.insert(user);
    return R.ok();
}
```

**✅ 分层清晰 + 权限 + 日志**
```java
@PostMapping
@SaCheckPermission("system:user:add")
@Log(title = "用户管理", operType = OperType.INSERT)
public R<Long> add(@Validated @RequestBody UserCreateDTO dto) {
    return R.ok(userService.createUser(dto));
}
```

**❌ 手工拼接数据权限**
```java
wrapper.in("dept_id", dataScopeService.getDeptIds(userId));
```

**✅ 由插件统一追加，写操作单独校验范围**
```java
dataScopeService.checkUserInScope(userId);   // 写操作显式校验
return userService.page(query);              // 查询由插件自动过滤
```

**❌ 用 SCAN 清理缓存**
```java
redisTemplate.keys("sa:permission:" + userId + ":*");
```

**✅ 结构化 key + 精确删除**
```java
redisTemplate.delete("sa:permission:" + userId);
```

**❌ ThreadLocal 不清理**
```java
DataScopeContext.set(ctx);
return doBusiness();
```

**✅ finally 中清理**
```java
try {
    DataScopeContext.set(ctx);
    return doBusiness();
} finally {
    DataScopeContext.remove();
}
```

---

## 14. 提交前自检清单

**后端**
- [ ] 编译通过，Alibaba 插件无 Blocker / Critical
- [ ] 接口已加 `@SaCheckPermission`，权限标识已登记到初始化数据
- [ ] 写接口已加 `@Log` 与防重复提交
- [ ] 按 ID 的写接口已做数据范围校验
- [ ] `@Transactional` 已指定 `rollbackFor`
- [ ] `ThreadLocal`（数据权限上下文）已在 `finally` 中清理
- [ ] 新增缓存 key 已设 TTL，无 `KEYS`/`SCAN` 使用
- [ ] SQL 无 `${}` 拼接、无 `SELECT *`
- [ ] 日志无密码/Token，异常日志带异常对象
- [ ] 涉及权限/数据权限的改动已补越权测试用例
- [ ] 数据库变更已通过 Flyway 新脚本提交

**前端**
- [ ] `npm run lint` 零 error
- [ ] 接口调用写在 `src/api`，未直接使用 axios
- [ ] 按钮权限使用 `v-permission`（Vue 3 钩子 `mounted`），未用角色编码判断
- [ ] 未使用 Vue 2 已移除 API（`filters` / `$on` / `Vue.prototype` / `.sync` / `this.xxx` 于 setup 中）
- [ ] 动态路由组件映射走 `import.meta.glob`，未使用变量拼接 `import()`
- [ ] 动态路由注入用 `addRoute()`，404 用 `/:pathMatch(.*)*`
- [ ] `reactive` 解构已用 `toRefs` 保持响应性
- [ ] Element Plus 组件/方法已显式导入，中文语言包已配置
- [ ] 环境变量使用 `import.meta.env.VITE_*`
- [ ] 敏感字段展示已脱敏
- [ ] 401 / 403 处理正常，无重复弹窗
- [ ] 单文件未超 500 行，弹窗已拆分为独立组件

**通用**
- [ ] 提交信息符合 Conventional Commits，一次提交只做一件事
- [ ] 需求变更已回写需求文档并登记版本
- [ ] 新增错误码已登记到错误码表
- [ ] MR 描述包含变更说明、影响范围、测试方式、需求编号

---

## 15. Code Review 检查清单（评审人用）

| 维度 | 检查点 |
| --- | --- |
| 权限 | 新接口是否漏加权限注解？权限标识是否与菜单数据一致？ |
| 数据权限 | 是否存在绕过插件的手工拼接？写操作是否校验范围？ThreadLocal 是否清理？ |
| SQL | 是否有 `${}`、`SELECT *`、循环查库、缺索引的新查询条件？ |
| 事务 | `rollbackFor` 是否指定？是否有长事务（远程调用/循环）？ |
| 异常 | 是否吞异常？是否向前端泄露堆栈/SQL？ |
| 缓存 | key 是否有 TTL？是否存在前缀匹配清理？失效触发点是否覆盖？ |
| 安全 | 是否有敏感信息落日志/落库/返回前端？上传是否白名单？ |
| 并发 | 写接口是否有防重复提交？是否需要乐观锁？ |
| 可读性 | 命名是否达意？是否有超长方法（>80 行）/超长类（>800 行）？ |
| 测试 | 核心逻辑是否有测试？Bug 修复是否补了复现用例？ |
| 规范 | 命名、分层、后缀是否符合第 1、2 节？ |

---

> 本规范随项目迭代演进；修订需在《权限管理系统需求文档》版本记录中登记，并同步到团队。
