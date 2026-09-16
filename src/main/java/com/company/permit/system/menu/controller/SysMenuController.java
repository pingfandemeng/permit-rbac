package com.company.permit.system.menu.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.company.permit.framework.log.Log;
import com.company.permit.framework.log.OperType;
import com.company.permit.framework.security.RepeatSubmit;
import com.company.permit.framework.web.R;
import com.company.permit.system.menu.dto.MenuSaveDTO;
import com.company.permit.system.menu.service.SysMenuService;
import com.company.permit.system.menu.vo.MenuVO;
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
@RequestMapping("/api/system/menu")
@RequiredArgsConstructor
@Validated
public class SysMenuController {

    private final SysMenuService menuService;

    @GetMapping
    @SaCheckPermission("system:menu:list")
    public R<List<MenuVO>> list() {
        return R.ok(menuService.tree());
    }

    @GetMapping("/tree")
    public R<List<MenuVO>> tree() {
        return R.ok(menuService.treeAll());
    }

    @GetMapping("/{menuId}")
    @SaCheckPermission("system:menu:query")
    public R<MenuVO> detail(@PathVariable Long menuId) {
        return R.ok(menuService.detail(menuId));
    }

    @PostMapping
    @SaCheckPermission("system:menu:add")
    @Log(title = "菜单管理", operType = OperType.INSERT)
    @RepeatSubmit
    public R<Long> add(@Validated @RequestBody MenuSaveDTO dto) {
        return R.ok(menuService.createMenu(dto));
    }

    @PutMapping
    @SaCheckPermission("system:menu:edit")
    @Log(title = "菜单管理", operType = OperType.UPDATE)
    @RepeatSubmit
    public R<Void> edit(@Validated @RequestBody MenuSaveDTO dto) {
        menuService.updateMenu(dto);
        return R.ok();
    }

    @DeleteMapping("/{menuId}")
    @SaCheckPermission("system:menu:remove")
    @Log(title = "菜单管理", operType = OperType.DELETE)
    @RepeatSubmit
    public R<Void> remove(@PathVariable Long menuId) {
        menuService.removeMenu(menuId);
        return R.ok();
    }
}
