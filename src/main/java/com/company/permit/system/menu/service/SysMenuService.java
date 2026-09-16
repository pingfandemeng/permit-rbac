package com.company.permit.system.menu.service;

import com.company.permit.system.auth.vo.RouterVO;
import com.company.permit.system.menu.dto.MenuSaveDTO;
import com.company.permit.system.menu.entity.SysMenu;
import com.company.permit.system.menu.vo.MenuVO;

import java.util.List;

public interface SysMenuService {
    List<MenuVO> tree();

    List<MenuVO> treeAll();

    Long createMenu(MenuSaveDTO dto);

    void updateMenu(MenuSaveDTO dto);

    void removeMenu(Long menuId);

    List<RouterVO> buildRouters(List<SysMenu> menus);

    MenuVO detail(Long menuId);
}
