package com.company.permit.system.dept.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.company.permit.framework.log.Log;
import com.company.permit.framework.log.OperType;
import com.company.permit.framework.security.RepeatSubmit;
import com.company.permit.framework.web.R;
import com.company.permit.system.dept.dto.DeptSaveDTO;
import com.company.permit.system.dept.service.SysDeptService;
import com.company.permit.system.dept.vo.DeptVO;
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
@RequestMapping("/api/system/dept")
@RequiredArgsConstructor
@Validated
public class SysDeptController {

    private final SysDeptService deptService;

    @GetMapping
    @SaCheckPermission("system:dept:list")
    public R<List<DeptVO>> list() {
        return R.ok(deptService.tree(null));
    }

    @GetMapping("/tree")
    public R<List<DeptVO>> tree() {
        return R.ok(deptService.treeForScope());
    }

    @GetMapping("/{deptId}")
    @SaCheckPermission("system:dept:query")
    public R<DeptVO> detail(@PathVariable Long deptId) {
        return R.ok(deptService.detail(deptId));
    }

    @PostMapping
    @SaCheckPermission("system:dept:add")
    @Log(title = "部门管理", operType = OperType.INSERT)
    @RepeatSubmit
    public R<Long> add(@Validated @RequestBody DeptSaveDTO dto) {
        return R.ok(deptService.createDept(dto));
    }

    @PutMapping
    @SaCheckPermission("system:dept:edit")
    @Log(title = "部门管理", operType = OperType.UPDATE)
    @RepeatSubmit
    public R<Void> edit(@Validated @RequestBody DeptSaveDTO dto) {
        deptService.updateDept(dto);
        return R.ok();
    }

    @DeleteMapping("/{deptId}")
    @SaCheckPermission("system:dept:remove")
    @Log(title = "部门管理", operType = OperType.DELETE)
    @RepeatSubmit
    public R<Void> remove(@PathVariable Long deptId) {
        deptService.removeDept(deptId);
        return R.ok();
    }
}
