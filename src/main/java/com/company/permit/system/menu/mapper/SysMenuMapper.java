package com.company.permit.system.menu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.company.permit.system.menu.entity.SysMenu;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysMenuMapper extends BaseMapper<SysMenu> {

    @InterceptorIgnore(dataPermission = "1")
    List<SysMenu> selectMenusByUserId(@Param("userId") Long userId);

    @InterceptorIgnore(dataPermission = "1")
    Long selectCountByPerms(@Param("perms") String perms, @Param("excludeId") Long excludeId);
}
