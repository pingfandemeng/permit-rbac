package com.company.permit.monitor.operlog.service;

import com.company.permit.framework.log.OperLogBO;
import com.company.permit.framework.log.OperLogWriter;
import com.company.permit.framework.web.PageResult;
import com.company.permit.monitor.operlog.dto.OperLogQueryDTO;
import com.company.permit.monitor.operlog.vo.OperLogVO;

public interface SysOperLogService extends OperLogWriter {
    PageResult<OperLogVO> page(OperLogQueryDTO query);

    void clean();
}
