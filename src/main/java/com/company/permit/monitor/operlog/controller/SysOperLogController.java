package com.company.permit.monitor.operlog.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.company.permit.framework.log.Log;
import com.company.permit.framework.log.OperType;
import com.company.permit.framework.web.PageResult;
import com.company.permit.framework.web.R;
import com.company.permit.monitor.operlog.dto.OperLogQueryDTO;
import com.company.permit.monitor.operlog.service.SysOperLogService;
import com.company.permit.monitor.operlog.vo.OperLogVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/monitor/operlog")
@RequiredArgsConstructor
public class SysOperLogController {

    private final SysOperLogService operLogService;

    @GetMapping
    @SaCheckPermission("monitor:operlog:list")
    public R<PageResult<OperLogVO>> page(OperLogQueryDTO query) {
        return R.ok(operLogService.page(query));
    }

    @DeleteMapping
    @SaCheckPermission("monitor:operlog:remove")
    @Log(title = "操作日志", operType = OperType.DELETE)
    public R<Void> clean() {
        operLogService.clean();
        return R.ok();
    }
}
