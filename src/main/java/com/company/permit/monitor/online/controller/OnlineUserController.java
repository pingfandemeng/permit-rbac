package com.company.permit.monitor.online.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.company.permit.framework.log.Log;
import com.company.permit.framework.log.OperType;
import com.company.permit.framework.web.PageResult;
import com.company.permit.framework.web.R;
import com.company.permit.monitor.online.service.OnlineUserService;
import com.company.permit.monitor.online.vo.OnlineUserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/monitor/online")
@RequiredArgsConstructor
public class OnlineUserController {

    private final OnlineUserService onlineUserService;

    @GetMapping
    @SaCheckPermission("monitor:online:list")
    public R<PageResult<OnlineUserVO>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                            @RequestParam(defaultValue = "10") Integer pageSize) {
        return R.ok(onlineUserService.page(pageNum, pageSize));
    }

    @DeleteMapping("/{userId}")
    @SaCheckPermission("monitor:online:forceLogout")
    @Log(title = "在线用户", operType = OperType.DELETE)
    public R<Void> kick(@PathVariable Long userId) {
        onlineUserService.forceLogout(userId);
        return R.ok();
    }
}
