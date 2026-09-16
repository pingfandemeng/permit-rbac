package com.company.permit.system.config.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class ConfigUpdateDTO {
    @NotNull
    private Long configId;
    @NotBlank
    @Size(max = 500)
    private String configValue;
    private String remark;
}
