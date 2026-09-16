package com.company.permit.system.config.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.permit.framework.cache.CacheKeys;
import com.company.permit.framework.web.Constants;
import com.company.permit.framework.web.ErrorCode;
import com.company.permit.framework.web.PageResult;
import com.company.permit.framework.web.ServiceException;
import com.company.permit.system.config.dto.ConfigUpdateDTO;
import com.company.permit.system.config.entity.SysConfig;
import com.company.permit.system.config.mapper.SysConfigMapper;
import com.company.permit.system.config.vo.ConfigVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class SysConfigServiceImpl implements SysConfigService {

    private final SysConfigMapper configMapper;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public PageResult<ConfigVO> selectConfigPage(String configName, String configKey, Integer pageNum, Integer pageSize) {
        int pn = pageNum == null ? 1 : Math.min(Math.max(pageNum, 1), 10000);
        int ps = pageSize == null ? 10 : Math.min(Math.max(pageSize, 1), 100);
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(configName), SysConfig::getConfigName, configName)
                .like(StringUtils.hasText(configKey), SysConfig::getConfigKey, configKey)
                .orderByAsc(SysConfig::getConfigId);
        Page<SysConfig> page = configMapper.selectPage(new Page<>(pn, ps), wrapper);
        List<ConfigVO> rows = new ArrayList<>();
        for (SysConfig cfg : page.getRecords()) {
            rows.add(toVo(cfg));
        }
        return new PageResult<>(page.getTotal(), rows);
    }

    @Override
    public String getValue(String key) {
        String cacheKey = CacheKeys.config(key);
        String cached = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }
        SysConfig cfg = configMapper.selectOne(new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, key));
        String value = cfg == null ? "" : cfg.getConfigValue();
        stringRedisTemplate.opsForValue().set(cacheKey, value, 30, TimeUnit.MINUTES);
        return value;
    }

    @Override
    public int getInt(String key, int defaultValue) {
        try {
            String v = getValue(key);
            return StringUtils.hasText(v) ? Integer.parseInt(v.trim()) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    public boolean getBool(String key, boolean defaultValue) {
        String v = getValue(key);
        if (!StringUtils.hasText(v)) {
            return defaultValue;
        }
        return "true".equalsIgnoreCase(v) || "1".equals(v);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateConfig(ConfigUpdateDTO dto) {
        SysConfig cfg = configMapper.selectById(dto.getConfigId());
        if (cfg == null) {
            throw new ServiceException(ErrorCode.B05002, "参数不存在");
        }
        validate(cfg.getConfigKey(), dto.getConfigValue());
        cfg.setConfigValue(dto.getConfigValue());
        if (dto.getRemark() != null) {
            cfg.setRemark(dto.getRemark());
        }
        configMapper.updateById(cfg);
        stringRedisTemplate.delete(CacheKeys.config(cfg.getConfigKey()));
    }

    private void validate(String key, String value) {
        if (Constants.CFG_SESSION_TIMEOUT.equals(key) || Constants.CFG_SESSION_ACTIVITY.equals(key)) {
            long timeout = Constants.CFG_SESSION_TIMEOUT.equals(key) ? parseLong(value)
                    : getLong(Constants.CFG_SESSION_TIMEOUT, 604800);
            long activity = Constants.CFG_SESSION_ACTIVITY.equals(key) ? parseLong(value)
                    : getLong(Constants.CFG_SESSION_ACTIVITY, 7200);
            if (Constants.CFG_SESSION_TIMEOUT.equals(key)) {
                timeout = parseLong(value);
            }
            if (Constants.CFG_SESSION_ACTIVITY.equals(key)) {
                activity = parseLong(value);
            }
            if (activity > timeout) {
                throw new ServiceException(ErrorCode.B05002, "无操作下线时长不得大于 Token 绝对有效期");
            }
            if (timeout < 1 || activity < 1) {
                throw new ServiceException(ErrorCode.B05002, "会话时长须 ≥1 秒");
            }
        }
        if (Constants.CFG_LOGIN_RETRY.equals(key)) {
            int n = parseInt(value);
            if (n < 1 || n > 20) {
                throw new ServiceException(ErrorCode.B05002, "锁定次数须在 1-20 之间");
            }
        }
        if (Constants.CFG_LOGIN_LOCK.equals(key)) {
            int n = parseInt(value);
            if (n < 1 || n > 1440) {
                throw new ServiceException(ErrorCode.B05002, "锁定分钟数须在 1-1440 之间");
            }
        }
        if (Constants.CFG_PWD_MIN_LENGTH.equals(key)) {
            int n = parseInt(value);
            if (n < 1 || n > 64) {
                throw new ServiceException(ErrorCode.B05002, "密码最小长度须在 1-64 之间");
            }
        }
        if (Constants.CFG_LOG_DAYS.equals(key)) {
            int n = parseInt(value);
            if (n < 1) {
                throw new ServiceException(ErrorCode.B05002, "日志保留天数须 ≥1");
            }
        }
        if (Constants.CFG_PWD_LETTER.equals(key) || Constants.CFG_PWD_DIGIT.equals(key) || Constants.CFG_PWD_SYMBOL.equals(key)) {
            if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) {
                throw new ServiceException(ErrorCode.B05002, "布尔参数只能为 true 或 false");
            }
        }
        if (Constants.CFG_INIT_PASSWORD.equals(key) && !StringUtils.hasText(value)) {
            throw new ServiceException(ErrorCode.B05002, "默认密码不能为空");
        }
    }

    private int parseInt(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            throw new ServiceException(ErrorCode.B05002, "参数值必须为数字");
        }
    }

    private long parseLong(String value) {
        try {
            return Long.parseLong(value.trim());
        } catch (Exception e) {
            throw new ServiceException(ErrorCode.B05002, "参数值必须为数字");
        }
    }

    private long getLong(String key, long def) {
        try {
            String v = getValue(key);
            return StringUtils.hasText(v) ? Long.parseLong(v.trim()) : def;
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private ConfigVO toVo(SysConfig cfg) {
        ConfigVO vo = new ConfigVO();
        vo.setConfigId(cfg.getConfigId());
        vo.setConfigName(cfg.getConfigName());
        vo.setConfigKey(cfg.getConfigKey());
        vo.setConfigValue(maskIfSecret(cfg.getConfigKey(), cfg.getConfigValue()));
        vo.setConfigType(cfg.getConfigType());
        vo.setRemark(cfg.getRemark());
        return vo;
    }

    private String maskIfSecret(String key, String value) {
        if (Constants.CFG_INIT_PASSWORD.equals(key)) {
            return "****";
        }
        return value;
    }
}
