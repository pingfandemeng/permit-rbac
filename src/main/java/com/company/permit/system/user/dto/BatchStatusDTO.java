package com.company.permit.system.user.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class BatchStatusDTO {
    @NotEmpty
    private List<Long> userIds;
    @NotBlank
    private String status;
}
