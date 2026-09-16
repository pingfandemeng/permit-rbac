package com.company.permit.system.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.company.permit.system.role.entity.SysRoleMenu;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenu> {

    @InterceptorIgnore(dataPermission = "1")
    List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);
}
