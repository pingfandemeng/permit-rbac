package com.company.permit.framework.dataperm;

import com.baomidou.mybatisplus.extension.plugins.handler.DataPermissionHandler;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.HexValue;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.schema.Column;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 解析失败一律拒绝放行（追加 1=2），禁止静默越权。
 */
@Slf4j
@Component
public class PermitDataPermissionHandler implements DataPermissionHandler {

    private static final Set<String> SKIP_MAPPERS = new HashSet<>(Arrays.asList(
            "SysConfigMapper",
            "SysLoginLogMapper",
            "SysOperLogMapper",
            "SysMenuMapper",
            "SysRoleMenuMapper",
            "SysUserRoleMapper",
            "SysRoleDeptMapper",
            "SysRoleMapper"
    ));
    private static final Set<String> SKIP_METHODS = new HashSet<>(Arrays.asList(
            "selectByUsername",
            "selectUserByIdUnscoped",
            "selectCountByUsername",
            "selectCountByPhone",
            "selectRoleKeysByUserId",
            "selectPermsByUserId",
            "selectMenuIdsByRoleId",
            "selectDeptIdsByRoleId",
            "selectRoleIdsByUserId",
            "selectUserIdsByRoleId",
            "selectById",
            "selectCount",
            "selectOne",
            "selectBatchIds",
            "selectByMap"
    ));
    private static final Set<String> FORCE_APPLY_MAPPERS = new HashSet<>(Collections.singletonList("SysUserMapper"));

    private final ConcurrentHashMap<String, DataScope> annotationCache = new ConcurrentHashMap<>();

    @Override
    public Expression getSqlSegment(Expression where, String mappedStatementId) {
        if (shouldSkip(mappedStatementId)) {
            return where;
        }
        DataScopeInfo info = DataScopeContext.get();
        if (info == null || info.isAll()) {
            return where;
        }
        try {
            DataScope meta = resolveAnnotation(mappedStatementId);
            String alias = meta == null ? "" : meta.alias();
            String deptCol = meta == null ? "dept_id" : meta.deptColumn();
            String userCol = meta == null ? "create_by" : meta.userColumn();
            Expression scope = buildScope(info, alias, deptCol, userCol);
            if (where == null) {
                return scope;
            }
            return new AndExpression(new Parenthesis(where), new Parenthesis(scope));
        } catch (Exception e) {
            log.error("数据权限 SQL 解析失败，拒绝放行 mappedStatementId={}", mappedStatementId, e);
            return deny(where);
        }
    }

    private Expression deny(Expression where) {
        EqualsTo deny = new EqualsTo();
        deny.setLeftExpression(new LongValue(1));
        deny.setRightExpression(new LongValue(2));
        if (where == null) {
            return deny;
        }
        return new AndExpression(new Parenthesis(where), deny);
    }

    private Expression buildScope(DataScopeInfo info, String alias, String deptCol, String userCol) {
        if (info.denyAll()) {
            EqualsTo deny = new EqualsTo();
            deny.setLeftExpression(new LongValue(1));
            deny.setRightExpression(new LongValue(2));
            return deny;
        }
        String prefix = (alias == null || alias.isEmpty()) ? "" : alias + ".";
        Expression deptExpr = null;
        if (info.getDeptIds() != null && !info.getDeptIds().isEmpty()) {
            if (info.getDeptIds().size() > 1000) {
                log.warn("数据权限 IN 列表超过 1000，当前 {} 个部门，建议改为 EXISTS", info.getDeptIds().size());
            }
            List<Expression> values = new ArrayList<>();
            for (Long deptId : info.getDeptIds()) {
                values.add(new LongValue(deptId));
            }
            InExpression in = new InExpression();
            in.setLeftExpression(new Column(prefix + deptCol));
            in.setRightItemsList(new ExpressionList(values));
            deptExpr = in;
        }
        Expression selfExpr = null;
        if (info.isSelf() && info.getUserId() != null) {
            EqualsTo eq = new EqualsTo();
            eq.setLeftExpression(new Column(prefix + userCol));
            eq.setRightExpression(new StringValue(String.valueOf(info.getUserId())));
            selfExpr = eq;
        }
        if (deptExpr != null && selfExpr != null) {
            return new OrExpression(new Parenthesis(deptExpr), new Parenthesis(selfExpr));
        }
        if (deptExpr != null) {
            return deptExpr;
        }
        if (selfExpr != null) {
            return selfExpr;
        }
        EqualsTo deny = new EqualsTo();
        deny.setLeftExpression(new HexValue("1"));
        deny.setRightExpression(new HexValue("2"));
        return deny;
    }

    private boolean shouldSkip(String mappedStatementId) {
        if (mappedStatementId == null) {
            return true;
        }
        int lastDot = mappedStatementId.lastIndexOf('.');
        if (lastDot < 0) {
            return true;
        }
        String className = mappedStatementId.substring(0, lastDot);
        String method = mappedStatementId.substring(lastDot + 1);
        if (method.contains("_COUNT") || method.endsWith("_mpCount")) {
            method = method.replace("_mpCount", "").replace("_COUNT", "");
        }
        if (SKIP_METHODS.contains(method) || method.contains("Unscoped")) {
            return true;
        }
        String simple = className.substring(className.lastIndexOf('.') + 1);
        if (SKIP_MAPPERS.contains(simple)) {
            return true;
        }
        DataScope annotation = resolveAnnotation(mappedStatementId);
        if (annotation != null) {
            return false;
        }
        return !FORCE_APPLY_MAPPERS.contains(simple);
    }

    private DataScope resolveAnnotation(String mappedStatementId) {
        return annotationCache.computeIfAbsent(mappedStatementId, id -> {
            try {
                int lastDot = id.lastIndexOf('.');
                String className = id.substring(0, lastDot);
                String methodName = id.substring(lastDot + 1)
                        .replace("_mpCount", "")
                        .replace("_COUNT", "");
                Class<?> clazz = Class.forName(className);
                for (Method method : clazz.getMethods()) {
                    if (method.getName().equals(methodName)) {
                        return method.getAnnotation(DataScope.class);
                    }
                }
            } catch (Exception ignored) {
                return null;
            }
            return null;
        });
    }
}
