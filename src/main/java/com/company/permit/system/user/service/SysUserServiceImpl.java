package com.company.permit.system.user.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.permit.framework.cache.OnlineUserCacheService;
import com.company.permit.framework.cache.PermissionCacheService;
import com.company.permit.framework.dataperm.DataScopeService;
import com.company.permit.framework.util.SecurityUtils;
import com.company.permit.framework.web.Constants;
import com.company.permit.framework.web.ErrorCode;
import com.company.permit.framework.web.PageResult;
import com.company.permit.framework.web.ServiceException;
import com.company.permit.system.config.service.SysConfigService;
import com.company.permit.system.role.entity.SysRole;
import com.company.permit.system.role.mapper.SysRoleMapper;
import com.company.permit.system.user.convert.UserConvert;
import com.company.permit.system.user.dto.BatchRolesDTO;
import com.company.permit.system.user.dto.BatchStatusDTO;
import com.company.permit.system.user.dto.UserCreateDTO;
import com.company.permit.system.user.dto.UserQueryDTO;
import com.company.permit.system.user.dto.UserRoleDTO;
import com.company.permit.system.user.dto.UserStatusDTO;
import com.company.permit.system.user.dto.UserUpdateDTO;
import com.company.permit.system.user.entity.SysUser;
import com.company.permit.system.user.entity.SysUserRole;
import com.company.permit.system.user.mapper.SysUserMapper;
import com.company.permit.system.user.mapper.SysUserRoleMapper;
import com.company.permit.system.user.vo.UserBO;
import com.company.permit.system.user.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;
    private final DataScopeService dataScopeService;
    private final SysConfigService configService;
    private final PasswordEncoder passwordEncoder;
    private final PermissionCacheService permissionCacheService;
    private final OnlineUserCacheService onlineUserCacheService;

    @Override
    public PageResult<UserVO> selectUserPage(UserQueryDTO query) {
        IPage<UserBO> page = userMapper.selectUserPage(new Page<>(query.safePageNum(), query.safePageSize()), query);
        return new PageResult<>(page.getTotal(), UserConvert.toVoList(page.getRecords()));
    }

    @Override
    public UserVO detail(Long userId) {
        dataScopeService.checkUserInScope(userId);
        SysUser user = userMapper.selectUserByIdUnscoped(userId);
        if (user == null) {
            throw new ServiceException(ErrorCode.B01003);
        }
        UserBO bo = new UserBO();
        bo.setUserId(user.getUserId());
        bo.setUsername(user.getUsername());
        bo.setNickname(user.getNickname());
        bo.setPhone(user.getPhone());
        bo.setEmail(user.getEmail());
        bo.setDeptId(user.getDeptId());
        bo.setStatus(user.getStatus());
        bo.setPwdResetFlag(user.getPwdResetFlag());
        bo.setLockStatus(user.getLockStatus());
        bo.setCreateTime(user.getCreateTime());
        bo.setVersion(user.getVersion());
        bo.setRemark(user.getRemark());
        UserVO vo = UserConvert.toVo(bo);
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setRoleIds(userMapper.selectRoleIdsByUserId(userId));
        vo.setRoleKeys(userMapper.selectRoleKeysByUserId(userId));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createUser(UserCreateDTO dto) {
        dataScopeService.checkDeptInScope(dto.getDeptId());
        if (userMapper.selectCountByUsername(dto.getUsername(), null) > 0) {
            throw new ServiceException(ErrorCode.B01001);
        }
        if (StringUtils.hasText(dto.getPhone()) && userMapper.selectCountByPhone(dto.getPhone(), null) > 0) {
            throw new ServiceException(ErrorCode.B01002);
        }
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setNickname(dto.getNickname());
        user.setPhone(dto.getPhone() == null ? "" : dto.getPhone());
        user.setEmail(dto.getEmail() == null ? "" : dto.getEmail());
        user.setDeptId(dto.getDeptId());
        user.setStatus(Constants.STATUS_NORMAL);
        user.setPwdResetFlag(Constants.FLAG_YES);
        user.setLockStatus(Constants.FLAG_NO);
        String init = configService.getValue(Constants.CFG_INIT_PASSWORD);
        user.setPassword(passwordEncoder.encode(init));
        user.setRemark(dto.getRemark());
        userMapper.insert(user);
        replaceRoles(user.getUserId(), dto.getRoleIds(), false);
        return user.getUserId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(UserUpdateDTO dto) {
        dataScopeService.checkUserInScope(dto.getUserId());
        protectAdminAndSelf(dto.getUserId(), false);
        dataScopeService.checkDeptInScope(dto.getDeptId());
        SysUser db = userMapper.selectUserByIdUnscoped(dto.getUserId());
        if (db == null) {
            throw new ServiceException(ErrorCode.B01003);
        }
        if (StringUtils.hasText(dto.getPhone()) && userMapper.selectCountByPhone(dto.getPhone(), dto.getUserId()) > 0) {
            throw new ServiceException(ErrorCode.B01002);
        }
        db.setNickname(dto.getNickname());
        db.setPhone(dto.getPhone() == null ? "" : dto.getPhone());
        db.setEmail(dto.getEmail() == null ? "" : dto.getEmail());
        db.setDeptId(dto.getDeptId());
        db.setRemark(dto.getRemark());
        if (dto.getVersion() != null) {
            db.setVersion(dto.getVersion());
        }
        int rows = userMapper.updateById(db);
        if (rows == 0) {
            throw new ServiceException(ErrorCode.C01002);
        }
        replaceRoles(dto.getUserId(), dto.getRoleIds(), true);
        permissionCacheService.evict(dto.getUserId());
        dataScopeService.evict(dto.getUserId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeUser(Long userId) {
        dataScopeService.checkUserInScope(userId);
        protectAdminAndSelf(userId, true);
        userMapper.deleteById(userId);
        kick(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(UserStatusDTO dto) {
        dataScopeService.checkUserInScope(dto.getUserId());
        protectAdminAndSelf(dto.getUserId(), true);
        SysUser user = userMapper.selectUserByIdUnscoped(dto.getUserId());
        if (user == null) {
            throw new ServiceException(ErrorCode.B01003);
        }
        user.setStatus(dto.getStatus());
        userMapper.updateById(user);
        if (Constants.STATUS_DISABLE.equals(dto.getStatus())) {
            kick(dto.getUserId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPwd(Long userId) {
        dataScopeService.checkUserInScope(userId);
        protectAdminAndSelf(userId, false);
        SysUser user = userMapper.selectUserByIdUnscoped(userId);
        if (user == null) {
            throw new ServiceException(ErrorCode.B01003);
        }
        String init = configService.getValue(Constants.CFG_INIT_PASSWORD);
        user.setPassword(passwordEncoder.encode(init));
        user.setPwdResetFlag(Constants.FLAG_YES);
        user.setPwdUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        kick(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(UserRoleDTO dto) {
        dataScopeService.checkUserInScope(dto.getUserId());
        protectAdminAndSelf(dto.getUserId(), false);
        replaceRoles(dto.getUserId(), dto.getRoleIds(), true);
        permissionCacheService.evict(dto.getUserId());
        dataScopeService.evict(dto.getUserId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchStatus(BatchStatusDTO dto) {
        int count = 0;
        for (Long userId : dto.getUserIds()) {
            if (skipProtected(userId)) {
                continue;
            }
            try {
                UserStatusDTO statusDTO = new UserStatusDTO();
                statusDTO.setUserId(userId);
                statusDTO.setStatus(dto.getStatus());
                changeStatus(statusDTO);
                count++;
            } catch (ServiceException ignored) {
                // 跳过范围外或受保护账号
            }
        }
        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchRoles(BatchRolesDTO dto) {
        int count = 0;
        for (Long userId : dto.getUserIds()) {
            if (skipProtected(userId)) {
                continue;
            }
            try {
                dataScopeService.checkUserInScope(userId);
                appendRoles(userId, dto.getRoleIds());
                permissionCacheService.evict(userId);
                dataScopeService.evict(userId);
                count++;
            } catch (ServiceException ignored) {
            }
        }
        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(List<Long> userIds) {
        int count = 0;
        for (Long userId : userIds) {
            if (skipProtected(userId)) {
                continue;
            }
            try {
                removeUser(userId);
                count++;
            } catch (ServiceException ignored) {
            }
        }
        return count;
    }

    private void replaceRoles(Long userId, List<Long> roleIds, boolean checkAdminRole) {
        if (checkAdminRole && userId == SecurityUtils.ADMIN_USER_ID) {
            boolean keepAdmin = roleIds != null && roleIds.contains(1L);
            if (!keepAdmin) {
                throw new ServiceException(ErrorCode.B02008);
            }
        }
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        if (roleIds == null) {
            return;
        }
        for (Long roleId : roleIds) {
            validateAssignable(roleId);
            SysUserRole rel = new SysUserRole();
            rel.setUserId(userId);
            rel.setRoleId(roleId);
            userRoleMapper.insert(rel);
        }
    }

    private void appendRoles(Long userId, List<Long> roleIds) {
        List<Long> exists = userMapper.selectRoleIdsByUserId(userId);
        for (Long roleId : roleIds) {
            if (exists != null && exists.contains(roleId)) {
                continue;
            }
            validateAssignable(roleId);
            SysUserRole rel = new SysUserRole();
            rel.setUserId(userId);
            rel.setRoleId(roleId);
            userRoleMapper.insert(rel);
        }
    }

    private void validateAssignable(Long roleId) {
        SysRole role = roleMapper.selectById(roleId);
        if (role == null || !Constants.STATUS_NORMAL.equals(role.getStatus())) {
            throw new ServiceException(ErrorCode.C01003, "角色不存在或已停用");
        }
        if (Constants.BUILTIN_ROLE_ADMIN.equals(role.getRoleKey()) && !SecurityUtils.hasWildcardPermission()) {
            throw new ServiceException(ErrorCode.A01003, "不能分配超级管理员角色");
        }
    }

    private void protectAdminAndSelf(Long userId, boolean destructive) {
        if (userId != null && userId == SecurityUtils.ADMIN_USER_ID && destructive) {
            throw new ServiceException(ErrorCode.B01006);
        }
        Long loginId = SecurityUtils.getLoginId();
        if (destructive && loginId != null && loginId.equals(userId)) {
            throw new ServiceException(ErrorCode.B01004);
        }
    }

    private boolean skipProtected(Long userId) {
        Long loginId = SecurityUtils.getLoginId();
        return userId != null && (userId == SecurityUtils.ADMIN_USER_ID || userId.equals(loginId));
    }

    private void kick(Long userId) {
        permissionCacheService.evict(userId);
        onlineUserCacheService.remove(userId);
        try {
            StpUtil.kickout(userId);
        } catch (Exception ignored) {
        }
    }
}
