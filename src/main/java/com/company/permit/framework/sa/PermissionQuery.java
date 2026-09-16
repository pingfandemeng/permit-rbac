package com.company.permit.framework.sa;

import java.util.List;

public interface PermissionQuery {
    List<String> listPerms(Long userId);

    List<String> listRoleKeys(Long userId);

    List<Long> listUserIdsByRole(Long roleId);

    String getUsername(Long userId);
}
