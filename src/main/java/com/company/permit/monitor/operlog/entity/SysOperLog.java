package com.company.permit.monitor.operlog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_oper_log")
public class SysOperLog {
    @TableId(value = "oper_id", type = IdType.AUTO)
    private Long operId;
    private String title;
    private String operType;
    private String method;
    private String operName;
    private String operParam;
    private String operIp;
    private String status;
    private String errorMsg;
    private Long costTime;
    private String traceId;
    private LocalDateTime operTime;
}
