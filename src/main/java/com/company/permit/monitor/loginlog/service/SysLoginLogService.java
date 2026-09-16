package com.company.permit.monitor.loginlog.service;

import com.company.permit.framework.web.PageResult;
import com.company.permit.monitor.loginlog.dto.LoginLogQueryDTO;
import com.company.permit.monitor.loginlog.vo.LoginLogVO;

public interface SysLoginLogService {
    PageResult<LoginLogVO> page(LoginLogQueryDTO query);

    void clean();
}
