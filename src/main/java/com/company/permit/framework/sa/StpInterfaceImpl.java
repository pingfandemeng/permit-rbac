package com.company.permit.framework.sa;

import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import com.company.permit.framework.cache.PermissionCacheService;
import com.company.permit.framework.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final PermissionQuery permissionQuery;
    private final PermissionCacheService permissionCacheService;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        Long userId = Long.valueOf(String.valueOf(loginId));
        if (isSuperAdmin(userId, loginId)) {
            return Collections.singletonList(SecurityUtils.WILDCARD_PERM);
        }
        Set<String> cached = permissionCacheService.getPermissions(userId);
        if (cached != null && !cached.isEmpty()) {
            return new ArrayList<>(cached);
        }
        if (cached != null) {
            return Collections.emptyList();
        }
        List<String> perms = permissionQuery.listPerms(userId);
        permissionCacheService.savePermissions(userId, perms);
        return perms;
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Long userId = Long.valueOf(String.valueOf(loginId));
        return permissionQuery.listRoleKeys(userId);
    }

    private boolean isSuperAdmin(Long userId, Object loginId) {
        if (SecurityUtils.ADMIN_USER_ID == userId) {
            return true;
        }
        try {
            Object username = StpUtil.getSessionByLoginId(loginId).get("username");
            return SecurityUtils.ADMIN_USERNAME.equals(String.valueOf(username));
        } catch (Exception e) {
            String username = permissionQuery.getUsername(userId);
            return SecurityUtils.ADMIN_USERNAME.equals(username);
        }
    }
}
