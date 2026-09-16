package com.company.permit.framework.dataperm;

import cn.dev33.satoken.stp.StpUtil;
import com.company.permit.framework.cache.CacheKeys;
import com.company.permit.framework.web.ErrorCode;
import com.company.permit.framework.web.ServiceException;
import com.company.permit.system.dept.entity.SysDept;
import com.company.permit.system.dept.mapper.SysDeptMapper;
import com.company.permit.system.role.entity.SysRole;
import com.company.permit.system.role.mapper.SysRoleDeptMapper;
import com.company.permit.system.role.mapper.SysRoleMapper;
import com.company.permit.system.user.entity.SysUser;
import com.company.permit.system.user.mapper.SysUserMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataScopeServiceImpl implements DataScopeService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysRoleDeptMapper roleDeptMapper;
    private final SysDeptMapper deptMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public DataScopeInfo buildContext(Long userId) {
        String key = CacheKeys.dataScope(userId);
        String cached = stringRedisTemplate.opsForValue().get(key);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, DataScopeInfo.class);
            } catch (Exception e) {
                stringRedisTemplate.delete(key);
            }
        }
        SysUser user = userMapper.selectUserByIdUnscoped(userId);
        if (user == null) {
            DataScopeInfo empty = new DataScopeInfo();
            empty.setUserId(userId);
            return empty;
        }
        List<SysRole> roles = roleMapper.selectRolesByUserId(userId);
        List<DataScopeMerger.RoleScope> scopes = new ArrayList<>();
        for (SysRole role : roles) {
            if ("0".equals(role.getStatus())) {
                scopes.add(new DataScopeMerger.RoleScope(role.getRoleId(), role.getDataScope()));
            }
        }
        DataScopeInfo info = DataScopeMerger.merge(userId, user.getDeptId(), scopes,
                deptId -> new HashSet<>(deptMapper.selectChildDeptIds(deptId)),
                roleId -> new HashSet<>(roleDeptMapper.selectDeptIdsByRoleId(roleId)));
        try {
            stringRedisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(info), 5, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("缓存数据范围失败 userId={}", userId);
        }
        return info;
    }

    @Override
    public void evict(Long userId) {
        stringRedisTemplate.delete(CacheKeys.dataScope(userId));
    }

    @Override
    public void evictByRoleId(Long roleId) {
        List<Long> userIds = userMapper.selectUserIdsByRoleId(roleId);
        for (Long userId : userIds) {
            stringRedisTemplate.delete(CacheKeys.dataScope(userId));
            stringRedisTemplate.delete(CacheKeys.permission(userId));
        }
    }

    @Override
    public void evictByDeptChange(Long deptId) {
        Set<Long> deptIds = new HashSet<>();
        deptIds.add(deptId);
        List<Long> children = deptMapper.selectChildDeptIds(deptId);
        if (children != null) {
            deptIds.addAll(children);
        }
        SysDept current = deptMapper.selectById(deptId);
        if (current != null && current.getParentId() != null && current.getParentId() != 0L) {
            deptIds.add(current.getParentId());
        }
        List<Long> userIds = userMapper.selectUserIdsByDeptIds(new ArrayList<>(deptIds));
        for (Long userId : userIds) {
            stringRedisTemplate.delete(CacheKeys.dataScope(userId));
        }
    }

    @Override
    public void checkUserInScope(Long userId) {
        if (userId == null) {
            throw new ServiceException(ErrorCode.B06001);
        }
        DataScopeInfo info = currentInfo();
        if (info != null && info.isAll()) {
            return;
        }
        SysUser target = userMapper.selectUserByIdUnscoped(userId);
        if (target == null) {
            throw new ServiceException(ErrorCode.B01003);
        }
        if (!inScope(info, target.getDeptId(), target.getCreateBy(), target.getUserId())) {
            throw new ServiceException(ErrorCode.B06001);
        }
    }

    @Override
    public void checkDeptInScope(Long deptId) {
        if (deptId == null) {
            throw new ServiceException(ErrorCode.B06001);
        }
        DataScopeInfo info = currentInfo();
        if (info != null && info.isAll()) {
            return;
        }
        if (info == null || info.getDeptIds() == null || !info.getDeptIds().contains(deptId)) {
            throw new ServiceException(ErrorCode.B06001);
        }
    }

    private DataScopeInfo currentInfo() {
        DataScopeInfo ctx = DataScopeContext.get();
        if (ctx != null) {
            return ctx;
        }
        if (StpUtil.isLogin()) {
            return buildContext(StpUtil.getLoginIdAsLong());
        }
        return null;
    }

    private boolean inScope(DataScopeInfo info, Long deptId, String createBy, Long targetUserId) {
        if (info == null || info.denyAll()) {
            return false;
        }
        boolean deptOk = info.getDeptIds() != null && deptId != null && info.getDeptIds().contains(deptId);
        boolean selfOk = info.isSelf() && info.getUserId() != null
                && (String.valueOf(info.getUserId()).equals(createBy) || info.getUserId().equals(targetUserId));
        return deptOk || selfOk;
    }
}
