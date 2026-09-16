package com.company.permit.system.role.service;

import com.company.permit.framework.web.PageResult;
import com.company.permit.system.role.dto.RoleCreateDTO;
import com.company.permit.system.role.dto.RoleDataScopeDTO;
import com.company.permit.system.role.dto.RoleMenuDTO;
import com.company.permit.system.role.dto.RoleQueryDTO;
import com.company.permit.system.role.dto.RoleUpdateDTO;
import com.company.permit.system.role.vo.RoleVO;

import java.util.List;

public interface SysRoleService {
    PageResult<RoleVO> selectRolePage(RoleQueryDTO query);

    List<RoleVO> options();

    RoleVO detail(Long roleId);

    Long createRole(RoleCreateDTO dto);

    void updateRole(RoleUpdateDTO dto);

    void removeRole(Long roleId);

    void assignMenus(Long roleId, RoleMenuDTO dto);

    void assignDataScope(Long roleId, RoleDataScopeDTO dto);
}
