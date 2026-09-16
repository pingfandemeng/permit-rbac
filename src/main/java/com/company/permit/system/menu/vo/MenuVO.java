package com.company.permit.system.menu.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MenuVO {
    private Long menuId;
    private Long parentId;
    private String menuName;
    private String menuType;
    private String path;
    private String component;
    private String perms;
    private Integer orderNum;
    private String visible;
    private String icon;
    private Integer version;
    private String remark;
    private List<MenuVO> children = new ArrayList<>();
}
