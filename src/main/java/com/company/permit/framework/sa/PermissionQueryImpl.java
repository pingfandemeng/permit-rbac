package com.company.permit.framework.sa;

import com.company.permit.system.user.entity.SysUser;
import com.company.permit.system.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionQueryImpl implements PermissionQuery {

    private final SysUserMapper userMapper;

    @Override
    public List<String> listPerms(Long userId) {
        return userMapper.selectPermsByUserId(userId);
    }

    @Override
    public List<String> listRoleKeys(Long userId) {
        return userMapper.selectRoleKeysByUserId(userId);
    }

    @Override
    public List<Long> listUserIdsByRole(Long roleId) {
        return userMapper.selectUserIdsByRoleId(roleId);
    }

    @Override
    public String getUsername(Long userId) {
        SysUser user = userMapper.selectUserByIdUnscoped(userId);
        return user == null ? "" : user.getUsername();
    }
}
