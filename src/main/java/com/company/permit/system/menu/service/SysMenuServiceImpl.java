package com.company.permit.system.menu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.permit.framework.web.Constants;
import com.company.permit.framework.web.ErrorCode;
import com.company.permit.framework.web.ServiceException;
import com.company.permit.system.auth.vo.RouterVO;
import com.company.permit.system.menu.dto.MenuSaveDTO;
import com.company.permit.system.menu.entity.SysMenu;
import com.company.permit.system.menu.mapper.SysMenuMapper;
import com.company.permit.system.menu.vo.MenuVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl implements SysMenuService {

    private final SysMenuMapper menuMapper;

    @Override
    public List<MenuVO> tree() {
        return buildTree(menuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                .orderByAsc(SysMenu::getParentId, SysMenu::getOrderNum, SysMenu::getMenuId)));
    }

    @Override
    public List<MenuVO> treeAll() {
        return tree();
    }

    @Override
    public MenuVO detail(Long menuId) {
        SysMenu menu = menuMapper.selectById(menuId);
        if (menu == null) {
            throw new ServiceException(ErrorCode.C01003, "菜单不存在");
        }
        return toVo(menu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createMenu(MenuSaveDTO dto) {
        checkPerms(dto.getPerms(), null);
        SysMenu menu = new SysMenu();
        fill(menu, dto);
        if (menu.getParentId() == null) {
            menu.setParentId(0L);
        }
        menuMapper.insert(menu);
        return menu.getMenuId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMenu(MenuSaveDTO dto) {
        SysMenu db = menuMapper.selectById(dto.getMenuId());
        if (db == null) {
            throw new ServiceException(ErrorCode.C01003, "菜单不存在");
        }
        checkPerms(dto.getPerms(), dto.getMenuId());
        if (dto.getVersion() != null) {
            db.setVersion(dto.getVersion());
        }
        fill(db, dto);
        if (dto.getMenuId().equals(db.getParentId())) {
            throw new ServiceException(ErrorCode.C01003, "上级菜单不能选择自己");
        }
        int rows = menuMapper.updateById(db);
        if (rows == 0) {
            throw new ServiceException(ErrorCode.C01002);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeMenu(Long menuId) {
        Long children = menuMapper.selectCount(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getParentId, menuId));
        if (children != null && children > 0) {
            throw new ServiceException(ErrorCode.B03002);
        }
        menuMapper.deleteById(menuId);
    }

    @Override
    public List<RouterVO> buildRouters(List<SysMenu> menus) {
        if (menus == null) {
            return new ArrayList<>();
        }
        List<SysMenu> visible = menus.stream()
                .filter(m -> !Constants.MENU_BUTTON.equals(m.getMenuType()))
                .sorted(Comparator.comparing(SysMenu::getParentId, Comparator.nullsFirst(Long::compareTo))
                        .thenComparing(SysMenu::getOrderNum, Comparator.nullsLast(Integer::compareTo)))
                .collect(Collectors.toList());
        return buildRouterTree(visible, 0L);
    }

    private List<RouterVO> buildRouterTree(List<SysMenu> menus, Long parentId) {
        List<RouterVO> list = new ArrayList<>();
        for (SysMenu menu : menus) {
            if (parentId.equals(menu.getParentId() == null ? 0L : menu.getParentId())) {
                RouterVO router = new RouterVO();
                router.setName(buildRouteName(menu));
                router.setPath(buildPath(menu, parentId));
                router.setHidden("1".equals(menu.getVisible()));
                router.setComponent(resolveComponent(menu));
                RouterVO.MetaVO meta = new RouterVO.MetaVO();
                meta.setTitle(menu.getMenuName());
                meta.setIcon(menu.getIcon());
                router.setMeta(meta);
                List<RouterVO> children = buildRouterTree(menus, menu.getMenuId());
                if (!children.isEmpty()) {
                    router.setChildren(children);
                }
                list.add(router);
            }
        }
        return list;
    }

    private String buildPath(SysMenu menu, Long parentId) {
        String path = StrUtilBlank(menu.getPath());
        if (parentId == null || parentId == 0L) {
            return path.startsWith("/") ? path : "/" + path;
        }
        return path;
    }

    private String resolveComponent(SysMenu menu) {
        if (Constants.MENU_DIR.equals(menu.getMenuType())) {
            return "Layout";
        }
        return menu.getComponent();
    }

    private String buildRouteName(SysMenu menu) {
        String path = StrUtilBlank(menu.getPath()).replace("/", "-");
        return "M" + menu.getMenuId() + path;
    }

    private String StrUtilBlank(String s) {
        return s == null ? "" : s;
    }

    private void checkPerms(String perms, Long excludeId) {
        if (!StringUtils.hasText(perms)) {
            return;
        }
        Long count = menuMapper.selectCountByPerms(perms, excludeId);
        if (count != null && count > 0) {
            throw new ServiceException(ErrorCode.B03001);
        }
    }

    private void fill(SysMenu menu, MenuSaveDTO dto) {
        menu.setParentId(dto.getParentId() == null ? 0L : dto.getParentId());
        menu.setMenuName(dto.getMenuName());
        menu.setMenuType(dto.getMenuType());
        menu.setPath(dto.getPath() == null ? "" : dto.getPath());
        menu.setComponent(dto.getComponent());
        menu.setPerms(dto.getPerms());
        menu.setOrderNum(dto.getOrderNum() == null ? 0 : dto.getOrderNum());
        menu.setVisible(dto.getVisible() == null ? "0" : dto.getVisible());
        menu.setIcon(dto.getIcon() == null ? "" : dto.getIcon());
        menu.setRemark(dto.getRemark());
    }

    private List<MenuVO> buildTree(List<SysMenu> menus) {
        List<MenuVO> vos = menus.stream().map(this::toVo).collect(Collectors.toList());
        Map<Long, MenuVO> map = vos.stream().collect(Collectors.toMap(MenuVO::getMenuId, v -> v));
        List<MenuVO> roots = new ArrayList<>();
        for (MenuVO vo : vos) {
            if (vo.getParentId() == null || vo.getParentId() == 0L || !map.containsKey(vo.getParentId())) {
                roots.add(vo);
            } else {
                map.get(vo.getParentId()).getChildren().add(vo);
            }
        }
        return roots;
    }

    private MenuVO toVo(SysMenu menu) {
        MenuVO vo = new MenuVO();
        vo.setMenuId(menu.getMenuId());
        vo.setParentId(menu.getParentId());
        vo.setMenuName(menu.getMenuName());
        vo.setMenuType(menu.getMenuType());
        vo.setPath(menu.getPath());
        vo.setComponent(menu.getComponent());
        vo.setPerms(menu.getPerms());
        vo.setOrderNum(menu.getOrderNum());
        vo.setVisible(menu.getVisible());
        vo.setIcon(menu.getIcon());
        vo.setVersion(menu.getVersion());
        vo.setRemark(menu.getRemark());
        return vo;
    }
}
