package com.company.permit.monitor.loginlog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.permit.framework.web.Constants;
import com.company.permit.framework.web.PageResult;
import com.company.permit.monitor.loginlog.dto.LoginLogQueryDTO;
import com.company.permit.monitor.loginlog.entity.SysLoginLog;
import com.company.permit.monitor.loginlog.mapper.SysLoginLogMapper;
import com.company.permit.monitor.loginlog.vo.LoginLogVO;
import com.company.permit.system.config.service.SysConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SysLoginLogServiceImpl implements SysLoginLogService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SysLoginLogMapper loginLogMapper;
    private final SysConfigService configService;

    @Override
    public PageResult<LoginLogVO> page(LoginLogQueryDTO query) {
        LambdaQueryWrapper<SysLoginLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getUsername()), SysLoginLog::getUsername, query.getUsername())
                .eq(StringUtils.hasText(query.getStatus()), SysLoginLog::getStatus, query.getStatus())
                .ge(StringUtils.hasText(query.getBeginTime()), SysLoginLog::getLoginTime, parse(query.getBeginTime()))
                .le(StringUtils.hasText(query.getEndTime()), SysLoginLog::getLoginTime, parse(query.getEndTime()))
                .orderByDesc(SysLoginLog::getLoginId);
        Page<SysLoginLog> page = loginLogMapper.selectPage(new Page<>(query.safePageNum(), query.safePageSize()), wrapper);
        List<LoginLogVO> rows = new ArrayList<>();
        for (SysLoginLog e : page.getRecords()) {
            LoginLogVO vo = new LoginLogVO();
            vo.setLoginId(e.getLoginId());
            vo.setUsername(e.getUsername());
            vo.setLoginIp(e.getLoginIp());
            vo.setBrowser(e.getBrowser());
            vo.setOs(e.getOs());
            vo.setStatus(e.getStatus());
            vo.setMsg(e.getMsg());
            vo.setLoginTime(e.getLoginTime());
            rows.add(vo);
        }
        return new PageResult<>(page.getTotal(), rows);
    }

    @Override
    public void clean() {
        int days = configService.getInt(Constants.CFG_LOG_DAYS, 180);
        loginLogMapper.delete(new LambdaQueryWrapper<SysLoginLog>()
                .lt(SysLoginLog::getLoginTime, LocalDateTime.now().minusDays(days)));
    }

    private LocalDateTime parse(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        return LocalDateTime.parse(text.length() == 10 ? text + " 00:00:00" : text, FMT);
    }
}
