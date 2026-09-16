package com.company.permit.monitor.loginlog.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.company.permit.framework.log.Log;
import com.company.permit.framework.log.OperType;
import com.company.permit.framework.web.PageResult;
import com.company.permit.framework.web.R;
import com.company.permit.monitor.loginlog.dto.LoginLogQueryDTO;
import com.company.permit.monitor.loginlog.service.SysLoginLogService;
import com.company.permit.monitor.loginlog.vo.LoginLogVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/monitor/loginlog")
@RequiredArgsConstructor
public class SysLoginLogController {

    private final SysLoginLogService loginLogService;

    @GetMapping
    @SaCheckPermission("monitor:loginlog:list")
    public R<PageResult<LoginLogVO>> page(LoginLogQueryDTO query) {
        return R.ok(loginLogService.page(query));
    }

    @DeleteMapping
    @SaCheckPermission("monitor:loginlog:remove")
    @Log(title = "登录日志", operType = OperType.DELETE)
    public R<Void> clean() {
        loginLogService.clean();
        return R.ok();
    }
}
