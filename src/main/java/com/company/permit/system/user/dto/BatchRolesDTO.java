package com.company.permit.system.user.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class BatchRolesDTO {
    @NotEmpty
    private List<Long> userIds;
    @NotEmpty
    private List<Long> roleIds;
}
