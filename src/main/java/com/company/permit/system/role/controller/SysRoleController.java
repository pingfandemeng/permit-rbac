package com.company.permit.system.role.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.company.permit.framework.log.Log;
import com.company.permit.framework.log.OperType;
import com.company.permit.framework.security.RepeatSubmit;
import com.company.permit.framework.web.PageResult;
import com.company.permit.framework.web.R;
import com.company.permit.system.role.dto.RoleCreateDTO;
import com.company.permit.system.role.dto.RoleDataScopeDTO;
import com.company.permit.system.role.dto.RoleMenuDTO;
import com.company.permit.system.role.dto.RoleQueryDTO;
import com.company.permit.system.role.dto.RoleUpdateDTO;
import com.company.permit.system.role.service.SysRoleService;
import com.company.permit.system.role.vo.RoleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/system/role")
@RequiredArgsConstructor
@Validated
public class SysRoleController {

    private final SysRoleService roleService;

    @GetMapping
    @SaCheckPermission("system:role:list")
    public R<PageResult<RoleVO>> page(RoleQueryDTO query) {
        return R.ok(roleService.selectRolePage(query));
    }

    @GetMapping("/options")
    public R<List<RoleVO>> options() {
        return R.ok(roleService.options());
    }

    @GetMapping("/{roleId}")
    @SaCheckPermission("system:role:query")
    public R<RoleVO> detail(@PathVariable Long roleId) {
        return R.ok(roleService.detail(roleId));
    }

    @PostMapping
    @SaCheckPermission("system:role:add")
    @Log(title = "角色管理", operType = OperType.INSERT)
    @RepeatSubmit
    public R<Long> add(@Validated @RequestBody RoleCreateDTO dto) {
        return R.ok(roleService.createRole(dto));
    }

    @PutMapping
    @SaCheckPermission("system:role:edit")
    @Log(title = "角色管理", operType = OperType.UPDATE)
    @RepeatSubmit
    public R<Void> edit(@Validated @RequestBody RoleUpdateDTO dto) {
        roleService.updateRole(dto);
        return R.ok();
    }

    @DeleteMapping("/{roleId}")
    @SaCheckPermission("system:role:remove")
    @Log(title = "角色管理", operType = OperType.DELETE)
    @RepeatSubmit
    public R<Void> remove(@PathVariable Long roleId) {
        roleService.removeRole(roleId);
        return R.ok();
    }

    @PutMapping("/{roleId}/menus")
    @SaCheckPermission("system:role:edit")
    @Log(title = "角色分配菜单", operType = OperType.UPDATE)
    @RepeatSubmit
    public R<Void> menus(@PathVariable Long roleId, @Validated @RequestBody RoleMenuDTO dto) {
        roleService.assignMenus(roleId, dto);
        return R.ok();
    }

    @PutMapping("/{roleId}/dataScope")
    @SaCheckPermission("system:role:edit")
    @Log(title = "角色数据权限", operType = OperType.UPDATE)
    @RepeatSubmit
    public R<Void> dataScope(@PathVariable Long roleId, @Validated @RequestBody RoleDataScopeDTO dto) {
        roleService.assignDataScope(roleId, dto);
        return R.ok();
    }
}
