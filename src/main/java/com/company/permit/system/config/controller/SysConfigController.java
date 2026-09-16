package com.company.permit.system.config.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.company.permit.framework.log.Log;
import com.company.permit.framework.log.OperType;
import com.company.permit.framework.security.RepeatSubmit;
import com.company.permit.framework.web.PageResult;
import com.company.permit.framework.web.R;
import com.company.permit.system.config.dto.ConfigUpdateDTO;
import com.company.permit.system.config.service.SysConfigService;
import com.company.permit.system.config.vo.ConfigVO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system/config")
@RequiredArgsConstructor
@Validated
public class SysConfigController {

    private final SysConfigService configService;

    @GetMapping
    @SaCheckPermission("system:config:list")
    public R<PageResult<ConfigVO>> page(@RequestParam(required = false) String configName,
                                        @RequestParam(required = false) String configKey,
                                        @RequestParam(required = false) Integer pageNum,
                                        @RequestParam(required = false) Integer pageSize) {
        return R.ok(configService.selectConfigPage(configName, configKey, pageNum, pageSize));
    }

    @PutMapping
    @SaCheckPermission("system:config:edit")
    @Log(title = "参数配置", operType = OperType.UPDATE)
    @RepeatSubmit
    public R<Void> edit(@Validated @RequestBody ConfigUpdateDTO dto) {
        configService.updateConfig(dto);
        return R.ok();
    }
}
