package com.company.permit.monitor.operlog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.permit.framework.log.OperLogBO;
import com.company.permit.framework.web.Constants;
import com.company.permit.framework.web.PageResult;
import com.company.permit.monitor.operlog.dto.OperLogQueryDTO;
import com.company.permit.monitor.operlog.entity.SysOperLog;
import com.company.permit.monitor.operlog.mapper.SysOperLogMapper;
import com.company.permit.monitor.operlog.vo.OperLogVO;
import com.company.permit.system.config.service.SysConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysOperLogServiceImpl implements SysOperLogService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SysOperLogMapper operLogMapper;
    private final SysConfigService configService;

    @Override
    @Async("operLogExecutor")
    public void writeAsync(OperLogBO bo) {
        try {
            SysOperLog entity = new SysOperLog();
            entity.setTitle(bo.getTitle());
            entity.setOperType(bo.getOperType());
            entity.setMethod(bo.getMethod());
            entity.setOperName(bo.getOperName());
            entity.setOperParam(bo.getOperParam());
            entity.setOperIp(bo.getOperIp());
            entity.setStatus(bo.getStatus());
            entity.setErrorMsg(bo.getErrorMsg());
            entity.setCostTime(bo.getCostTime());
            entity.setTraceId(bo.getTraceId());
            entity.setOperTime(LocalDateTime.now());
            operLogMapper.insert(entity);
        } catch (Exception e) {
            log.warn("异步写入操作日志失败", e);
        }
    }

    @Override
    public PageResult<OperLogVO> page(OperLogQueryDTO query) {
        LambdaQueryWrapper<SysOperLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getOperName()), SysOperLog::getOperName, query.getOperName())
                .like(StringUtils.hasText(query.getTitle()), SysOperLog::getTitle, query.getTitle())
                .eq(StringUtils.hasText(query.getStatus()), SysOperLog::getStatus, query.getStatus())
                .ge(StringUtils.hasText(query.getBeginTime()), SysOperLog::getOperTime, parse(query.getBeginTime()))
                .le(StringUtils.hasText(query.getEndTime()), SysOperLog::getOperTime, parse(query.getEndTime()))
                .orderByDesc(SysOperLog::getOperId);
        Page<SysOperLog> page = operLogMapper.selectPage(new Page<>(query.safePageNum(), query.safePageSize()), wrapper);
        List<OperLogVO> rows = new ArrayList<>();
        for (SysOperLog logEntity : page.getRecords()) {
            rows.add(toVo(logEntity));
        }
        return new PageResult<>(page.getTotal(), rows);
    }

    @Override
    public void clean() {
        int days = configService.getInt(Constants.CFG_LOG_DAYS, 180);
        LocalDateTime threshold = LocalDateTime.now().minusDays(days);
        operLogMapper.delete(new LambdaQueryWrapper<SysOperLog>().lt(SysOperLog::getOperTime, threshold));
    }

    private LocalDateTime parse(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        return LocalDateTime.parse(text.length() == 10 ? text + " 00:00:00" : text, FMT);
    }

    private OperLogVO toVo(SysOperLog e) {
        OperLogVO vo = new OperLogVO();
        vo.setOperId(e.getOperId());
        vo.setTitle(e.getTitle());
        vo.setOperType(e.getOperType());
        vo.setMethod(e.getMethod());
        vo.setOperName(e.getOperName());
        vo.setOperParam(e.getOperParam());
        vo.setOperIp(e.getOperIp());
        vo.setStatus(e.getStatus());
        vo.setErrorMsg(e.getErrorMsg());
        vo.setCostTime(e.getCostTime());
        vo.setTraceId(e.getTraceId());
        vo.setOperTime(e.getOperTime());
        return vo;
    }
}
