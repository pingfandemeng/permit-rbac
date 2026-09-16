package com.company.permit.system.config.service;

import com.company.permit.framework.web.PageResult;
import com.company.permit.system.config.dto.ConfigUpdateDTO;
import com.company.permit.system.config.vo.ConfigVO;

public interface SysConfigService {
    PageResult<ConfigVO> selectConfigPage(String configName, String configKey, Integer pageNum, Integer pageSize);

    String getValue(String key);

    int getInt(String key, int defaultValue);

    boolean getBool(String key, boolean defaultValue);

    void updateConfig(ConfigUpdateDTO dto);
}
