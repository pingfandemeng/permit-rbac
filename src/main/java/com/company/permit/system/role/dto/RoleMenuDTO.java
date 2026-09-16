package com.company.permit.system.role.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class RoleMenuDTO {
    @NotEmpty
    private List<Long> menuIds;
}
