package com.company.permit.framework.dataperm;

public interface DataScopeService {

    DataScopeInfo buildContext(Long userId);

    void evict(Long userId);

    void evictByRoleId(Long roleId);

    void evictByDeptChange(Long deptId);

    void checkUserInScope(Long userId);

    void checkDeptInScope(Long deptId);
}
