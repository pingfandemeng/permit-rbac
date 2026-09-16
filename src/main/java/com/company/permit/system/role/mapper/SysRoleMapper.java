package com.company.permit.system.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.company.permit.system.role.entity.SysRole;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysRoleMapper extends BaseMapper<SysRole> {

    @InterceptorIgnore(dataPermission = "1")
    List<SysRole> selectRolesByUserId(@Param("userId") Long userId);
}
