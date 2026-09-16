package com.company.permit.system.menu.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class MenuSaveDTO {
    private Long menuId;
    private Long parentId;
    @NotBlank(message = "菜单名称不能为空")
    @Size(max = 50)
    private String menuName;
    @NotBlank(message = "菜单类型不能为空")
    private String menuType;
    private String path;
    private String component;
    private String perms;
    private Integer orderNum;
    private String visible;
    private String icon;
    private Integer version;
    private String remark;
}
