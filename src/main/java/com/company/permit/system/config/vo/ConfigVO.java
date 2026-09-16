package com.company.permit.system.config.vo;

import lombok.Data;

@Data
public class ConfigVO {
    private Long configId;
    private String configName;
    private String configKey;
    private String configValue;
    private String configType;
    private String remark;
}
