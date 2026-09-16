package com.company.permit.framework.dataperm;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * 多角色数据范围并集：任一角色为全部则不过滤；否则部门集合取并集，仅本人用 OR create_by。
 */
public final class DataScopeMerger {

    public static final String ALL = "1";
    public static final String DEPT_AND_CHILD = "2";
    public static final String DEPT_ONLY = "3";
    public static final String SELF = "4";
    public static final String CUSTOM = "5";

    private DataScopeMerger() {
    }

    @Data
    @AllArgsConstructor
    public static class RoleScope {
        private Long roleId;
        private String dataScope;
    }

    public static DataScopeInfo merge(Long userId, Long deptId, List<RoleScope> roles,
                                      Function<Long, Set<Long>> childrenFinder,
                                      Function<Long, Set<Long>> customDeptFinder) {
        DataScopeInfo info = new DataScopeInfo();
        info.setUserId(userId);
        info.setDeptId(deptId);
        if (roles == null || roles.isEmpty()) {
            return info;
        }
        for (RoleScope role : roles) {
            if (ALL.equals(role.getDataScope())) {
                info.setAll(true);
                info.getDeptIds().clear();
                info.setSelf(false);
                return info;
            }
        }
        Set<Long> deptIds = new HashSet<>();
        boolean self = false;
        for (RoleScope role : roles) {
            String scope = role.getDataScope();
            if (DEPT_AND_CHILD.equals(scope)) {
                if (deptId != null) {
                    deptIds.add(deptId);
                    Set<Long> children = childrenFinder.apply(deptId);
                    if (children != null) {
                        deptIds.addAll(children);
                    }
                }
            } else if (DEPT_ONLY.equals(scope)) {
                if (deptId != null) {
                    deptIds.add(deptId);
                }
            } else if (CUSTOM.equals(scope)) {
                Set<Long> custom = customDeptFinder.apply(role.getRoleId());
                if (custom != null) {
                    deptIds.addAll(custom);
                }
            } else if (SELF.equals(scope)) {
                self = true;
            }
        }
        info.setDeptIds(deptIds);
        info.setSelf(self);
        return info;
    }
}
