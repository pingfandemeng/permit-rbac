package com.company.permit.system.role.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.permit.framework.cache.PermissionCacheService;
import com.company.permit.framework.dataperm.DataScopeMerger;
import com.company.permit.framework.dataperm.DataScopeService;
import com.company.permit.framework.util.SecurityUtils;
import com.company.permit.framework.web.Constants;
import com.company.permit.framework.web.ErrorCode;
import com.company.permit.framework.web.PageResult;
import com.company.permit.framework.web.ServiceException;
import com.company.permit.system.role.dto.RoleCreateDTO;
import com.company.permit.system.role.dto.RoleDataScopeDTO;
import com.company.permit.system.role.dto.RoleMenuDTO;
import com.company.permit.system.role.dto.RoleQueryDTO;
import com.company.permit.system.role.dto.RoleUpdateDTO;
import com.company.permit.system.role.entity.SysRole;
import com.company.permit.system.role.entity.SysRoleDept;
import com.company.permit.system.role.entity.SysRoleMenu;
import com.company.permit.system.role.mapper.SysRoleDeptMapper;
import com.company.permit.system.role.mapper.SysRoleMapper;
import com.company.permit.system.role.mapper.SysRoleMenuMapper;
import com.company.permit.system.role.vo.RoleVO;
import com.company.permit.system.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl implements SysRoleService {

    private static final Set<String> BUILTIN = new HashSet<>(Arrays.asList(
            Constants.BUILTIN_ROLE_ADMIN, Constants.BUILTIN_ROLE_SYS,
            Constants.BUILTIN_ROLE_DEPT, Constants.BUILTIN_ROLE_COMMON));

    private final SysRoleMapper roleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysRoleDeptMapper roleDeptMapper;
    private final SysUserMapper userMapper;
    private final DataScopeService dataScopeService;
    private final PermissionCacheService permissionCacheService;

    @Override
    public PageResult<RoleVO> selectRolePage(RoleQueryDTO query) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getRoleName()), SysRole::getRoleName, query.getRoleName())
                .like(StringUtils.hasText(query.getRoleKey()), SysRole::getRoleKey, query.getRoleKey())
                .eq(StringUtils.hasText(query.getStatus()), SysRole::getStatus, query.getStatus())
                .orderByAsc(SysRole::getSort, SysRole::getRoleId);
        Page<SysRole> page = roleMapper.selectPage(new Page<>(query.safePageNum(), query.safePageSize()), wrapper);
        List<RoleVO> rows = new ArrayList<>();
        for (SysRole role : page.getRecords()) {
            rows.add(toVo(role, false));
        }
        return new PageResult<>(page.getTotal(), rows);
    }

    @Override
    public List<RoleVO> options() {
        List<SysRole> roles = roleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getStatus, Constants.STATUS_NORMAL)
                .orderByAsc(SysRole::getSort));
        List<RoleVO> list = new ArrayList<>();
        for (SysRole role : roles) {
            if (Constants.BUILTIN_ROLE_ADMIN.equals(role.getRoleKey()) && !SecurityUtils.hasWildcardPermission()) {
                continue;
            }
            list.add(toVo(role, false));
        }
        return list;
    }

    @Override
    public RoleVO detail(Long roleId) {
        SysRole role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new ServiceException(ErrorCode.C01003, "角色不存在");
        }
        return toVo(role, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRole(RoleCreateDTO dto) {
        Long exists = roleMapper.selectCount(new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleKey, dto.getRoleKey()));
        if (exists != null && exists > 0) {
            throw new ServiceException(ErrorCode.B02001);
        }
        SysRole role = new SysRole();
        role.setRoleName(dto.getRoleName());
        role.setRoleKey(dto.getRoleKey());
        role.setSort(dto.getSort() == null ? 0 : dto.getSort());
        role.setStatus(dto.getStatus() == null ? Constants.STATUS_NORMAL : dto.getStatus());
        role.setRemark(dto.getRemark());
        applyDeptAdminAndScope(role, dto.getDeptAdminFlag(), dto.getDataScope(), true);
        roleMapper.insert(role);
        return role.getRoleId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(RoleUpdateDTO dto) {
        SysRole db = roleMapper.selectById(dto.getRoleId());
        if (db == null) {
            throw new ServiceException(ErrorCode.C01003, "角色不存在");
        }
        db.setRoleName(dto.getRoleName());
        db.setSort(dto.getSort() == null ? db.getSort() : dto.getSort());
        db.setStatus(dto.getStatus() == null ? db.getStatus() : dto.getStatus());
        db.setRemark(dto.getRemark());
        if (dto.getVersion() != null) {
            db.setVersion(dto.getVersion());
        }
        applyDeptAdminAndScope(db, dto.getDeptAdminFlag(), dto.getDataScope(), false);
        int rows = roleMapper.updateById(db);
        if (rows == 0) {
            throw new ServiceException(ErrorCode.C01002);
        }
        dataScopeService.evictByRoleId(db.getRoleId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeRole(Long roleId) {
        SysRole role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new ServiceException(ErrorCode.C01003, "角色不存在");
        }
        if (BUILTIN.contains(role.getRoleKey())) {
            throw new ServiceException(ErrorCode.B02003);
        }
        List<Long> users = userMapper.selectUserIdsByRoleId(roleId);
        if (users != null && !users.isEmpty()) {
            throw new ServiceException(ErrorCode.B02002);
        }
        roleMapper.deleteById(roleId);
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
        roleDeptMapper.delete(new LambdaQueryWrapper<SysRoleDept>().eq(SysRoleDept::getRoleId, roleId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignMenus(Long roleId, RoleMenuDTO dto) {
        SysRole role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new ServiceException(ErrorCode.C01003, "角色不存在");
        }
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
        for (Long menuId : dto.getMenuIds()) {
            SysRoleMenu rm = new SysRoleMenu();
            rm.setRoleId(roleId);
            rm.setMenuId(menuId);
            roleMenuMapper.insert(rm);
        }
        List<Long> userIds = userMapper.selectUserIdsByRoleId(roleId);
        for (Long userId : userIds) {
            permissionCacheService.evictPermissions(userId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignDataScope(Long roleId, RoleDataScopeDTO dto) {
        SysRole role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new ServiceException(ErrorCode.C01003, "角色不存在");
        }
        if (Constants.FLAG_YES.equals(role.getDeptAdminFlag()) && !DataScopeMerger.DEPT_AND_CHILD.equals(dto.getDataScope())) {
            throw new ServiceException(ErrorCode.B02007);
        }
        applyScopeValue(role, dto.getDataScope());
        roleMapper.updateById(role);
        roleDeptMapper.delete(new LambdaQueryWrapper<SysRoleDept>().eq(SysRoleDept::getRoleId, roleId));
        if (DataScopeMerger.CUSTOM.equals(dto.getDataScope())) {
            if (dto.getDeptIds() == null || dto.getDeptIds().isEmpty()) {
                throw new ServiceException(ErrorCode.B02004);
            }
            for (Long deptId : dto.getDeptIds()) {
                SysRoleDept rd = new SysRoleDept();
                rd.setRoleId(roleId);
                rd.setDeptId(deptId);
                roleDeptMapper.insert(rd);
            }
        }
        dataScopeService.evictByRoleId(roleId);
    }

    private void applyDeptAdminAndScope(SysRole role, String deptAdminFlag, String dataScope, boolean creating) {
        if (deptAdminFlag != null && !deptAdminFlag.equals(role.getDeptAdminFlag())) {
            if (!SecurityUtils.hasWildcardPermission()) {
                throw new ServiceException(ErrorCode.B02006);
            }
            role.setDeptAdminFlag(deptAdminFlag);
        } else if (creating) {
            role.setDeptAdminFlag(deptAdminFlag == null ? Constants.FLAG_NO : deptAdminFlag);
        }
        if (Constants.FLAG_YES.equals(role.getDeptAdminFlag())) {
            role.setDataScope(DataScopeMerger.DEPT_AND_CHILD);
            return;
        }
        if (dataScope != null) {
            applyScopeValue(role, dataScope);
        } else if (creating) {
            role.setDataScope(DataScopeMerger.DEPT_AND_CHILD);
        }
    }

    private void applyScopeValue(SysRole role, String dataScope) {
        if (DataScopeMerger.ALL.equals(dataScope) && !SecurityUtils.hasWildcardPermission()) {
            throw new ServiceException(ErrorCode.B02005);
        }
        role.setDataScope(dataScope);
    }

    private RoleVO toVo(SysRole role, boolean withRel) {
        RoleVO vo = new RoleVO();
        vo.setRoleId(role.getRoleId());
        vo.setRoleName(role.getRoleName());
        vo.setRoleKey(role.getRoleKey());
        vo.setDataScope(role.getDataScope());
        vo.setDeptAdminFlag(role.getDeptAdminFlag());
        vo.setStatus(role.getStatus());
        vo.setSort(role.getSort());
        vo.setVersion(role.getVersion());
        vo.setRemark(role.getRemark());
        vo.setCreateTime(role.getCreateTime());
        if (withRel) {
            vo.setMenuIds(roleMenuMapper.selectMenuIdsByRoleId(role.getRoleId()));
            vo.setDeptIds(roleDeptMapper.selectDeptIdsByRoleId(role.getRoleId()));
        }
        return vo;
    }
}
