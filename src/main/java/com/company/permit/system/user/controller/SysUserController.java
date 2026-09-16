package com.company.permit.system.user.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.company.permit.framework.log.Log;
import com.company.permit.framework.log.OperType;
import com.company.permit.framework.security.RepeatSubmit;
import com.company.permit.framework.web.R;
import com.company.permit.framework.web.PageResult;
import com.company.permit.system.user.dto.BatchRolesDTO;
import com.company.permit.system.user.dto.BatchStatusDTO;
import com.company.permit.system.user.dto.UserCreateDTO;
import com.company.permit.system.user.dto.UserQueryDTO;
import com.company.permit.system.user.dto.UserRoleDTO;
import com.company.permit.system.user.dto.UserStatusDTO;
import com.company.permit.system.user.dto.UserUpdateDTO;
import com.company.permit.system.user.service.SysUserService;
import com.company.permit.system.user.vo.UserVO;
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
@RequestMapping("/api/system/user")
@RequiredArgsConstructor
@Validated
public class SysUserController {

    private final SysUserService userService;
    // TODO: Excel 导入/导出（EasyExcel 全量校验、5 万行上限）本期未做，权限标识已入库

    @GetMapping
    @SaCheckPermission("system:user:list")
    public R<PageResult<UserVO>> page(UserQueryDTO query) {
        return R.ok(userService.selectUserPage(query));
    }

    @GetMapping("/{userId}")
    @SaCheckPermission("system:user:query")
    public R<UserVO> detail(@PathVariable Long userId) {
        return R.ok(userService.detail(userId));
    }

    @PostMapping
    @SaCheckPermission("system:user:add")
    @Log(title = "用户管理", operType = OperType.INSERT)
    @RepeatSubmit
    public R<Long> add(@Validated @RequestBody UserCreateDTO dto) {
        return R.ok(userService.createUser(dto));
    }

    @PutMapping
    @SaCheckPermission("system:user:edit")
    @Log(title = "用户管理", operType = OperType.UPDATE)
    @RepeatSubmit
    public R<Void> edit(@Validated @RequestBody UserUpdateDTO dto) {
        userService.updateUser(dto);
        return R.ok();
    }

    @DeleteMapping("/{userId}")
    @SaCheckPermission("system:user:remove")
    @Log(title = "用户管理", operType = OperType.DELETE)
    @RepeatSubmit
    public R<Void> remove(@PathVariable Long userId) {
        userService.removeUser(userId);
        return R.ok();
    }

    @PutMapping("/{userId}/status")
    @SaCheckPermission("system:user:edit")
    @Log(title = "用户启停用", operType = OperType.UPDATE)
    @RepeatSubmit
    public R<Void> status(@PathVariable Long userId, @Validated @RequestBody UserStatusDTO dto) {
        dto.setUserId(userId);
        userService.changeStatus(dto);
        return R.ok();
    }

    @PutMapping("/resetPwd")
    @SaCheckPermission("system:user:resetPwd")
    @Log(title = "重置密码", operType = OperType.UPDATE)
    @RepeatSubmit
    public R<Void> resetPwd(@Validated @RequestBody com.company.permit.system.user.dto.ResetPwdDTO dto) {
        userService.resetPwd(dto.getUserId());
        return R.ok();
    }

    @PutMapping("/{userId}/roles")
    @SaCheckPermission("system:user:edit")
    @Log(title = "分配角色", operType = OperType.UPDATE)
    @RepeatSubmit
    public R<Void> roles(@PathVariable Long userId, @Validated @RequestBody UserRoleDTO dto) {
        dto.setUserId(userId);
        userService.assignRoles(dto);
        return R.ok();
    }

    @PostMapping("/batchDelete")
    @SaCheckPermission("system:user:remove")
    @Log(title = "批量删除用户", operType = OperType.DELETE)
    @RepeatSubmit
    public R<Integer> batchDelete(@RequestBody List<Long> userIds) {
        return R.ok(userService.batchDelete(userIds));
    }

    @PutMapping("/batchStatus")
    @SaCheckPermission("system:user:edit")
    @Log(title = "批量启停用户", operType = OperType.UPDATE)
    @RepeatSubmit
    public R<Integer> batchStatus(@Validated @RequestBody BatchStatusDTO dto) {
        return R.ok(userService.batchStatus(dto));
    }

    @PutMapping("/batchRoles")
    @SaCheckPermission("system:user:edit")
    @Log(title = "批量分配角色", operType = OperType.UPDATE)
    @RepeatSubmit
    public R<Integer> batchRoles(@Validated @RequestBody BatchRolesDTO dto) {
        return R.ok(userService.batchRoles(dto));
    }
}
